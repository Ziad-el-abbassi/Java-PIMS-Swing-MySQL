import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MaintenanceFrame extends JFrame implements AppConstants {
    private JTable table;
    private DefaultTableModel model;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private JTextField itemIdField, itemNameField, lastDateField, nextDateField, notesField;
    private DashboardFrame parentFrame;

    public MaintenanceFrame(DashboardFrame parent) {
        this.parentFrame = parent;
        setTitle("🛠️ Maintenance Tracker");
        setSize(950, 650);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header and Back Button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        JButton backBtn = createHeaderButton("⬅️ Back to Menu");
        backBtn.addActionListener(e -> { dispose(); parentFrame.setVisible(true); });
        
        JLabel title = new JLabel("Maintenance Schedule", SwingConstants.CENTER);
        title.setFont(SUB_HEADER_FONT);
        title.setForeground(PRIMARY_COLOR);
        
        headerPanel.add(backBtn, BorderLayout.WEST);
        headerPanel.add(title, BorderLayout.CENTER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(headerPanel, BorderLayout.NORTH);

        // --- 1. Table Setup ---
        String[] columnNames = {"Maint. ID", "Item ID", "Item Name", "Last Date", "Next Date", "Notes"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        loadMaintenanceData(); 

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- 2. Input and Control Panel (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        southPanel.setBackground(BACKGROUND_COLOR);
        
        // Input Fields (GridLayout for Alignment)
        JPanel inputPanel = new JPanel(new GridLayout(3, 4, 15, 10));
        inputPanel.setBackground(BACKGROUND_COLOR);

        itemIdField = new JTextField(); 
        itemNameField = new JTextField(); itemNameField.setEditable(false);
        lastDateField = new JTextField();
        nextDateField = new JTextField();
        notesField = new JTextField();

        // Date Chooser Buttons (Calendar functionality)
        JButton lastDateBtn = new JButton("🗓️");
        lastDateBtn.addActionListener(e -> new DateChooserDialog(this, lastDateField).setVisible(true));
        JButton nextDateBtn = new JButton("🗓️");
        nextDateBtn.addActionListener(e -> new DateChooserDialog(this, nextDateField).setVisible(true));

        inputPanel.add(new JLabel("Item ID:")); inputPanel.add(itemIdField);
        inputPanel.add(new JLabel("Item Name:")); inputPanel.add(itemNameField);
        
        // Date Fields (Now fully integrated)
        inputPanel.add(new JLabel("Last Date:")); 
        JPanel lDatePanel = new JPanel(new BorderLayout()); lDatePanel.add(lastDateField, BorderLayout.CENTER); lDatePanel.add(lastDateBtn, BorderLayout.EAST);
        inputPanel.add(lDatePanel);
        
        inputPanel.add(new JLabel("Next Date:")); 
        JPanel nDatePanel = new JPanel(new BorderLayout()); nDatePanel.add(nextDateField, BorderLayout.CENTER); nDatePanel.add(nextDateBtn, BorderLayout.EAST);
        inputPanel.add(nDatePanel);

        inputPanel.add(new JLabel("Notes:")); inputPanel.add(notesField);
        
        // Buttons (FlowLayout for Alignment and Spacing)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 15));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton addBtn = createThemedButton("➕ Add/Update Log");
        JButton checkDueBtn = createThemedButton("🔔 Check Reminders");
        JButton refreshBtn = createThemedButton("♻️ Refresh Data"); 
        JButton clearFormBtn = createThemedButton("🧹 Clear Form"); 
        
        buttonPanel.add(addBtn);
        buttonPanel.add(checkDueBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(clearFormBtn);
        
        southPanel.add(inputPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        // --- 3. Action Listeners ---
        addBtn.addActionListener(e -> addMaintenance());
        checkDueBtn.addActionListener(e -> showReminders());
        refreshBtn.addActionListener(e -> loadMaintenanceData());
        clearFormBtn.addActionListener(e -> clearFields());
        
        table.getSelectionModel().addListSelectionListener(e -> populateFields());
        itemIdField.addActionListener(e -> getItemName(itemIdField.getText()));
    }
    
    private void clearFields() {
        itemIdField.setText("");
        itemNameField.setText("");
        lastDateField.setText("");
        nextDateField.setText("");
        notesField.setText("");
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
    
    private void getItemName(String itemId) {
        String sql = "SELECT item_name FROM items WHERE item_id = ? AND user_id = ?";
        try {
            Integer.parseInt(itemId);
        } catch (NumberFormatException ex) {
             itemNameField.setText("Invalid ID Format!");
             return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(itemId));
            pstmt.setInt(2, LoginFrame.currentUserId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                itemNameField.setText(rs.getString("item_name"));
            } else {
                itemNameField.setText("Item not found!");
            }
        } catch (Exception ex) {
            itemNameField.setText("DB Error");
        }
    }
    
    private void loadMaintenanceData() {
        model.setRowCount(0);
        String sql = "SELECT m.maintenance_id, m.item_id, i.item_name, m.last_maintenance, m.next_maintenance, m.notes " +
                     "FROM maintenance m JOIN items i ON m.item_id = i.item_id WHERE i.user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, LoginFrame.currentUserId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("maintenance_id"),
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    dateFormat.format(rs.getDate("last_maintenance")),
                    dateFormat.format(rs.getDate("next_maintenance")),
                    rs.getString("notes")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading maintenance data: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addMaintenance() {
        String sql = "INSERT INTO maintenance (item_id, last_maintenance, next_maintenance, notes) VALUES (?, ?, ?, ?)";
        
        try {
            Integer.parseInt(itemIdField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Item ID must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (itemNameField.getText().contains("not found")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid Item ID from your inventory.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            pstmt.setInt(1, Integer.parseInt(itemIdField.getText()));
            pstmt.setDate(2, new java.sql.Date(dateFormat.parse(lastDateField.getText()).getTime()));
            pstmt.setDate(3, new java.sql.Date(dateFormat.parse(nextDateField.getText()).getTime()));
            pstmt.setString(4, notesField.getText());
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Maintenance record added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadMaintenanceData();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding maintenance: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFields() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            itemIdField.setText(model.getValueAt(selectedRow, 1).toString());
            itemNameField.setText(model.getValueAt(selectedRow, 2).toString());
            lastDateField.setText(model.getValueAt(selectedRow, 3).toString());
            nextDateField.setText(model.getValueAt(selectedRow, 4).toString());
            notesField.setText(model.getValueAt(selectedRow, 5).toString());
        }
    }

    private void showReminders() {
        String sql = "SELECT i.item_name, m.next_maintenance FROM maintenance m JOIN items i ON m.item_id = i.item_id WHERE i.user_id = ?";
        StringBuilder reminderMessage = new StringBuilder("Upcoming Maintenance Reminders (Due in 30 days):\n\n");
        long today = new Date().getTime();
        int reminderCount = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, LoginFrame.currentUserId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Date nextDate = rs.getDate("next_maintenance");
                if (nextDate != null) {
                    long diff = nextDate.getTime() - today;
                    long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                    
                    if (days >= 0 && days <= 30) { 
                        reminderMessage.append(String.format("- %s is due on %s (in %d days).\n", 
                            rs.getString("item_name"), dateFormat.format(nextDate), days));
                        reminderCount++;
                    }
                }
            }
            
            if (reminderCount > 0) {
                JOptionPane.showMessageDialog(this, reminderMessage.toString(), "Maintenance Alert! (Total: " + reminderCount + ")", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No maintenance due in the next 30 days. You're all set!", "Maintenance Check", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error checking reminders: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}