package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.User;
import com.acme.payroll.service.Session;
import com.acme.payroll.ui.model.PayslipTableModel;
import com.acme.payroll.ui.util.Formats;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Employee self-service: own profile and released payslips. */
public class MyProfilePanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final JPanel profile = new JPanel(new GridBagLayout());
    private final PayslipTableModel model = new PayslipTableModel(true);
    private final JTable table = new JTable(model);

    public MyProfilePanel(AppContext ctx) {
        super(new BorderLayout(8, 8));
        this.ctx = ctx;
        setName("myProfilePanel");
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        profile.setName("myProfile.details");
        table.setName("myProfile.payslips");
        UiUtil.installMoneyRenderer(table);

        JButton open = UiUtil.button("Open payslip...", "myProfile.openPayslip");
        open.addActionListener(e -> openPayslip());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openPayslip();
                }
            }
        });

        JPanel slips = new JPanel(new BorderLayout());
        slips.add(new JLabel("My payslips"), BorderLayout.NORTH);
        slips.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(open);
        slips.add(buttons, BorderLayout.SOUTH);

        add(profile, BorderLayout.NORTH);
        add(slips, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        profile.removeAll();
        User u = Session.getUser();
        if (u == null || u.getEmployeeId() == null) {
            profile.add(new JLabel("No employee record is linked to this account."));
            return;
        }
        Employee e = ctx.getEmployeeController().find(u.getEmployeeId());
        int row = 0;
        row = add(row, "Name", e.getFullName());
        row = add(row, "Employee no.", e.getEmployeeNo());
        row = add(row, "Department", e.getDepartmentName());
        row = add(row, "Job title", e.getJobTitle());
        row = add(row, "Email", e.getEmail());
        add(row, "Hire date", Formats.date(e.getHireDate()));
        profile.revalidate();
        model.setRows(ctx.getEmployeeController().payslips(e.getId()));
    }

    private int add(int row, String label, String value) {
        JLabel v = new JLabel(value);
        JLabel l = new JLabel(label + ":");
        l.setLabelFor(v);
        profile.add(l, UiUtil.gbc(0, row));
        profile.add(v, UiUtil.gbc(1, row));
        return row + 1;
    }

    private void openPayslip() {
        Payslip p = model.getRow(table.getSelectedRow());
        if (p != null) {
            new PayslipDialog(this, ctx, p).setVisible(true);
        }
    }
}
