import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/personal_inventory"; 
    private static final String USER = "root"; 
    private static final String PASS = "Anto@2930"; 

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL Driver not found! (Check JAR reference)", "DB Error", JOptionPane.ERROR_MESSAGE);
            return null;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "CONNECTION FAILED. Check MySQL service (Port 3306) and password.\nError: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }
}