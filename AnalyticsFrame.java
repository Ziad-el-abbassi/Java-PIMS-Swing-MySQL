import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsFrame extends JFrame implements AppConstants {
    private JLabel totalItemsLabel, borrowedItemsLabel, expiredItemsLabel, maintenanceDueLabel;
    private DashboardFrame parentFrame;

    public AnalyticsFrame(DashboardFrame parent) {
        this.parentFrame = parent;
        setTitle("📊 Real-time Analytics");
        setPreferredSize(new Dimension(900, 650));
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(15, 15));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Header and Back Button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        JButton backBtn = createHeaderButton("⬅️ Back to Menu");
        backBtn.addActionListener(e -> { dispose(); parentFrame.setVisible(true); });
        
        JLabel title = new JLabel("System Statistics", SwingConstants.CENTER);
        title.setFont(SUB_HEADER_FONT);
        title.setForeground(PRIMARY_COLOR);
        
        headerPanel.add(backBtn, BorderLayout.WEST);
        headerPanel.add(title, BorderLayout.CENTER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(headerPanel, BorderLayout.NORTH);

        // --- 1. Stats Panel (West) ---
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20),
            BorderFactory.createTitledBorder("Key Statistics")
        ));
        infoPanel.setBackground(Color.WHITE);

        Font statFont = new Font("SansSerif", Font.BOLD, 18);

        totalItemsLabel = createStatLabel("Total Items: 0", statFont);
        borrowedItemsLabel = createStatLabel("Borrowed Items: 0", statFont);
        expiredItemsLabel = createStatLabel("Expired Items: 0", statFont, new Color(200, 0, 0));
        maintenanceDueLabel = createStatLabel("Maintenance Due (30 Days): 0", statFont, new Color(255, 140, 0));

        // Refresh Button in Stats Panel
        JButton refreshStatsBtn = createThemedButton("♻️ Refresh Stats");
        refreshStatsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshStatsBtn.addActionListener(e -> updateStats());

        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(totalItemsLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        infoPanel.add(borrowedItemsLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        infoPanel.add(expiredItemsLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        infoPanel.add(maintenanceDueLabel);
        infoPanel.add(Box.createVerticalStrut(30));
        infoPanel.add(refreshStatsBtn);
        infoPanel.add(Box.createVerticalGlue());

        add(infoPanel, BorderLayout.WEST);

        // --- 2. Chart Panel (Center) ---
        CustomBarChartPanel chartPanel = new CustomBarChartPanel(getAnalyticsData());
        chartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Real-Time Bar Chart"));
        chartPanel.setBackground(Color.WHITE);
        add(chartPanel, BorderLayout.CENTER);
        
        updateStats(); 

        pack();
    }
    
    private JButton createThemedButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        return button;
    }
    
    private JButton createHeaderButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(230, 230, 230));
        return button;
    }

    private JLabel createStatLabel(String text, Font font) {
        return createStatLabel(text, font, ACCENT_COLOR);
    }
    
    private JLabel createStatLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private void updateStats() {
        Map<String, Integer> data = getAnalyticsData();
        int totalItems = data.get("Total");
        int borrowedItems = data.get("Borrowed");
        int expiredItems = data.get("Expired");
        int maintenanceDue = data.get("Maintenance");
        
        if (totalItemsLabel != null) {
            totalItemsLabel.setText("Total Items: " + totalItems);
            borrowedItemsLabel.setText("Borrowed Items: " + borrowedItems);
            expiredItemsLabel.setText("Expired Items: " + expiredItems);
            maintenanceDueLabel.setText("Maintenance Due (30 Days): " + maintenanceDue);
            
            CustomBarChartPanel chartPanel = (CustomBarChartPanel) ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (chartPanel != null) {
                chartPanel.updateData(data);
            }
        }
    }
    
    private Map<String, Integer> getAnalyticsData() {
        Map<String, Integer> data = new HashMap<>();
        // Note: The keys here MUST match the keys used by the chart drawing logic below
        data.put("Total", executeCountQuery("SELECT COUNT(*) FROM items WHERE user_id = ?"));
        data.put("Borrowed", executeCountQuery("SELECT COUNT(DISTINCT item_id) FROM lending WHERE return_status = 'Pending' AND item_id IN (SELECT item_id FROM items WHERE user_id = ?)"));
        data.put("Expired", executeCountQuery("SELECT COUNT(*) FROM items WHERE expiry_date < CURDATE() AND user_id = ?"));
        data.put("Maintenance", executeCountQuery("SELECT COUNT(DISTINCT m.item_id) FROM maintenance m JOIN items i ON m.item_id = i.item_id WHERE m.next_maintenance <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) AND i.user_id = ?"));
        return data;
    }

    private int executeCountQuery(String sql) {
        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, LoginFrame.currentUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Analytics Query Error: " + e.getMessage());
        }
        return count;
    }
}

/**
 * Custom JPanel component to draw a simple bar chart based on live database data.
 * FIX: The keys used to retrieve data from the map now correctly reference the database query results.
 */
class CustomBarChartPanel extends JPanel {
    private Map<String, Integer> dataMap;
    // These labels must align with the keys used in getAnalyticsData()
    private final String[] labels = {"Total", "Borrowed", "Expired", "Maintenance"}; 
    private final String[] chartLabels = {"Total", "Lent", "Expired", "Maint."}; // Display names
    
    private final Color[] colors = {
        new Color(60, 179, 113),  
        new Color(255, 165, 0),   
        new Color(220, 20, 60),   
        new Color(70, 130, 180)   
    };

    public CustomBarChartPanel(Map<String, Integer> initialData) {
        this.dataMap = initialData;
    }
    
    public void updateData(Map<String, Integer> newData) {
        this.dataMap = newData;
        repaint(); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dataMap == null || dataMap.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int barCount = labels.length;
        int barWidth = (panelWidth / barCount) - 30;
        
        int maxValue = 0;
        for (int value : dataMap.values()) {
            if (value > maxValue) maxValue = value;
        }
        if (maxValue == 0) maxValue = 1; 
        maxValue = (int)(maxValue * 1.2); 

        // Draw Bars
        for (int i = 0; i < barCount; i++) {
            // FIX: Use the full key name from the map (e.g., "Borrowed")
            String mapKey = labels[i]; 
            int value = dataMap.getOrDefault(mapKey, 0);
            
            int barHeight = (int)(((double)value / maxValue) * (panelHeight - 80));
            int x = 20 + i * (barWidth + 20);
            int y = panelHeight - barHeight - 40;

            // Draw Bar
            g2d.setColor(colors[i]);
            g2d.fillRect(x, y, barWidth, barHeight);
            
            // Draw Label (Display Name: Lent, Maint.)
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2d.drawString(chartLabels[i], x + (barWidth / 2) - 15, panelHeight - 20);
            
            // Draw Value (Number)
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2d.drawString(String.valueOf(value), x + (barWidth / 2) - 5, y - 5);
        }

        // Draw X-axis line
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawLine(0, panelHeight - 40, panelWidth, panelHeight - 40);
    }
}