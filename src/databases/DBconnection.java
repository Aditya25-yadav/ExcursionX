package databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBconnection — provides fresh MySQL connections on every call.
 *
 * WHY NOT SINGLETON: A shared singleton connection is closed by the
 * try-with-resources in each DAO method. Subsequent calls on the same
 * object then fail with "Connection is closed" silently.
 * Each DAO already uses try-with-resources, so connections are properly
 * closed and do not leak.
 */
public class DBconnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/excursionx"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Messi2025#";   // ← update if your password differs

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBconnection] MySQL driver not found: " + e.getMessage());
        }
    }

    /**
     * Returns a fresh, open Connection every time.
     * The caller MUST close it (use try-with-resources).
     *
     * @return new Connection, or null if the DB is unreachable
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.err.println("[DBconnection] Failed to connect: " + e.getMessage());
            return null;
        }
    }
}