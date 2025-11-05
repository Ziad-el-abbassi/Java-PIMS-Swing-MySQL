import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame implements AppConstants {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public static int currentUserId = -1;

    public LoginFrame() {
        setTitle("🔑 PIMS Login");
        setSize(400, 300);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(20, 20));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel header = new JLabel("Personal Inventory Management", SwingConstants.CENTER);
        header.setFont(SUB_HEADER_FONT);
        header.setForeground(PRIMARY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        inputPanel.setBackground(BACKGROUND_COLOR);

        inputPanel.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        inputPanel.add(usernameField);

        inputPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        inputPanel.add(passwordField);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton loginBtn = createThemedButton("🔑 Login");
        JButton signupBtn = createThemedButton("✍️ Sign Up");
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(signupBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> authenticate());
        signupBtn.addActionListener(e -> new SignupFrame().setVisible(true));
        
        // Expert Improvement #1: Security Note
        JOptionPane.showMessageDialog(this, 
            "NOTE FOR VIVA:\nCredentials are compared in plain text for demonstration purposes. In production, passwords must be securely hashed (e.g., using BCrypt).", 
            "Security Notice", JOptionPane.WARNING_MESSAGE);
    }
    
    private JButton createThemedButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(130, 35));
        return button;
    }

    private void authenticate() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        String sql = "SELECT user_id, password FROM users WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                if (password.equals(storedPassword)) { 
                    currentUserId = rs.getInt("user_id");
                    new DashboardFrame().setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "User not found.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error during login.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}