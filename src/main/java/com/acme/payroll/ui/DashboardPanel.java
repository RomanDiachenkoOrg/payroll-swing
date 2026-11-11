package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.controller.DashboardController;
import com.acme.payroll.model.DepartmentCost;
import com.acme.payroll.ui.model.PayRunTableModel;
import com.acme.payroll.ui.util.BarChartPanel;
import com.acme.payroll.ui.util.Formats;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final JLabel headcount = valueLabel("dashboard.headcount");
    private final JLabel lastPayroll = valueLabel("dashboard.lastPayroll");
    private final JLabel pending = valueLabel("dashboard.pendingTimesheets");
    private final JLabel nextPayDate = valueLabel("dashboard.nextPayDate");
    private final JLabel chartTitle = new JLabel("Gross pay by department (last payroll)");
    private final BarChartPanel chart = new BarChartPanel();
    private final PayRunTableModel runsModel = new PayRunTableModel();

    public DashboardPanel(AppContext ctx) {
        super(new BorderLayout(8, 8));
        this.ctx = ctx;
        setName("dashboardPanel");
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel tiles = new JPanel(new GridLayout(1, 4, 12, 0));
        tiles.add(tile("Active employees", headcount));
        tiles.add(tile("Last payroll (gross)", lastPayroll));
        tiles.add(tile("Timesheets awaiting approval", pending));
        tiles.add(tile("Next pay date", nextPayDate));

        chart.setName("dashboard.chart");
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(chartTitle, BorderLayout.NORTH);
        chartPanel.add(chart, BorderLayout.CENTER);

        JTable runs = new JTable(runsModel);
        runs.setName("dashboard.recentRuns");
        UiUtil.installMoneyRenderer(runs);
        JScrollPane runsScroll = new JScrollPane(runs);
        runsScroll.setPreferredSize(new Dimension(500, 160));
        JPanel runsPanel = new JPanel(new BorderLayout());
        runsPanel.add(new JLabel("Recent pay runs"), BorderLayout.NORTH);
        runsPanel.add(runsScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 0));
        center.add(chartPanel);
        center.add(runsPanel);

        add(tiles, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private static JLabel valueLabel(String name) {
        JLabel l = new JLabel("-");
        l.setName(name);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 20f));
        return l;
    }

    private static JPanel tile(String caption, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        p.add(new JLabel(caption), BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void refresh() {
        DashboardController.Stats s = ctx.getDashboardController().load();
        headcount.setText(String.valueOf(s.headcount));
        lastPayroll.setText(Formats.money(s.lastPayrollTotal));
        lastPayroll.setToolTipText(s.lastPayrollLabel);
        pending.setText(String.valueOf(s.pendingTimesheets));
        nextPayDate.setText(Formats.date(s.nextPayDate));
        chartTitle.setText("Gross pay by department (" + s.lastPayrollLabel + ")");
        List<String> labels = new ArrayList<String>();
        List<BigDecimal> values = new ArrayList<BigDecimal>();
        for (DepartmentCost c : s.lastRunByDepartment) {
            labels.add(c.getDepartmentName());
            values.add(c.getGross());
        }
        chart.setData(labels, values);
        runsModel.setRows(s.recentRuns);
    }
}
