package com.acme.payroll.controller;

import com.acme.payroll.AppContext;
import com.acme.payroll.dao.SettingsDao;
import com.acme.payroll.model.AuditEntry;
import com.acme.payroll.model.TaxBracket;
import com.acme.payroll.model.User;
import com.acme.payroll.service.Session;
import com.acme.payroll.service.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdminController {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final AppContext ctx;

    public AdminController(AppContext ctx) {
        this.ctx = ctx;
    }

    public List<TaxBracket> taxBrackets() {
        return ctx.getSettingsDao().findTaxBrackets();
    }

    public BigDecimal socialRate() {
        return ctx.getSettingsDao().getDecimal(SettingsDao.SOCIAL_RATE, new BigDecimal("5"));
    }

    public BigDecimal pensionRate() {
        return ctx.getSettingsDao().getDecimal(SettingsDao.PENSION_RATE, new BigDecimal("3"));
    }

    /**
     * Saves brackets and contribution rates. Brackets must start at 0, be contiguous, and only the
     * last one may be open-ended.
     */
    public void saveTaxSettings(List<TaxBracket> brackets, BigDecimal socialRate, BigDecimal pensionRate) {
        List<String> errors = new ArrayList<String>();
        if (brackets.isEmpty()) {
            errors.add("At least one tax bracket is required.");
        }
        BigDecimal expectedLower = BigDecimal.ZERO;
        for (int i = 0; i < brackets.size(); i++) {
            TaxBracket b = brackets.get(i);
            String row = "Bracket " + (i + 1) + ": ";
            if (b.getLowerBound() == null || b.getRate() == null) {
                errors.add(row + "lower bound and rate are required.");
                continue;
            }
            if (b.getLowerBound().compareTo(expectedLower) != 0) {
                errors.add(row + "must start at " + expectedLower.toPlainString() + ".");
            }
            if (b.getRate().signum() < 0 || b.getRate().compareTo(HUNDRED) > 0) {
                errors.add(row + "rate must be between 0 and 100.");
            }
            boolean last = i == brackets.size() - 1;
            if (b.getUpperBound() == null) {
                if (!last) {
                    errors.add(row + "only the last bracket can have no upper bound.");
                }
            } else {
                if (b.getUpperBound().compareTo(b.getLowerBound()) <= 0) {
                    errors.add(row + "upper bound must be greater than lower bound.");
                }
                if (last) {
                    errors.add(row + "the last bracket must have no upper bound.");
                }
                expectedLower = b.getUpperBound();
            }
        }
        checkRate("Social contribution", socialRate, errors);
        checkRate("Pension", pensionRate, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        ctx.getSettingsDao().saveTaxBrackets(brackets);
        ctx.getSettingsDao().set(SettingsDao.SOCIAL_RATE, socialRate.toPlainString());
        ctx.getSettingsDao().set(SettingsDao.PENSION_RATE, pensionRate.toPlainString());
        ctx.getAuditLogDao().log(Session.actor(), "SETTINGS_UPDATE", "TaxBracket", null,
                brackets.size() + " brackets, social " + socialRate + "%, pension " + pensionRate + "%");
    }

    private static void checkRate(String label, BigDecimal rate, List<String> errors) {
        if (rate == null) {
            errors.add(label + " rate is required.");
        } else if (rate.signum() < 0 || rate.compareTo(HUNDRED) > 0) {
            errors.add(label + " rate must be between 0 and 100.");
        }
    }

    public List<User> users() {
        return ctx.getUserDao().findAll();
    }

    public void setUserActive(User user, boolean active) {
        if (Session.getUser() != null && Session.getUser().getId().equals(user.getId()) && !active) {
            throw new ValidationException("You cannot disable your own account.");
        }
        ctx.getUserDao().setActive(user.getId(), active);
        ctx.getAuditLogDao().log(Session.actor(), active ? "USER_ENABLE" : "USER_DISABLE", "User", user.getId(),
                user.getEmail());
    }

    public List<AuditEntry> auditLog(String user, String action, LocalDate from, LocalDate to) {
        return ctx.getAuditLogDao().find(user, action, from, to, 1000);
    }

    public List<String> auditActions() {
        return ctx.getAuditLogDao().findDistinctActions();
    }
}
