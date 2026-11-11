package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.model.TaxBracket;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Tax brackets and simple key/value settings (contribution rates etc.).
 */
public class SettingsDao extends AbstractDao {

    public static final String SOCIAL_RATE = "rate.social";
    public static final String PENSION_RATE = "rate.pension";
    public static final String COMPANY_NAME = "company.name";

    private static final RowMapper<TaxBracket> BRACKET_MAPPER = new RowMapper<TaxBracket>() {
        @Override
        public TaxBracket map(ResultSet rs) throws SQLException {
            TaxBracket b = new TaxBracket(rs.getBigDecimal("lower_bound"), rs.getBigDecimal("upper_bound"),
                    rs.getBigDecimal("rate"));
            b.setId(rs.getLong("id"));
            return b;
        }
    };

    public SettingsDao(Database db) {
        super(db);
    }

    public List<TaxBracket> findTaxBrackets() {
        return query("SELECT * FROM tax_bracket ORDER BY lower_bound", BRACKET_MAPPER);
    }

    /** Replaces the whole bracket table in one transaction. */
    public void saveTaxBrackets(List<TaxBracket> brackets) {
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                c.createStatement().executeUpdate("DELETE FROM tax_bracket");
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO tax_bracket (lower_bound, upper_bound, rate) VALUES (?, ?, ?)")) {
                    for (TaxBracket b : brackets) {
                        ps.setBigDecimal(1, b.getLowerBound());
                        ps.setBigDecimal(2, b.getUpperBound());
                        ps.setBigDecimal(3, b.getRate());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("Saving tax brackets failed", e);
        }
    }

    public String get(String key, String defaultValue) {
        String v = queryOne("SELECT setting_value FROM app_setting WHERE setting_key = ?", new RowMapper<String>() {
            @Override
            public String map(ResultSet rs) throws SQLException {
                return rs.getString(1);
            }
        }, key);
        return v == null ? defaultValue : v;
    }

    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        String v = get(key, null);
        return v == null ? defaultValue : new BigDecimal(v.trim());
    }

    public void set(String key, String value) {
        update("MERGE INTO app_setting (setting_key, setting_value) KEY (setting_key) VALUES (?, ?)", key, value);
    }
}
