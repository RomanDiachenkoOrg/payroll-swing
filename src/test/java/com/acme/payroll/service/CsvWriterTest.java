package com.acme.payroll.service;

import org.junit.Test;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CsvWriterTest {

    @Test
    public void quotesValuesWithCommasAndQuotes() {
        assertEquals("\"Smith, John\"", CsvWriter.escape("Smith, John"));
        assertEquals("\"say \"\"hi\"\"\"", CsvWriter.escape("say \"hi\""));
        assertEquals("plain", CsvWriter.escape("plain"));
        assertEquals("", CsvWriter.escape(null));
        assertEquals("1200.50", CsvWriter.escape(new BigDecimal("1200.50")));
    }

    @Test
    public void writesHeaderAndRowsWithCrLf() throws Exception {
        StringWriter w = new StringWriter();
        List<List<Object>> rows = Collections.singletonList(Arrays.<Object>asList("Finance", 4, new BigDecimal("10.00")));
        CsvWriter.write(w, Arrays.asList("Department", "Headcount", "Gross"), rows);
        assertEquals("Department,Headcount,Gross\r\nFinance,4,10.00\r\n", w.toString());
    }
}
