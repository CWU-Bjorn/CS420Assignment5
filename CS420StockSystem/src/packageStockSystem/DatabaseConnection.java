package packageStockSystem;

import java.sql.*;

/**
 * Singleton JDBC connection manager for the Stock Portfolio Database
 * Fill in URL, USER, and PASSWORD before running.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/CS420StockProject";
    private static final String USER     = "root";     
    private static final String PASSWORD = "testTestPassword";   
    
    private static Connection connection = null;

    /** Returns the shared connection, opening it if necessary. */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. "
                        + "Add mysql-connector-j to your build path.", e);
            }
        }
        return connection;
    }

    /** Closes the shared connection gracefully — call on application exit. */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }

    private DatabaseConnection() {}   // prevent instantiation
}

