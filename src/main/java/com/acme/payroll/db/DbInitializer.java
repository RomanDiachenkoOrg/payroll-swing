package com.acme.payroll.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Creates the schema and loads sample data the first time the application starts
 * against an empty database.
 */
public final class DbInitializer {

    private static final Logger LOG = Logger.getLogger(DbInitializer.class.getName());

    private DbInitializer() {
    }

    public static void initialize(Database db) {
        initialize(db, true);
    }

    /**
     * @param withSampleData when false only the schema and reference data (seed.sql) are loaded
     */
    public static void initialize(Database db, boolean withSampleData) {
        try (Connection c = db.getConnection()) {
            if (schemaExists(c)) {
                return;
            }
            LOG.info("Empty database - creating schema");
            try (Statement st = c.createStatement()) {
                st.execute("RUNSCRIPT FROM 'classpath:/db/schema.sql'");
                st.execute("RUNSCRIPT FROM 'classpath:/db/seed.sql'");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database initialisation failed", e);
        }
        if (withSampleData) {
            LOG.info("Loading sample data");
            new SeedData(db).load();
        }
    }

    private static boolean schemaExists(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'DEPARTMENT'")) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
