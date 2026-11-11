package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.Role;
import com.acme.payroll.service.Session;
import com.acme.payroll.ui.model.PayslipTableModel;
import com.acme.payroll.ui.model.SalaryHistoryTableModel;
import com.acme.payroll.ui.util.DateField;
import com.acme.payroll.ui.util.Formats;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Read-only employee profile with salary history and payslips. */
public class EmployeeDetailDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final long employeeId;
    private final JPanel profilePanel = new JPanel(new GridBagLayout());
    private final SalaryHistoryTableModel historyModel = new SalaryHistoryTableModel();
    private final PayslipTableModel payslipModel = new PayslipTableModel(true);
    private final JTable payslipTable = new JTable(payslipModel);
    private Employee employee;

    public EmployeeDetailDialog(Component parent, AppContext ctx, long employeeId) {
        super(SwingUtilities.getWindowAncestor(parent), "Employee details", ModalityType.APPLICATION_MODAL);
        this.ctx = ctx;
        this.employeeId = employeeId;
        setName("employeeDetailDialog");

        JPanel content = buildContent(ctx);
        getContentPane().add(content, BorderLayout.CENTER);
        JButton close = UiUtil.button("Close", "employeeDetail.close");
        close.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(close);
        getContentPane().add(south, BorderLayout.SOUTH);

        reload();
        setSize(new Dimension(820, 480));
        setLocationRelativeTo(parent);
    }

    /** Builds the Profile / Salary history / Payslips tabs. */
    private JPanel buildContent(AppContext ctx) {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setName("employeeDetail.tabs");
        profilePanel.setName("employeeDetail.profile");
        profilePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tabs.addTab("Profile", new JScrollPane(profilePanel));

        JTable historyTable = new JTable(historyModel);
        historyTable.setName("employeeDetail.salaryHistory");
        UiUtil.installMoneyRenderer(historyTable);
        JPanel history = new JPanel(new BorderLayout());
        history.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        if (!Session.isLoggedIn() || Session.hasRole(Role.ADMIN)) {
            JButton raise = UiUtil.button("Add raise...", "employeeDetail.addRaise");
            raise.addActionListener(e -> addRaise());
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.add(raise);
            history.add(p, BorderLayout.SOUTH);
        }
        tabs.addTab("Salary history", history);

        payslipTable.setName("employeeDetail.payslips");
        UiUtil.installMoneyRenderer(payslipTable);
        payslipTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openPayslip();
                }
            }
        });
        JButton open = UiUtil.button("Open payslip...", "employeeDetail.openPayslip");
        open.addActionListener(e -> openPayslip());
        JPanel slips = new JPanel(new BorderLayout());
        slips.add(new JScrollPane(payslipTable), BorderLayout.CENTER);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(open);
        slips.add(p, BorderLayout.SOUTH);
        tabs.addTab("Payslips", slips);

        JPanel content = new JPanel(new BorderLayout());
        content.add(tabs, BorderLayout.CENTER);
        return content;
    }

    private void reload() {
        employee = ctx.getEmployeeController().find(employeeId);
        setTitle("Employee - " + employee.getFullName());
        profilePanel.removeAll();
        int row = 0;
        row = info(row, "Employee no.", employee.getEmployeeNo());
        row = info(row, "Name", employee.getFullName());
        row = info(row, "Email", employee.getEmail());
        row = info(row, "Phone", employee.getPhone());
        row = info(row, "Department", employee.getDepartmentName());
        row = info(row, "Job title", employee.getJobTitle());
        row = info(row, "Pay type", employee.getPayType().getLabel());
        row = info(row, employee.isHourly() ? "Hourly rate" : "Annual salary", Formats.money(employee.getCurrentRate()));
        row = info(row, "Hire date", Formats.date(employee.getHireDate()));
        row = info(row, "Pension", employee.isPensionOptIn() ? "Opted in" : "Not enrolled");
        List<String> deductions = ctx.getEmployeeController().deductions(employeeId);
        row = info(row, "Other deductions", deductions.isEmpty() ? "None" : String.join("; ", deductions));
        info(row, "Status", employee.isActive() ? "Active" : "Inactive");
        profilePanel.revalidate();
        profilePanel.repaint();
        historyModel.setRows(ctx.getEmployeeController().salaryHistory(employeeId));
        payslipModel.setRows(ctx.getEmployeeController().payslips(employeeId));
    }

    private int info(int row, String label, String value) {
        JLabel l = new JLabel(label + ":");
        JLabel v = new JLabel(value == null ? "" : value);
        v.setName("employeeDetail." + label.replace(" ", "").replace(".", ""));
        l.setLabelFor(v);
        profilePanel.add(l, UiUtil.gbc(0, row));
        profilePanel.add(v, UiUtil.gbc(1, row));
        return row + 1;
    }

    private void addRaise() {
        DateField effective = new DateField("raise.effectiveDate");
        effective.setDate(LocalDate.now());
        JTextField amount = new JTextField(12);
        amount.setName("raise.amount");
        JTextField reason = new JTextField(20);
        reason.setName("raise.reason");
        JPanel p = new JPanel(new GridBagLayout());
        p.add(UiUtil.label("Effective date:", effective), UiUtil.gbc(0, 0));
        p.add(effective, UiUtil.gbc(1, 0));
        p.add(UiUtil.label(employee.isHourly() ? "New hourly rate:" : "New annual salary:", amount), UiUtil.gbc(0, 1));
        p.add(amount, UiUtil.gbc(1, 1));
        p.add(UiUtil.label("Reason:", reason), UiUtil.gbc(0, 2));
        p.add(reason, UiUtil.gbc(1, 2));
        if (JOptionPane.showConfirmDialog(this, p, "Add raise", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            BigDecimal value = Formats.parseDecimal(amount.getText());
            ctx.getEmployeeController().addRaise(employeeId, effective.getDate(), value, reason.getText());
            reload();
        } catch (RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
    }

    private void openPayslip() {
        Payslip p = payslipModel.getRow(payslipTable.getSelectedRow());
        if (p != null) {
            new PayslipDialog(this, ctx, p).setVisible(true);
        }
    }
}
