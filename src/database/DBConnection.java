package database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection.java
 * Handles MySQL connection to Railway for the Internet Café POS System.
 *
 * Credentials are loaded from config.properties (located in src/config/).
 * NEVER hardcode credentials directly in this file.
 *
 * HOW TO USE:
 *   Connection conn = DBConnection.getConnection();
 *   // ... use conn ...
 *   DBConnection.closeConnection(conn);
 */
public class DBConnection {

    // ---------------------------------------------------------------
    // Path to the properties file (relative to the classpath/src root)
    // ---------------------------------------------------------------
    private static final String CONFIG_PATH = "/config/config.properties";

    // ---------------------------------------------------------------
    // Loaded once when the class is first used
    // ---------------------------------------------------------------
    private static final Properties props = new Properties();
    private static String jdbcUrl;
    private static String dbUsername;
    private static String dbPassword;

    static {
        loadConfig();
    }

    // ---------------------------------------------------------------
    // Loads database credentials from config.properties
    // ---------------------------------------------------------------
    private static void loadConfig() {
        try (InputStream input = DBConnection.class.getResourceAsStream(CONFIG_PATH)) {
            if (input == null) {
                System.err.println("[DBConnection] config.properties not found at: " + CONFIG_PATH);
                System.err.println("Make sure config/config.properties exists under your src/ folder.");
                return;
            }
            props.load(input);

            String host     = props.getProperty("db.host");
            String port     = props.getProperty("db.port");
            String database = props.getProperty("db.database");
            String timezone = props.getProperty("db.timezone", "Asia/Manila");

            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");

            jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=" + timezone;

            System.out.println("[DBConnection] Configuration loaded successfully.");

        } catch (IOException e) {
            System.err.println("[DBConnection] Failed to load config.properties.");
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    // Returns a live Connection object, or null if it fails
    // ---------------------------------------------------------------
    public static Connection getConnection() {
        if (jdbcUrl == null || dbUsername == null || dbPassword == null) {
            System.err.println("[DBConnection] Cannot connect — config not loaded properly.");
            return null;
        }

        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL Driver not found.");
            System.err.println("Make sure mysql-connector-j JAR is added to your project libraries.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Failed to connect to the database.");
            System.err.println("Check your Railway credentials and network connection.");
            e.printStackTrace();
        }
        return connection;
    }

    // ---------------------------------------------------------------
    // Safely closes a connection
    // ---------------------------------------------------------------
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DBConnection] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DBConnection] Error closing connection.");
                e.printStackTrace();
            }
        }
    }
}