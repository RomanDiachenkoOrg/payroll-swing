package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.Department;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;
import com.acme.payroll.service.ValidationException;
import com.acme.payroll.ui.util.DateField;
import com.acme.payroll.ui.util.Formats;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Create / edit employee form. */
public class EmployeeDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final Employee employee;
    private boolean saved;

    private final JTextField firstName = field("employee.firstName");
    private final JTextField lastName = field("employee.lastName");
    private final JTextField email = field("employee.email");
    private final JTextField phone = field("employee.phone");
    private final JComboBox<Department> department = new JComboBox<Department>();
    private final JTextField jobTitle = field("employee.jobTitle");
    private final JComboBox<PayType> payType = new JComboBox<PayType>(PayType.values());
    private final JTextField annualSalary = field("employee.annualSalary");
    private final JTextField hourlyRate = field("employee.hourlyRate");
    private final DateField hireDate = new DateField("employee.hireDate");
    private final JCheckBox pension = new JCheckBox("Opted in to company pension");
    private final JCheckBox active = new JCheckBox("Active");
    private final JTextArea errors = new JTextArea(3, 30);

    public EmployeeDialog(Component parent, AppContext ctx, Employee existing) {
        super(SwingUtilities.getWindowAncestor(parent), existing == null ? "New employee" : "Edit employee",
                ModalityType.APPLICATION_MODAL);
        this.ctx = ctx;
        this.employee = existing == null ? new Employee() : existing;
        setName("employeeDialog");

        department.setName("employee.department");
        payType.setName("employee.payType");
        pension.setName("employee.pension");
        active.setName("employee.active");
        errors.setName("employee.errors");
        errors.setEditable(false);
        errors.setForeground(new Color(180, 30, 30));
        errors.setOpaque(false);
        errors.setLineWrap(true);
        errors.setWrapStyleWord(true);

        for (Department d : ctx.getDepartmentController().list()) {
            department.addItem(d);
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        int row = 0;
        row = addRow(form, row, "First name *", firstName);
        row = addRow(form, row, "Last name *", lastName);
        row = addRow(form, row, "Email *", email);
        row = addRow(form, row, "Phone", phone);
        row = addRow(form, row, "Department *", department);
        row = addRow(form, row, "Job title", jobTitle);
        row = addRow(form, row, "Pay type *", payType);
        row = addRow(form, row, "Annual salary (USD)", annualSalary);
        row = addRow(form, row, "Hourly rate (USD)", hourlyRate);
        row = addRow(form, row, "Hire date * (yyyy-mm-dd)", hireDate);
        GridBagConstraints c = UiUtil.gbc(1, row++);
        form.add(pension, c);
        c = UiUtil.gbc(1, row++);
        form.add(active, c);
        c = UiUtil.gbc(0, row);
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(errors, c);

        JButton save = UiUtil.button("Save", "employee.save");
        JButton cancel = UiUtil.button("Cancel", "employee.cancel");
        save.addActionListener(e -> save());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(save);
        buttons.add(cancel);

        payType.addActionListener(e -> updateRateFields());

        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);

        bind();
        pack();
        setLocationRelativeTo(parent instanceof Window ? parent : SwingUtilities.getWindowAncestor(parent));
    }

    private static JTextField field(String name) {
        JTextField f = new JTextField(22);
        f.setName(name);
        return f;
    }

    private static int addRow(JPanel form, int row, String label, java.awt.Component field) {
        JLabel l = new JLabel(label + ":");
        l.setLabelFor(field);
        form.add(l, UiUtil.gbc(0, row));
        form.add(field, UiUtil.gbc(1, row));
        return row + 1;
    }

    private void bind() {
        Employee e = employee;
        firstName.setText(e.getFirstName());
        lastName.setText(e.getLastName());
        email.setText(e.getEmail());
        phone.setText(e.getPhone());
        if (e.getDepartmentId() != null) {
            for (int i = 0; i < department.getItemCount(); i++) {
                if (e.getDepartmentId().equals(department.getItemAt(i).getId())) {
                    department.setSelectedIndex(i);
                }
            }
        } else {
            department.setSelectedIndex(-1);
        }
        jobTitle.setText(e.getJobTitle());
        payType.setSelectedItem(e.getPayType());
        annualSalary.setText(e.getAnnualSalary() == null ? "" : e.getAnnualSalary().toPlainString());
        hourlyRate.setText(e.getHourlyRate() == null ? "" : e.getHourlyRate().toPlainString());
        hireDate.setDate(e.getId() == null ? LocalDate.now() : e.getHireDate());
        pension.setSelected(e.isPensionOptIn());
        active.setSelected(e.isActive());
        active.setEnabled(e.getId() != null);
        updateRateFields();
    }

    private void updateRateFields() {
        boolean hourly = payType.getSelectedItem() == PayType.HOURLY;
        hourlyRate.setEnabled(hourly);
        annualSalary.setEnabled(!hourly);
    }

    private void save() {
        List<String> parseErrors = new ArrayList<String>();
        Employee e = employee;
        e.setFirstName(firstName.getText());
        e.setLastName(lastName.getText());
        e.setEmail(email.getText());
        e.setPhone(phone.getText());
        Department d = (Department) department.getSelectedItem();
        e.setDepartmentId(d == null ? null : d.getId());
        e.setJobTitle(jobTitle.getText());
        e.setPayType((PayType) payType.getSelectedItem());
        try {
            e.setAnnualSalary(Formats.parseDecimal(annualSalary.getText()));
        } catch (IllegalArgumentException ex) {
            parseErrors.add("Annual salary: " + ex.getMessage());
        }
        try {
            e.setHourlyRate(Formats.parseDecimal(hourlyRate.getText()));
        } catch (IllegalArgumentException ex) {
            parseErrors.add("Hourly rate: " + ex.getMessage());
        }
        try {
            e.setHireDate(hireDate.getDate());
        } catch (IllegalArgumentException ex) {
            parseErrors.add("Hire date: " + ex.getMessage());
        }
        e.setPensionOptIn(pension.isSelected());
        e.setActive(active.isSelected());
        if (!parseErrors.isEmpty()) {
            showErrors(parseErrors);
            return;
        }
        try {
            ctx.getEmployeeController().save(e);
            saved = true;
            dispose();
        } catch (ValidationException ex) {
            showErrors(ex.getErrors());
        } catch (RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
    }

    private void showErrors(List<String> list) {
        errors.setText(String.join("\n", list));
        pack();
    }

    public boolean isSaved() {
        return saved;
    }
}
