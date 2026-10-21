package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Function;

public final class DBConnection {
    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL JDBC driver is unavailable.", exception);
        }
        return getConnection(System::getenv, DriverManager::getConnection);
    }

    static Connection getConnection(Function<String, String> environment, Connector connector)
            throws SQLException {
        String url = environment.apply("DB_URL");
        String user = environment.apply("DB_USER");
        String password = environment.apply("DB_PASSWORD");

        if (url == null || url.isBlank() || user == null || user.isBlank() || password == null) {
            throw new SQLException("Database configuration is incomplete.");
        }
        if (!url.startsWith("jdbc:mysql://")) {
            throw new SQLException("DB_URL must use jdbc:mysql://.");
        }

        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        properties.setProperty("connectTimeout", "5000");
        properties.setProperty("socketTimeout", "5000");
        return connector.open(url, properties);
    }

    @FunctionalInterface
    interface Connector {
        Connection open(String url, Properties properties) throws SQLException;
    }
}
