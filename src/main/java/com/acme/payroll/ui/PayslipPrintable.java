package com.acme.payroll.ui;

import com.acme.payroll.model.Payslip;
import com.acme.payroll.ui.util.Formats;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.math.BigDecimal;

/** Renders one payslip on a single printed page. */
public class PayslipPrintable implements Printable {

    private final Payslip payslip;
    private final String companyName;

    public PayslipPrintable(Payslip payslip, String companyName) {
        this.payslip = payslip;
        this.companyName = companyName;
    }

    @Override
    public int print(Graphics graphics, PageFormat pf, int pageIndex) {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g = (Graphics2D) graphics;
        g.translate(pf.getImageableX(), pf.getImageableY());
        int width = (int) pf.getImageableWidth();
        int y = 20;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString(companyName + " - Payslip", 0, y);
        y += 28;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        y = line(g, width, y, "Employee", payslip.getEmployeeName() + " (" + payslip.getEmployeeNo() + ")");
        y = line(g, width, y, "Department", payslip.getDepartmentName());
        y = line(g, width, y, "Period", Formats.date(payslip.getPeriodStart()) + " - "
                + Formats.date(payslip.getPeriodEnd()));
        y = line(g, width, y, "Pay date", Formats.date(payslip.getPayDate()));
        y += 10;
        g.drawLine(0, y, width, y);
        y += 16;

        if (payslip.getRegularHours().signum() > 0) {
            y = line(g, width, y, "Regular hours", Formats.number(payslip.getRegularHours()));
            y = line(g, width, y, "Overtime hours", Formats.number(payslip.getOvertimeHours()));
        }
        y = money(g, width, y, "Gross pay", payslip.getGross());
        y = money(g, width, y, "Income tax", payslip.getIncomeTax().negate());
        y = money(g, width, y, "Social contribution", payslip.getSocial().negate());
        y = money(g, width, y, "Pension", payslip.getPension().negate());
        y = money(g, width, y, "Other deductions", payslip.getOtherDeductions().negate());
        y += 4;
        g.drawLine(width / 2, y, width, y);
        y += 16;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        money(g, width, y, "Net pay", payslip.getNet());
        return PAGE_EXISTS;
    }

    private static int line(Graphics g, int width, int y, String label, String value) {
        g.drawString(label + ":", 0, y);
        g.drawString(value == null ? "" : value, 140, y);
        return y + 16;
    }

    private static int money(Graphics g, int width, int y, String label, BigDecimal value) {
        g.drawString(label, 0, y);
        String text = Formats.money(value);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, width - fm.stringWidth(text), y);
        return y + 16;
    }
}
