package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.model.TimesheetStatus;
import com.acme.payroll.ui.model.TimesheetTableModel;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TimesheetsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final String ALL = "All statuses";

    private final AppContext ctx;
    private final TimesheetTableModel model = new TimesheetTableModel();
    private final JTable table = new JTable(model);
    private final JComboBox<Object> statusFilter = new JComboBox<Object>();
    private final JButton newButton = UiUtil.button("New...", "timesheets.new");
    private final JButton editButton = UiUtil.button("Edit...", "timesheets.edit");
    private final JButton submitButton = UiUtil.button("Submit", "timesheets.submit");
    private final JButton approveButton = UiUtil.button("Approve", "timesheets.approve");
    private final JButton rejectButton = UiUtil.button("Reject...", "timesheets.reject");

    public TimesheetsPanel(AppContext ctx) {
        super(new BorderLayout(4, 4));
        this.ctx = ctx;
        setName("timesheetsPanel");
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        statusFilter.setName("timesheets.statusFilter");
        statusFilter.addItem(ALL);
        for (TimesheetStatus s : TimesheetStatus.values()) {
            statusFilter.addItem(s);
        }
        table.setName("timesheets.table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(UiUtil.label("Status:", statusFilter));
        north.add(statusFilter);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(newButton);
        south.add(editButton);
        south.add(submitButton);
        boolean canDecide = ctx.getTimesheetController().canDecide();
        if (canDecide) {
            south.add(approveButton);
            south.add(rejectButton);
        }

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        statusFilter.addActionListener(e -> refresh());
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && editButton.isEnabled()) {
                    edit();
                }
            }
        });
        newButton.addActionListener(e -> openDialog(null));
        editButton.addActionListener(e -> edit());
        submitButton.addActionListener(e -> run(() -> ctx.getTimesheetController().submit(selected().getId())));
        approveButton.addActionListener(e -> run(() -> ctx.getTimesheetController().approve(selected().getId())));
        rejectButton.addActionListener(e -> reject());
    }

    @Override
    public void refresh() {
        Object s = statusFilter.getSelectedItem();
        model.setRows(ctx.getTimesheetController().list(s instanceof TimesheetStatus ? (TimesheetStatus) s : null));
        updateButtons();
    }

    private Timesheet selected() {
        return model.getRow(table.getSelectedRow());
    }

    private void updateButtons() {
        Timesheet t = selected();
        boolean editable = t != null && t.getStatus().isEditable();
        boolean submitted = t != null && t.getStatus() == TimesheetStatus.SUBMITTED;
        editButton.setEnabled(editable);
        submitButton.setEnabled(editable);
        approveButton.setEnabled(submitted);
        rejectButton.setEnabled(submitted);
    }

    private void edit() {
        Timesheet t = selected();
        if (t != null) {
            openDialog(ctx.getTimesheetDao().findById(t.getId()));
        }
    }

    private void openDialog(Timesheet t) {
        TimesheetDialog d = new TimesheetDialog(this, ctx, t);
        d.setVisible(true);
        if (d.isSaved()) {
            refresh();
        }
    }

    private void reject() {
        Timesheet t = selected();
        if (t == null) {
            return;
        }
        String reason = JOptionPane.showInputDialog(this, "Reason for rejecting the timesheet of "
                + t.getEmployeeName() + ":", "Reject timesheet", JOptionPane.QUESTION_MESSAGE);
        if (reason == null) {
            return;
        }
        run(() -> ctx.getTimesheetController().reject(t.getId(), reason));
    }

    private void run(Runnable action) {
        if (selected() == null) {
            return;
        }
        try {
            action.run();
            refresh();
        } catch (RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
    }
}
