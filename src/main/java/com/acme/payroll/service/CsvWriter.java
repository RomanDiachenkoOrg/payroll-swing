package com.acme.payroll.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Minimal RFC 4180 CSV writer (comma separated, quoted when needed, CRLF line endings).
 */
public final class CsvWriter {

    private CsvWriter() {
    }

    public static void write(File file, List<String> header, List<List<Object>> rows) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            write(w, header, rows);
        }
    }

    public static void write(Writer w, List<String> header, List<List<Object>> rows) throws IOException {
        writeLine(w, header);
        for (List<Object> row : rows) {
            writeLine(w, row);
        }
        w.flush();
    }

    private static void writeLine(Writer w, List<?> values) throws IOException {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                w.write(',');
            }
            w.write(escape(values.get(i)));
        }
        w.write("\r\n");
    }

    static String escape(Object value) {
        if (value == null) {
            return "";
        }
        String s = value instanceof java.math.BigDecimal
                ? ((java.math.BigDecimal) value).toPlainString() : value.toString();
        if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}
