import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame implements AppConstants {
    public DashboardFrame() {
        setTitle("PIMS Dashboard");
        
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout()); 
        setPreferredSize(new Dimension(600, 500)); 

        JLabel header = new JLabel("Inventory Management Dashboard", SwingConstants.CENTER);
        header.setFont(HEADER_FONT);
        header.setForeground(PRIMARY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        add(header, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(5, 1, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        JButton inventoryBtn = createStyledButton("📦 Inventory Management");
        JButton maintenanceBtn = createStyledButton("🛠️ Maintenance Tracker");
        JButton lendingBtn = createStyledButton("🤝 Lending & Borrowing");
        JButton analyticsBtn = createStyledButton("📊 System Analytics");
        JButton exitBtn = createStyledButton("🚪 Exit Application");

        mainPanel.add(inventoryBtn);
        mainPanel.add(maintenanceBtn);
        mainPanel.add(lendingBtn);
        mainPanel.add(analyticsBtn);
        mainPanel.add(exitBtn);

        add(mainPanel, BorderLayout.CENTER);

        inventoryBtn.addActionListener(e -> new InventoryFrame(this).setVisible(true));
        maintenanceBtn.addActionListener(e -> new MaintenanceFrame(this).setVisible(true));
        lendingBtn.addActionListener(e -> new LendingFrame(this).setVisible(true));
        analyticsBtn.addActionListener(e -> new AnalyticsFrame(this).setVisible(true));
        
        exitBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        pack(); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
    }
    
    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(SUB_HEADER_FONT);
        button.setBackground(new Color(230, 230, 230)); 
        button.setForeground(ACCENT_COLOR); 
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Hover effect simulation
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR); 
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 230, 230));
                button.setForeground(ACCENT_COLOR);
            }
        });
        return button;
    }
}