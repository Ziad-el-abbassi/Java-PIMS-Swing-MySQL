import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class LendingFrame extends JFrame implements AppConstants {
    private JTextField itemIdField, personField, dateLentField, dueDateField;
    private JComboBox<String> returnStatusBox;
    private JTable table;
    private DefaultTableModel model;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private DashboardFrame parentFrame;

    public LendingFrame(DashboardFrame parent) {
        this.parentFrame = parent;
        setTitle("🤝 Lending / Borrowing Tracker");
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
        
        JLabel title = new JLabel("Lent and Borrowed Item Records", SwingConstants.CENTER);
        title.setFont(SUB_HEADER_FONT);
        title.setForeground(PRIMARY_COLOR);
        
        headerPanel.add(backBtn, BorderLayout.WEST);
        headerPanel.add(title, BorderLayout.CENTER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(headerPanel, BorderLayout.NORTH);

        // --- 1. Form Panel (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        southPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 15, 10));
        inputPanel.setBackground(BACKGROUND_COLOR);

        itemIdField = new JTextField();
        personField = new JTextField();
        dateLentField = new JTextField();
        dueDateField = new JTextField();
        returnStatusBox = new JComboBox<>(new String[]{"Pending", "Returned"});
        
        // Date Chooser Buttons (Calendar functionality)
        JButton lentDateBtn = new JButton("🗓️");
        lentDateBtn.addActionListener(e -> new DateChooserDialog(this, dateLentField).setVisible(true));
        JButton dueDateBtn = new JButton("🗓️");
        dueDateBtn.addActionListener(e -> new DateChooserDialog(this, dueDateField).setVisible(true));
        
        // Input rows
        inputPanel.add(new JLabel("Item ID:")); inputPanel.add(itemIdField);
        inputPanel.add(new JLabel("Person Name:")); inputPanel.add(personField);
        
        inputPanel.add(new JLabel("Date Lent:")); 
        JPanel lDatePanel = new JPanel(new BorderLayout()); lDatePanel.add(dateLentField, BorderLayout.CENTER); lDatePanel.add(lentDateBtn, BorderLayout.EAST);
        inputPanel.add(lDatePanel);
        
        inputPanel.add(new JLabel("Due Date:")); 
        JPanel dDatePanel = new JPanel(new BorderLayout()); dDatePanel.add(dueDateField, BorderLayout.CENTER); dDatePanel.add(dueDateBtn, BorderLayout.EAST);
        inputPanel.add(dDatePanel);
        
        // Button Group
        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        buttonGroup.setBackground(BACKGROUND_COLOR);
        JButton addBtn = createThemedButton("➕ Add Lending");
        JButton updateStatusBtn = createThemedButton("✅ Mark as Returned");
        JButton refreshBtn = createThemedButton("♻️ Refresh Data"); 
        JButton clearFormBtn = createThemedButton("🧹 Clear Form"); 
        
        buttonGroup.add(addBtn);
        buttonGroup.add(updateStatusBtn);
        buttonGroup.add(refreshBtn);
        buttonGroup.add(clearFormBtn);

        southPanel.add(inputPanel, BorderLayout.NORTH);
        southPanel.add(buttonGroup, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH); 

        // --- 2. Table Panel (CENTER) ---
        String[] columnNames = {"Lend ID", "Item ID", "Item Name", "Person", "Date Lent", "Due Date", "Status"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        loadLendingData();

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- 3. Action Listeners ---
        addBtn.addActionListener(e -> addLending());
        updateStatusBtn.addActionListener(e -> updateLendingStatus());
        refreshBtn.addActionListener(e -> loadLendingData());
        clearFormBtn.addActionListener(e -> clearFields());
    }
    
    private void clearFields() {
        itemIdField.setText("");
        personField.setText("");
        dateLentField.setText("");
        dueDateField.setText("");
        returnStatusBox.setSelectedIndex(0);
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

    private void loadLendingData() {
        model.setRowCount(0);
        String sql = "SELECT l.lending_id, l.item_id, i.item_name, l.person_name, l.date_lent, l.due_date, l.return_status " +
                     "FROM lending l JOIN items i ON l.item_id = i.item_id WHERE i.user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, LoginFrame.currentUserId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("lending_id"),
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getString("person_name"),
                    dateFormat.format(rs.getDate("date_lent")),
                    dateFormat.format(rs.getDate("due_date")),
                    rs.getString("return_status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading lending data: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addLending() {
        String sql = "INSERT INTO lending (item_id, person_name, date_lent, due_date, return_status) VALUES (?, ?, ?, ?, ?)";
        
        // Expert Improvement #3: Robust Number Validation
        try {
            Integer.parseInt(itemIdField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Item ID must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (!checkItemOwnership(Integer.parseInt(itemIdField.getText()))) {
                JOptionPane.showMessageDialog(this, "Item ID not found in your inventory.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            pstmt.setInt(1, Integer.parseInt(itemIdField.getText()));
            pstmt.setString(2, personField.getText());
            pstmt.setDate(3, new Date(dateFormat.parse(dateLentField.getText()).getTime()));
            pstmt.setDate(4, new Date(dateFormat.parse(dueDateField.getText()).getTime()));
            pstmt.setString(5, (String) returnStatusBox.getSelectedItem());
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Lending record added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadLendingData();
            clearFields();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding lending record: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateLendingStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a pending record to mark as returned.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int lendingId = (int) model.getValueAt(selectedRow, 0);
        String sql = "UPDATE lending SET return_status = 'Returned' WHERE lending_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, lendingId);
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Record marked as Returned!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadLendingData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean checkItemOwnership(int itemId) {
        String sql = "SELECT item_id FROM items WHERE item_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, LoginFrame.currentUserId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}