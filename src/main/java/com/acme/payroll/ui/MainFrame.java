package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.Role;
import com.acme.payroll.model.User;
import com.acme.payroll.service.Session;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Main application window. Tabs and menu entries depend on the role of the signed-in user. */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final Runnable onLogout;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel statusBar = new JLabel();

    public MainFrame(AppContext ctx, Runnable onLogout) {
        super("Acme Payroll");
        this.ctx = ctx;
        this.onLogout = onLogout;
        setName("mainFrame");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        User user = Session.getUser();
        tabs.setName("main.tabs");
        buildTabs(user.getRole());
        tabs.addChangeListener(e -> refreshCurrent());

        statusBar.setName("main.statusBar");
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        statusBar.setText("Signed in as " + user.getEmail() + " (" + user.getRole().getDisplayName() + ")");

        setJMenuBar(buildMenu(user.getRole()));
        getContentPane().add(buildToolBar(user.getRole()), BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(900, 600));
        setSize(1150, 720);
        setLocationRelativeTo(null);
        refreshCurrent();
    }

    private void buildTabs(Role role) {
        switch (role) {
            case ADMIN:
                addTab("Dashboard", new DashboardPanel(ctx));
                addTab("Employees", new EmployeesPanel(ctx));
                addTab("Timesheets", new TimesheetsPanel(ctx));
                addTab("Pay runs", new PayRunsPanel(ctx));
                addTab("Departments", new DepartmentsPanel(ctx));
                addTab("Reports", new ReportsPanel(ctx));
                addTab("Admin", new AdminPanel(ctx));
                break;
            case MANAGER:
                addTab("Dashboard", new DashboardPanel(ctx));
                addTab("Employees", new EmployeesPanel(ctx));
                addTab("Timesheets", new TimesheetsPanel(ctx));
                addTab("Reports", new ReportsPanel(ctx));
                break;
            default:
                addTab("My profile", new MyProfilePanel(ctx));
                addTab("Timesheets", new TimesheetsPanel(ctx));
                break;
        }
    }

    private void addTab(String title, JComponent panel) {
        tabs.addTab(title, panel);
    }

    private JMenuBar buildMenu(Role role) {
        JMenuBar bar = new JMenuBar();
        bar.setName("main.menuBar");

        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        file.setName("menu.file");
        file.add(item("Refresh", "menu.file.refresh", KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), e -> refreshCurrent()));
        file.addSeparator();
        file.add(item("Sign out", "menu.file.logout", null, e -> logout()));
        file.add(item("Exit", "menu.file.exit", null, e -> exit()));
        bar.add(file);

        JMenu view = new JMenu("View");
        view.setMnemonic(KeyEvent.VK_V);
        view.setName("menu.view");
        for (int i = 0; i < tabs.getTabCount(); i++) {
            final int index = i;
            String title = tabs.getTitleAt(i);
            KeyStroke ks = i < 9 ? KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, ActionEvent.CTRL_MASK) : null;
            view.add(item(title, "menu.view." + title.toLowerCase().replace(" ", ""), ks, e -> tabs.setSelectedIndex(index)));
        }
        bar.add(view);

        if (role == Role.ADMIN) {
            JMenu payroll = new JMenu("Payroll");
            payroll.setMnemonic(KeyEvent.VK_P);
            payroll.setName("menu.payroll");
            payroll.add(item("New employee...", "menu.payroll.newEmployee", null, e -> newEmployee()));
            payroll.add(item("Pay runs", "menu.payroll.payRuns", null, e -> selectTab("Pay runs")));
            payroll.add(item("Cost report", "menu.payroll.report", null, e -> selectTab("Reports")));
            bar.add(payroll);
        }

        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        help.setName("menu.help");
        help.add(item("About", "menu.help.about", null, e -> JOptionPane.showMessageDialog(this,
                "Acme Payroll Desktop\nVersion " + version() + "\nJava " + System.getProperty("java.version"),
                "About", JOptionPane.INFORMATION_MESSAGE)));
        bar.add(help);
        return bar;
    }

    private JToolBar buildToolBar(Role role) {
        JToolBar tb = new JToolBar();
        tb.setName("main.toolBar");
        tb.setFloatable(false);
        JButton refresh = UiUtil.button("Refresh", "toolbar.refresh");
        refresh.addActionListener(e -> refreshCurrent());
        tb.add(refresh);
        if (role == Role.ADMIN) {
            JButton newEmp = UiUtil.button("New employee", "toolbar.newEmployee");
            newEmp.addActionListener(e -> newEmployee());
            tb.add(newEmp);
            JButton runs = UiUtil.button("Pay runs", "toolbar.payRuns");
            runs.addActionListener(e -> selectTab("Pay runs"));
            tb.add(runs);
        }
        JButton timesheets = UiUtil.button("Timesheets", "toolbar.timesheets");
        timesheets.addActionListener(e -> selectTab("Timesheets"));
        tb.add(timesheets);
        tb.addSeparator();
        JButton logout = UiUtil.button("Sign out", "toolbar.logout");
        logout.addActionListener(e -> logout());
        tb.add(logout);
        return tb;
    }

    private static JMenuItem item(String text, String name, KeyStroke accelerator,
                                  java.awt.event.ActionListener listener) {
        JMenuItem mi = new JMenuItem(text);
        mi.setName(name);
        if (accelerator != null) {
            mi.setAccelerator(accelerator);
        }
        mi.addActionListener(listener);
        return mi;
    }

    private void selectTab(String title) {
        int i = tabs.indexOfTab(title);
        if (i >= 0) {
            tabs.setSelectedIndex(i);
        }
    }

    private void newEmployee() {
        EmployeeDialog d = new EmployeeDialog(this.getRootPane(), ctx, null);
        d.setVisible(true);
        if (d.isSaved()) {
            refreshCurrent();
        }
    }

    private void refreshCurrent() {
        Component c = tabs.getSelectedComponent();
        if (c instanceof Refreshable) {
            try {
                ((Refreshable) c).refresh();
            } catch (RuntimeException ex) {
                UiUtil.showError(this, ex);
            }
        }
    }

    private void logout() {
        if (!UiUtil.confirm(this, "Sign out?", "Sign out")) {
            return;
        }
        ctx.getAuthService().logout();
        dispose();
        onLogout.run();
    }

    private void exit() {
        if (UiUtil.confirm(this, "Exit Acme Payroll?", "Exit")) {
            ctx.getAuthService().logout();
            dispose();
            ctx.getDatabase().shutdown();
            System.exit(0);
        }
    }

    private static String version() {
        String v = MainFrame.class.getPackage().getImplementationVersion();
        return v == null ? "dev" : v;
    }
}
