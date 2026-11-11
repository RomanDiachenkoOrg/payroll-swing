package com.acme.payroll.ui.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Display formatting shared by all screens (USD, ISO dates). */
public final class Formats {

    public static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Formats() {
    }

    public static String money(BigDecimal value) {
        if (value == null) {
            return "";
        }
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.US);
        return f.format(value);
    }

    public static String number(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    public static String date(LocalDate d) {
        return d == null ? "" : d.format(DATE);
    }

    public static String dateTime(LocalDateTime d) {
        return d == null ? "" : d.format(DATE_TIME);
    }

    /** Parses yyyy-MM-dd; returns null for blank input; throws IllegalArgumentException when malformed. */
    public static LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Use the date format yyyy-mm-dd");
        }
    }

    /** Parses a decimal allowing "$" and thousands separators; null for blank input. */
    public static BigDecimal parseDecimal(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String clean = text.trim().replace("$", "").replace(",", "");
        try {
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text.trim() + "' is not a number");
        }
    }
}
