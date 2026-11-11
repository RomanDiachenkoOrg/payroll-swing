package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.dao.SettingsDao;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.ui.util.Formats;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/** Payslip breakdown with print and CSV export. */
public class PayslipDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final Payslip payslip;
    private final JPanel body = new JPanel(new GridBagLayout());
    private int row;

    public PayslipDialog(Component parent, AppContext ctx, Payslip payslip) {
        super(parent instanceof java.awt.Window ? (java.awt.Window) parent : SwingUtilities.getWindowAncestor(parent),
                "Payslip", ModalityType.APPLICATION_MODAL);
        this.ctx = ctx;
        this.payslip = payslip;
        setName("payslipDialog");

        body.setName("payslip.body");
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel title = UiUtil.heading("Payslip - " + payslip.getEmployeeName());
        title.setName("payslip.title");

        text("Employee no.", payslip.getEmployeeNo());
        text("Department", payslip.getDepartmentName());
        text("Period", Formats.date(payslip.getPeriodStart()) + " - " + Formats.date(payslip.getPeriodEnd()));
        text("Pay date", Formats.date(payslip.getPayDate()));
        text("Pay run status", payslip.getRunStatus() == null ? "" : payslip.getRunStatus().name());
        if (payslip.getRegularHours().signum() > 0) {
            text("Regular hours", Formats.number(payslip.getRegularHours()));
            text("Overtime hours", Formats.number(payslip.getOvertimeHours()));
        }
        amount("Gross pay", payslip.getGross(), "payslip.gross", false);
        amount("Income tax", payslip.getIncomeTax().negate(), "payslip.tax", false);
        amount("Social contribution", payslip.getSocial().negate(), "payslip.social", false);
        amount("Pension", payslip.getPension().negate(), "payslip.pension", false);
        amount("Other deductions", payslip.getOtherDeductions().negate(), "payslip.other", false);
        amount("Net pay", payslip.getNet(), "payslip.net", true);

        JButton print = UiUtil.button("Print...", "payslip.print");
        JButton export = UiUtil.button("Export CSV...", "payslip.exportCsv");
        JButton close = UiUtil.button("Close", "payslip.close");
        print.addActionListener(e -> print());
        export.addActionListener(e -> exportCsv());
        close.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(print);
        buttons.add(export);
        buttons.add(close);

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(title);
        getContentPane().add(north, BorderLayout.NORTH);
        getContentPane().add(body, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    private void text(String label, String value) {
        JLabel v = new JLabel(value);
        JLabel l = new JLabel(label + ":");
        l.setLabelFor(v);
        body.add(l, UiUtil.gbc(0, row));
        body.add(v, UiUtil.gbc(1, row));
        row++;
    }

    private void amount(String label, BigDecimal value, String name, boolean bold) {
        JLabel v = new JLabel(Formats.money(value), SwingConstants.RIGHT);
        v.setName(name);
        JLabel l = new JLabel(label + ":");
        l.setLabelFor(v);
        if (bold) {
            v.setFont(v.getFont().deriveFont(Font.BOLD));
            l.setFont(l.getFont().deriveFont(Font.BOLD));
        }
        body.add(l, UiUtil.gbc(0, row));
        body.add(v, UiUtil.gbc(1, row));
        row++;
    }

    private void print() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Payslip " + payslip.getEmployeeNo() + " " + payslip.getPeriodStart());
        String company = ctx.getSettingsDao().get(SettingsDao.COMPANY_NAME, "Acme Corporation");
        job.setPrintable(new PayslipPrintable(payslip, company));
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                UiUtil.showError(this, ex);
            }
        }
    }

    private void exportCsv() {
        File f = UiUtil.chooseCsvFile(this, "payslip-" + payslip.getEmployeeNo() + "-"
                + Formats.date(payslip.getPeriodStart()) + ".csv");
        if (f == null) {
            return;
        }
        try {
            ctx.getReportController().exportPayslip(f, payslip);
            UiUtil.info(this, "Payslip exported to " + f.getAbsolutePath());
        } catch (IOException ex) {
            UiUtil.showError(this, ex);
        }
    }
}
