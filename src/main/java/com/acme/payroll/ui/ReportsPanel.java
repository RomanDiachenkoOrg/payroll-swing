package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.DepartmentCost;
import com.acme.payroll.ui.model.DepartmentCostTableModel;
import com.acme.payroll.ui.util.DateField;
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
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Payroll cost by department for a period. */
public class ReportsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final DateField from = new DateField("reports.from");
    private final DateField to = new DateField("reports.to");
    private final DepartmentCostTableModel model = new DepartmentCostTableModel();
    private final JTable table = new JTable(model);
    private final JLabel summary = new JLabel(" ");
    private final JButton runButton = UiUtil.button("Run report", "reports.run");
    private final JButton exportButton = UiUtil.button("Export CSV...", "reports.exportCsv");
    private List<DepartmentCost> lastResult = Collections.emptyList();
    private boolean initialised;

    public ReportsPanel(AppContext ctx) {
        super(new BorderLayout(4, 4));
        this.ctx = ctx;
        setName("reportsPanel");
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        table.setName("reports.table");
        summary.setName("reports.summary");
        UiUtil.installMoneyRenderer(table);

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(UiUtil.heading("Payroll cost by department"));
        north.add(UiUtil.label("From:", from));
        north.add(from);
        north.add(UiUtil.label("To:", to));
        north.add(to);
        north.add(runButton);
        north.add(exportButton);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(summary, BorderLayout.SOUTH);

        runButton.addActionListener(e -> runReport());
        exportButton.addActionListener(e -> export());
        exportButton.setEnabled(false);
    }

    @Override
    public void refresh() {
        if (!initialised) {
            LocalDate now = LocalDate.now();
            from.setDate(now.withDayOfYear(1));
            to.setDate(now.withDayOfMonth(now.lengthOfMonth()));
            initialised = true;
        }
        runReport();
    }

    private void runReport() {
        try {
            lastResult = ctx.getReportController().costByDepartment(from.getDate(), to.getDate());
            List<DepartmentCost> rows = new ArrayList<DepartmentCost>(lastResult);
            DepartmentCost total = ctx.getReportController().totals(lastResult);
            if (!rows.isEmpty()) {
                rows.add(total);
            }
            model.setRows(rows);
            summary.setText(rows.isEmpty() ? "No approved or paid pay runs in this period."
                    : "Total employer gross cost: " + Formats.money(total.getGross()));
            exportButton.setEnabled(!lastResult.isEmpty());
        } catch (RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
    }

    private void export() {
        File f = UiUtil.chooseCsvFile(this, "payroll-cost-" + from.getText() + "-to-" + to.getText() + ".csv");
        if (f == null) {
            return;
        }
        try {
            ctx.getReportController().exportCostReport(f, lastResult, from.getDate(), to.getDate());
            UiUtil.info(this, "Report exported to " + f.getAbsolutePath());
        } catch (IOException | RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
    }
}
