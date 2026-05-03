package database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final String CONFIG_PATH = "/config/config.properties";
    private static String jdbcUrl;
    private static String dbUsername;
    private static String dbPassword;

    // Single shared connection — reused across all DAO calls
    private static Connection cachedConnection = null;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream in =
                DBConnection.class.getResourceAsStream(CONFIG_PATH)) {
            if (in == null) {
                System.err.println("[DB] config.properties not found.");
                return;
            }
            Properties props = new Properties();
            props.load(in);

            String host     = props.getProperty("db.host");
            String port     = props.getProperty("db.port");
            String database = props.getProperty("db.database");
            String tz       = props.getProperty("db.timezone",
                                                 "Asia/Manila");
            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");

            jdbcUrl = "jdbc:mysql://" + host + ":" + port
                    + "/" + database
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&autoReconnect=true"
                    + "&serverTimezone=" + tz;

            System.out.println("[DB] Config loaded.");
        } catch (IOException e) {
            System.err.println("[DB] Failed to load config.");
            e.printStackTrace();
        }
    }

    // Returns the cached connection, reconnecting if needed.
    // synchronized prevents two SwingWorkers from reconnecting
    // at the same time.
    public static synchronized Connection getConnection() {
        if (jdbcUrl == null) return null;
        try {
            if (cachedConnection == null
                    || cachedConnection.isClosed()
                    || !cachedConnection.isValid(3)) {
                System.out.println("[DB] Connecting...");
                Class.forName("com.mysql.cj.jdbc.Driver");
                cachedConnection = DriverManager.getConnection(
                        jdbcUrl, dbUsername, dbPassword);
                System.out.println("[DB] Connected.");
            }
            return cachedConnection;
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed.");
            e.printStackTrace();
        }
        return null;
    }

    // No-op — connection is kept alive for reuse.
    // Only called to satisfy existing DAO code; does not close.
    public static void closeConnection(Connection conn) {
        // Intentionally empty — connection is cached and reused.
    }

    // Call this only when the application is closing.
    public static void shutdown() {
        if (cachedConnection != null) {
            try {
                cachedConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}