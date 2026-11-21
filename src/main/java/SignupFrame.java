import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignupFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    private static final Color PRIMARY_COLOR = new Color(60, 179, 113);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); 

    public SignupFrame() {
        setTitle("✍️ Create Account");
        setSize(400, 280);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(15, 15));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JLabel header = new JLabel("Create New Account", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setForeground(PRIMARY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        inputPanel.setBackground(BACKGROUND_COLOR);

        inputPanel.add(new JLabel("New Username:"));
        usernameField = new JTextField(15);
        inputPanel.add(usernameField);

        inputPanel.add(new JLabel("New Password:"));
        passwordField = new JPasswordField(15);
        inputPanel.add(passwordField);

        add(inputPanel, BorderLayout.CENTER);

        JButton signupBtn = createThemedButton("Create Account");
        signupBtn.setPreferredSize(new Dimension(170, 35));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(signupBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        signupBtn.addActionListener(e -> registerUser());
    }
    
    private JButton createThemedButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        return button;
    }

    private void registerUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password); 
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different one.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error during registration: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}