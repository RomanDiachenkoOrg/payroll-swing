package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.PayRun;
import com.acme.payroll.model.PayRunStatus;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.ui.model.PayRunTableModel;
import com.acme.payroll.ui.model.PayslipTableModel;
import com.acme.payroll.ui.util.DateField;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class PayRunsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final PayRunTableModel runModel = new PayRunTableModel();
    private final JTable runTable = new JTable(runModel);
    private final PayslipTableModel lineModel = new PayslipTableModel(false);
    private final JTable lineTable = new JTable(lineModel);
    private final JLabel linesLabel = new JLabel("Select a pay run to review its lines");

    private final JButton newButton = UiUtil.button("New pay run...", "payruns.new");
    private final JButton calculateButton = UiUtil.button("Calculate", "payruns.calculate");
    private final JButton approveButton = UiUtil.button("Approve", "payruns.approve");
    private final JButton paidButton = UiUtil.button("Mark paid", "payruns.markPaid");
    private final JButton deleteButton = UiUtil.button("Delete", "payruns.delete");
    private final JButton exportButton = UiUtil.button("Export lines CSV...", "payruns.exportCsv");
    private final JButton payslipButton = UiUtil.button("Open payslip...", "payruns.openPayslip");

    public PayRunsPanel(AppContext ctx) {
        super(new BorderLayout(4, 4));
        this.ctx = ctx;
        setName("payRunsPanel");
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        runTable.setName("payruns.table");
        runTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiUtil.installMoneyRenderer(runTable);
        lineTable.setName("payruns.lines");
        lineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiUtil.installMoneyRenderer(lineTable);
        linesLabel.setName("payruns.linesLabel");

        JPanel top = new JPanel(new BorderLayout());
        JPanel runActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        runActions.add(newButton);
        runActions.add(calculateButton);
        runActions.add(approveButton);
        runActions.add(paidButton);
        runActions.add(deleteButton);
        top.add(runActions, BorderLayout.NORTH);
        top.add(new JScrollPane(runTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(linesLabel, BorderLayout.NORTH);
        bottom.add(new JScrollPane(lineTable), BorderLayout.CENTER);
        JPanel lineActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lineActions.add(payslipButton);
        lineActions.add(exportButton);
        bottom.add(lineActions, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setName("payruns.split");
        split.setResizeWeight(0.4);
        add(split, BorderLayout.CENTER);

        runTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadLines();
            }
        });
        lineTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openPayslip();
                }
            }
        });
        newButton.addActionListener(e -> createRun());
        calculateButton.addActionListener(e -> act(id -> {
            int n = ctx.getPayRunController().calculate(id);
            UiUtil.info(this, n + " payslip(s) calculated.");
        }));
        approveButton.addActionListener(e -> {
            if (UiUtil.confirm(this, "Approve this pay run? Payslips become visible to employees.", "Approve")) {
                act(id -> ctx.getPayRunController().approve(id));
            }
        });
        paidButton.addActionListener(e -> {
            if (UiUtil.confirm(this, "Mark this pay run as paid?\nA paid pay run is locked and cannot be changed.",
                    "Mark paid")) {
                act(id -> ctx.getPayRunController().markPaid(id));
            }
        });
        deleteButton.addActionListener(e -> {
            if (UiUtil.confirm(this, "Delete this pay run and its payslips?", "Delete pay run")) {
                act(id -> ctx.getPayRunController().delete(id));
            }
        });
        exportButton.addActionListener(e -> exportLines());
        payslipButton.addActionListener(e -> openPayslip());
        updateButtons();
    }

    private interface RunAction {
        void apply(long id);
    }

    @Override
    public void refresh() {
        PayRun sel = selectedRun();
        runModel.setRows(ctx.getPayRunController().list());
        if (sel != null) {
            for (int i = 0; i < runModel.getRowCount(); i++) {
                if (runModel.getRow(i).getId().equals(sel.getId())) {
                    runTable.setRowSelectionInterval(i, i);
                }
            }
        }
        loadLines();
    }

    private PayRun selectedRun() {
        return runModel.getRow(runTable.getSelectedRow());
    }

    private void loadLines() {
        PayRun r = selectedRun();
        if (r == null) {
            lineModel.setRows(java.util.Collections.<Payslip>emptyList());
            linesLabel.setText("Select a pay run to review its lines");
        } else {
            lineModel.setRows(ctx.getPayRunController().payslips(r.getId()));
            linesLabel.setText("Lines of pay run #" + r.getId() + " - " + r.getPeriodLabel() + " ("
                    + r.getStatus() + (r.isLocked() ? ", locked" : "") + ")");
        }
        updateButtons();
    }

    private void updateButtons() {
        PayRun r = selectedRun();
        PayRunStatus s = r == null ? null : r.getStatus();
        calculateButton.setEnabled(s == PayRunStatus.DRAFT || s == PayRunStatus.CALCULATED);
        approveButton.setEnabled(s == PayRunStatus.CALCULATED);
        paidButton.setEnabled(s == PayRunStatus.CALCULATED || s == PayRunStatus.APPROVED);
        deleteButton.setEnabled(s == PayRunStatus.DRAFT || s == PayRunStatus.CALCULATED);
        exportButton.setEnabled(r != null && lineModel.getRowCount() > 0);
        payslipButton.setEnabled(r != null && lineModel.getRowCount() > 0);
    }

    private void act(RunAction action) {
        PayRun r = selectedRun();
        if (r == null) {
            return;
        }
        try {
            action.apply(r.getId());
        } catch (RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
        refresh();
    }

    private void createRun() {
        LocalDate first = LocalDate.now().withDayOfMonth(1);
        DateField start = new DateField("newPayRun.periodStart");
        DateField end = new DateField("newPayRun.periodEnd");
        DateField pay = new DateField("newPayRun.payDate");
        start.setDate(first.plusMonths(1));
        end.setDate(first.plusMonths(2).minusDays(1));
        pay.setDate(first.plusMonths(2).minusDays(1));
        JPanel p = new JPanel(new GridBagLayout());
        p.add(UiUtil.label("Period start:", start), UiUtil.gbc(0, 0));
        p.add(start, UiUtil.gbc(1, 0));
        p.add(UiUtil.label("Period end:", end), UiUtil.gbc(0, 1));
        p.add(end, UiUtil.gbc(1, 1));
        p.add(UiUtil.label("Pay date:", pay), UiUtil.gbc(0, 2));
        p.add(pay, UiUtil.gbc(1, 2));
        if (JOptionPane.showConfirmDialog(this, p, "New pay run", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            ctx.getPayRunController().create(start.getDate(), end.getDate(), pay.getDate());
            refresh();
        } catch (RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
    }

    private void openPayslip() {
        Payslip p = lineModel.getRow(lineTable.getSelectedRow());
        if (p == null && lineModel.getRowCount() > 0) {
            UiUtil.info(this, "Select a line first.");
            return;
        }
        if (p != null) {
            new PayslipDialog(this, ctx, p).setVisible(true);
        }
    }

    private void exportLines() {
        PayRun r = selectedRun();
        if (r == null) {
            return;
        }
        File f = UiUtil.chooseCsvFile(this, "payrun-" + r.getId() + "-" + r.getPeriodStart() + ".csv");
        if (f == null) {
            return;
        }
        try {
            ctx.getReportController().exportPayslips(f, lineModel.getRows());
            UiUtil.info(this, "Exported " + lineModel.getRowCount() + " lines to " + f.getAbsolutePath());
        } catch (IOException ex) {
            UiUtil.showError(this, ex);
        }
    }
}
