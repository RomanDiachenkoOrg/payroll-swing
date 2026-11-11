package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.User;
import com.acme.payroll.ui.model.AuditLogTableModel;
import com.acme.payroll.ui.model.TaxBracketTableModel;
import com.acme.payroll.ui.model.UserTableModel;
import com.acme.payroll.ui.util.DateField;
import com.acme.payroll.ui.util.Formats;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

/** Settings and administration: tax brackets and rates, users, audit log. */
public class AdminPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final String ANY_ACTION = "(any action)";

    private final AppContext ctx;

    private final TaxBracketTableModel bracketModel = new TaxBracketTableModel();
    private final JTable bracketTable = new JTable(bracketModel);
    private final JTextField socialRate = new JTextField(6);
    private final JTextField pensionRate = new JTextField(6);

    private final UserTableModel userModel = new UserTableModel();
    private final JTable userTable = new JTable(userModel);

    private final AuditLogTableModel auditModel = new AuditLogTableModel();
    private final JTable auditTable = new JTable(auditModel);
    private final JTextField auditUser = new JTextField(14);
    private final JComboBox<String> auditAction = new JComboBox<String>();
    private final DateField auditFrom = new DateField("admin.audit.from");
    private final DateField auditTo = new DateField("admin.audit.to");

    public AdminPanel(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setName("adminPanel");
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setName("admin.tabs");
        tabs.addTab("Tax & rates", buildTaxTab());
        tabs.addTab("Users", buildUsersTab());
        tabs.addTab("Audit log", buildAuditTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildTaxTab() {
        bracketTable.setName("admin.taxBrackets");
        socialRate.setName("admin.socialRate");
        pensionRate.setName("admin.pensionRate");

        JPanel rates = new JPanel(new GridBagLayout());
        rates.add(UiUtil.label("Social contribution (% of gross):", socialRate), UiUtil.gbc(0, 0));
        rates.add(socialRate, UiUtil.gbc(1, 0));
        rates.add(UiUtil.label("Pension, if opted in (% of gross):", pensionRate), UiUtil.gbc(0, 1));
        rates.add(pensionRate, UiUtil.gbc(1, 1));

        JButton add = UiUtil.button("Add bracket", "admin.addBracket");
        JButton remove = UiUtil.button("Remove bracket", "admin.removeBracket");
        JButton save = UiUtil.button("Save", "admin.saveTax");
        JButton reset = UiUtil.button("Revert", "admin.revertTax");
        add.addActionListener(e -> bracketModel.addRow());
        remove.addActionListener(e -> bracketModel.removeRow(bracketTable.getSelectedRow()));
        save.addActionListener(e -> saveTax());
        reset.addActionListener(e -> loadTax());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(add);
        buttons.add(remove);
        buttons.add(save);
        buttons.add(reset);

        JPanel south = new JPanel(new BorderLayout());
        south.add(rates, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);

        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.add(UiUtil.heading("Monthly income tax brackets"), BorderLayout.NORTH);
        p.add(new JScrollPane(bracketTable), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildUsersTab() {
        userTable.setName("admin.users");
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JButton toggle = UiUtil.button("Enable / disable", "admin.toggleUser");
        toggle.addActionListener(e -> {
            User u = userModel.getRow(userTable.getSelectedRow());
            if (u == null) {
                return;
            }
            try {
                ctx.getAdminController().setUserActive(u, !u.isActive());
                userModel.setRows(ctx.getAdminController().users());
            } catch (RuntimeException ex) {
                UiUtil.showError(this, ex);
            }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(toggle);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(userTable), BorderLayout.CENTER);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildAuditTab() {
        auditTable.setName("admin.auditLog");
        auditUser.setName("admin.audit.user");
        auditAction.setName("admin.audit.action");
        JButton search = UiUtil.button("Search", "admin.audit.search");
        search.addActionListener(e -> loadAudit());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(UiUtil.label("User:", auditUser));
        filters.add(auditUser);
        filters.add(UiUtil.label("Action:", auditAction));
        filters.add(auditAction);
        filters.add(UiUtil.label("From:", auditFrom));
        filters.add(auditFrom);
        filters.add(UiUtil.label("To:", auditTo));
        filters.add(auditTo);
        filters.add(search);

        JPanel p = new JPanel(new BorderLayout());
        p.add(filters, BorderLayout.NORTH);
        p.add(new JScrollPane(auditTable), BorderLayout.CENTER);
        return p;
    }

    @Override
    public void refresh() {
        loadTax();
        userModel.setRows(ctx.getAdminController().users());
        Object selectedAction = auditAction.getSelectedItem();
        auditAction.removeAllItems();
        auditAction.addItem(ANY_ACTION);
        for (String a : ctx.getAdminController().auditActions()) {
            auditAction.addItem(a);
        }
        if (selectedAction != null) {
            auditAction.setSelectedItem(selectedAction);
        }
        loadAudit();
    }

    private void loadTax() {
        bracketModel.setBrackets(ctx.getAdminController().taxBrackets());
        socialRate.setText(Formats.number(ctx.getAdminController().socialRate()));
        pensionRate.setText(Formats.number(ctx.getAdminController().pensionRate()));
    }

    private void saveTax() {
        if (bracketTable.isEditing()) {
            bracketTable.getCellEditor().stopCellEditing();
        }
        try {
            ctx.getAdminController().saveTaxSettings(bracketModel.toBrackets(),
                    Formats.parseDecimal(socialRate.getText()), Formats.parseDecimal(pensionRate.getText()));
            UiUtil.info(this, "Tax settings saved. They apply to pay runs calculated from now on.");
            loadTax();
        } catch (RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
    }

    private void loadAudit() {
        try {
            String action = (String) auditAction.getSelectedItem();
            auditModel.setRows(ctx.getAdminController().auditLog(auditUser.getText(),
                    ANY_ACTION.equals(action) ? null : action, auditFrom.getDate(), auditTo.getDate()));
        } catch (RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
    }
}
