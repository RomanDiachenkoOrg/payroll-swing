package com.acme.payroll.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Thin wrapper around the embedded H2 database. By default the data file lives in
 * {@code ~/.payroll-swing/payroll.mv.db}; override the folder with {@code -Dpayroll.home=...}.
 */
public final class Database {

    private static final Logger LOG = Logger.getLogger(Database.class.getName());

    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private final String url;

    private Database(String url) {
        this.url = url;
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("H2 driver not on classpath", e);
        }
    }

    public static Database fileDatabase() {
        String home = System.getProperty("payroll.home",
                System.getProperty("user.home") + File.separator + ".payroll-swing");
        File dir = new File(home);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create data directory " + dir.getAbsolutePath());
        }
        String path = new File(dir, "payroll").getAbsolutePath().replace('\\', '/');
        LOG.info("Using database " + path);
        return new Database("jdbc:h2:file:" + path + ";DB_CLOSE_DELAY=-1");
    }

    public static Database inMemory(String name) {
        return new Database("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, USER, PASSWORD);
    }

    public String getUrl() {
        return url;
    }

    /** Closes the embedded database (used on application exit). */
    public void shutdown() {
        try (Connection c = getConnection()) {
            c.createStatement().execute("SHUTDOWN");
        } catch (SQLException e) {
            LOG.warning("Shutdown failed: " + e.getMessage());
        }
    }
}
