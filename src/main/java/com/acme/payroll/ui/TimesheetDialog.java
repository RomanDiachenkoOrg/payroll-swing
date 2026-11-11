package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.service.ValidationException;
import com.acme.payroll.ui.util.DateField;
import com.acme.payroll.ui.util.Formats;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/** Weekly timesheet entry: one spinner per day (0-24 h in half-hour steps). */
public class TimesheetDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final Timesheet timesheet;
    private final JComboBox<Employee> employee = new JComboBox<Employee>();
    private final DateField weekStart = new DateField("timesheet.weekStart");
    private final JSpinner[] days = new JSpinner[7];
    private final JLabel total = new JLabel("0");
    private boolean saved;

    public TimesheetDialog(Component parent, AppContext ctx, Timesheet existing) {
        super(SwingUtilities.getWindowAncestor(parent), existing == null ? "New timesheet" : "Edit timesheet",
                ModalityType.APPLICATION_MODAL);
        this.ctx = ctx;
        this.timesheet = existing == null ? new Timesheet() : existing;
        setName("timesheetDialog");

        employee.setName("timesheet.employee");
        total.setName("timesheet.total");
        for (Employee e : ctx.getTimesheetController().selectableEmployees()) {
            employee.addItem(e);
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        form.add(UiUtil.label("Employee:", employee), UiUtil.gbc(0, 0));
        GridBagConstraints c = UiUtil.gbc(1, 0);
        c.gridwidth = 7;
        form.add(employee, c);
        form.add(UiUtil.label("Week starting (Monday):", weekStart), UiUtil.gbc(0, 1));
        c = UiUtil.gbc(1, 1);
        c.gridwidth = 3;
        form.add(weekStart, c);

        for (int i = 0; i < 7; i++) {
            days[i] = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 24.0, 0.5));
            days[i].setName("timesheet.hours." + Timesheet.DAY_NAMES[i].toLowerCase());
            days[i].addChangeListener(e -> updateTotal());
            JLabel l = new JLabel(Timesheet.DAY_NAMES[i]);
            l.setLabelFor(days[i]);
            GridBagConstraints lc = UiUtil.gbc(i + 1, 2);
            lc.anchor = GridBagConstraints.CENTER;
            lc.fill = GridBagConstraints.NONE;
            form.add(l, lc);
            form.add(days[i], UiUtil.gbc(i + 1, 3));
        }
        form.add(new JLabel("Hours:"), UiUtil.gbc(0, 3));
        form.add(UiUtil.label("Total:", total), UiUtil.gbc(0, 4));
        form.add(total, UiUtil.gbc(1, 4));

        JButton save = UiUtil.button("Save draft", "timesheet.save");
        JButton saveSubmit = UiUtil.button("Save and submit", "timesheet.saveSubmit");
        JButton cancel = UiUtil.button("Cancel", "timesheet.cancel");
        save.addActionListener(e -> save(false));
        saveSubmit.addActionListener(e -> save(true));
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(save);
        buttons.add(saveSubmit);
        buttons.add(cancel);

        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        bind();
        pack();
        setLocationRelativeTo(parent);
    }

    private void bind() {
        if (timesheet.getId() == null) {
            weekStart.setDate(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        } else {
            weekStart.setDate(timesheet.getWeekStart());
            for (int i = 0; i < employee.getItemCount(); i++) {
                if (employee.getItemAt(i).getId().equals(timesheet.getEmployeeId())) {
                    employee.setSelectedIndex(i);
                }
            }
            employee.setEnabled(false);
        }
        for (int i = 0; i < 7; i++) {
            days[i].setValue(timesheet.getHours(i).doubleValue());
        }
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (JSpinner s : days) {
            sum = sum.add(BigDecimal.valueOf(((Number) s.getValue()).doubleValue()));
        }
        total.setText(Formats.number(sum) + " h");
    }

    private void save(boolean submit) {
        try {
            Employee e = (Employee) employee.getSelectedItem();
            timesheet.setEmployeeId(e == null ? null : e.getId());
            timesheet.setWeekStart(weekStart.getDate());
            for (int i = 0; i < 7; i++) {
                timesheet.setHours(i, BigDecimal.valueOf(((Number) days[i].getValue()).doubleValue()));
            }
            ctx.getTimesheetController().save(timesheet);
            saved = true;
            if (submit) {
                ctx.getTimesheetController().submit(timesheet.getId());
            }
            dispose();
        } catch (ValidationException | IllegalArgumentException ex) {
            UiUtil.showError(this, ex);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
