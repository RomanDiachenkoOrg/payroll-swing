package com.acme.payroll.ui.util;

import javax.swing.JTextField;
import java.time.LocalDate;

/** Text field for ISO dates (yyyy-mm-dd). */
public class DateField extends JTextField {

    private static final long serialVersionUID = 1L;

    public DateField(String name) {
        super(10);
        setName(name);
        setToolTipText("Date in the format yyyy-mm-dd");
    }

    public LocalDate getDate() {
        return Formats.parseDate(getText());
    }

    public void setDate(LocalDate date) {
        setText(Formats.date(date));
    }
}
