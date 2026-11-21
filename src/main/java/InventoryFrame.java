import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InventoryFrame extends JFrame implements AppConstants {
    private JTable table;
    private DefaultTableModel model;
    private JTextField idField, nameField, purchaseDateField, expiryDateField, quantityField, conditionField;
    private JTextField categoryField; // CHANGED to JTextField
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private DashboardFrame parentFrame;

    public InventoryFrame(DashboardFrame parent) {
        this.parentFrame = parent;
        setTitle("📦 Inventory Management");
        setSize(1000, 700);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header and Back Button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        JButton backBtn = createHeaderButton("⬅️ Back to Menu");
        backBtn.addActionListener(e -> { dispose(); parentFrame.setVisible(true); });
        
        JLabel title = new JLabel("Inventory Items (CRUD)", SwingConstants.CENTER);
        title.setFont(SUB_HEADER_FONT);
        title.setForeground(PRIMARY_COLOR);
        
        headerPanel.add(backBtn, BorderLayout.WEST);
        headerPanel.add(title, BorderLayout.CENTER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(headerPanel, BorderLayout.NORTH);

        // --- 1. Table Setup ---
        String[] columnNames = {"ID", "Name", "Category", "Purchase Date", "Expiry Date", "Quantity", "Condition"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        table.getTableHeader().setBackground(PRIMARY_COLOR); 
        table.getTableHeader().setForeground(Color.WHITE);
        
        // Removed categoryComboBox setup
        loadInventoryData(); 

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- 2. Input and Button Panel (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        southPanel.setBackground(BACKGROUND_COLOR);

        // Input Fields (GridLayout for Alignment)
        JPanel inputPanel = new JPanel(new GridLayout(4, 4, 15, 10));
        inputPanel.setBackground(BACKGROUND_COLOR);
        
        idField = new JTextField(); idField.setEditable(false); 
        nameField = new JTextField();
        categoryField = new JTextField(); // INITIALIZED TEXT FIELD
        purchaseDateField = new JTextField();
        expiryDateField = new JTextField();
        quantityField = new JTextField();
        conditionField = new JTextField();

        // Date Chooser Buttons (Calendar functionality)
        JButton purchaseDateBtn = new JButton("🗓️");
        purchaseDateBtn.addActionListener(e -> new DateChooserDialog(this, purchaseDateField).setVisible(true));
        JButton expiryDateBtn = new JButton("🗓️");
        expiryDateBtn.addActionListener(e -> new DateChooserDialog(this, expiryDateField).setVisible(true));

        // Row 1
        inputPanel.add(new JLabel("Item ID (View Only):")); inputPanel.add(idField);
        inputPanel.add(new JLabel("Name:")); inputPanel.add(nameField);
        
        // Row 2
        inputPanel.add(new JLabel("Category:")); inputPanel.add(categoryField); // ADDED TEXT FIELD
        inputPanel.add(new JLabel("Condition:")); inputPanel.add(conditionField);
        
        // Row 3 (Purchase Date)
        inputPanel.add(new JLabel("Purchase Date:")); 
        JPanel pDatePanel = new JPanel(new BorderLayout()); pDatePanel.add(purchaseDateField, BorderLayout.CENTER); pDatePanel.add(purchaseDateBtn, BorderLayout.EAST);
        inputPanel.add(pDatePanel);
        
        // Row 4 (Expiry Date)
        inputPanel.add(new JLabel("Expiry Date (N/A OK):")); 
        JPanel eDatePanel = new JPanel(new BorderLayout()); eDatePanel.add(expiryDateField, BorderLayout.CENTER); eDatePanel.add(expiryDateBtn, BorderLayout.EAST);
        inputPanel.add(eDatePanel);
        
        // Final Row
        inputPanel.add(new JLabel("Quantity (Numbers Only):")); inputPanel.add(quantityField);
        inputPanel.add(new JLabel()); inputPanel.add(new JLabel()); // Spacers

        
        // Buttons (FlowLayout for Alignment and Spacing)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton addBtn = createThemedButton("➕ Add Item");
        JButton updateBtn = createThemedButton("🔄 Update Item");
        JButton deleteBtn = createThemedButton("🗑️ Delete Item");
        JButton refreshBtn = createThemedButton("♻️ Refresh Data"); 
        JButton clearFormBtn = createThemedButton("🧹 Clear Form"); 
        
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(clearFormBtn);
        
        southPanel.add(inputPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        // --- 3. Action Listeners ---
        addBtn.addActionListener(e -> addItem());
        updateBtn.addActionListener(e -> updateItem());
        deleteBtn.addActionListener(e -> deleteItem());
        refreshBtn.addActionListener(e -> loadInventoryData()); 
        clearFormBtn.addActionListener(e -> clearFields());     
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    populateFields();
                }
            }
        });
    }
    
    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        categoryField.setText(""); // Cleared
        purchaseDateField.setText("");
        expiryDateField.setText("");
        quantityField.setText("");
        conditionField.setText("");
        table.clearSelection();
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

    // Removed loadCategories method as it's no longer needed

    private void loadInventoryData() {
        model.setRowCount(0); 
        String sql = "SELECT * FROM items WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, LoginFrame.currentUserId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String expiry = rs.getDate("expiry_date") != null ? dateFormat.format(rs.getDate("expiry_date")) : "N/A";
                model.addRow(new Object[]{
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getString("category"),
                    dateFormat.format(rs.getDate("purchase_date")),
                    expiry,
                    rs.getInt("quantity"),
                    rs.getString("condition_status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFields() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            idField.setText(model.getValueAt(selectedRow, 0).toString());
            nameField.setText(model.getValueAt(selectedRow, 1).toString());
            categoryField.setText(model.getValueAt(selectedRow, 2).toString()); // POPULATE TEXT FIELD
            purchaseDateField.setText(model.getValueAt(selectedRow, 3).toString());
            expiryDateField.setText(model.getValueAt(selectedRow, 4).toString());
            quantityField.setText(model.getValueAt(selectedRow, 5).toString());
            conditionField.setText(model.getValueAt(selectedRow, 6).toString());
        }
    }

    private void addItem() {
        String name = nameField.getText();
        String pDate = purchaseDateField.getText();
        String quantityStr = quantityField.getText();
        String categoryStr = categoryField.getText(); // GET value from JTextField
        
        if (name.isEmpty() || pDate.isEmpty() || quantityStr.isEmpty() || categoryStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Purchase Date, Quantity, and Category are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Integer.parseInt(quantityStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String sql = "INSERT INTO items (user_id, item_name, category, purchase_date, expiry_date, quantity, condition_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String eDate = expiryDateField.getText();
            Date expiryDate = (eDate.toUpperCase().equals("N/A") || eDate.isEmpty()) ? null : dateFormat.parse(eDate);

            pstmt.setInt(1, LoginFrame.currentUserId);
            pstmt.setString(2, name);
            pstmt.setString(3, categoryStr); // INSERT the typed category string
            pstmt.setDate(4, new java.sql.Date(dateFormat.parse(pDate).getTime()));
            
            if (expiryDate != null) {
                pstmt.setDate(5, new java.sql.Date(expiryDate.getTime()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }
            pstmt.setInt(6, Integer.parseInt(quantityStr));
            pstmt.setString(7, conditionField.getText());
            
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadInventoryData(); 
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding item: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateItem() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String categoryStr = categoryField.getText(); // GET value from JTextField
        String quantityStr = quantityField.getText();
        
        try {
            Integer.parseInt(quantityStr);
            Integer.parseInt(idField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Item ID must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE items SET item_name=?, category=?, purchase_date=?, expiry_date=?, quantity=?, condition_status=? WHERE item_id=? AND user_id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String eDate = expiryDateField.getText();
            Date expiryDate = (eDate.toUpperCase().equals("N/A") || eDate.isEmpty()) ? null : dateFormat.parse(eDate);

            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, categoryStr); // UPDATE the typed category string
            pstmt.setDate(3, new java.sql.Date(dateFormat.parse(purchaseDateField.getText()).getTime()));
            
            if (expiryDate != null) {
                pstmt.setDate(4, new java.sql.Date(expiryDate.getTime()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            pstmt.setInt(5, Integer.parseInt(quantityField.getText()));
            pstmt.setString(6, conditionField.getText());
            pstmt.setInt(7, Integer.parseInt(idField.getText()));
            pstmt.setInt(8, LoginFrame.currentUserId);
            
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Item updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadInventoryData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating item: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteItem() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item? This will also remove associated records.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM items WHERE item_id = ? AND user_id = ?";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, Integer.parseInt(idField.getText()));
                pstmt.setInt(2, LoginFrame.currentUserId);
                
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Item deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInventoryData();
                clearFields();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting item: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}