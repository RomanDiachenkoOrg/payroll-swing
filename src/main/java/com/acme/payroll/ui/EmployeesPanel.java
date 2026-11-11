package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.dao.EmployeeFilter;
import com.acme.payroll.model.Department;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.Role;
import com.acme.payroll.service.Session;
import com.acme.payroll.ui.model.EmployeeTableModel;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class EmployeesPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;
    static final int PAGE_SIZE = 10;

    private static final Department ALL_DEPARTMENTS = new Department(null, "All departments", null);

    private final AppContext ctx;
    private final EmployeeFilter filter = new EmployeeFilter();
    private final EmployeeTableModel model = new EmployeeTableModel();
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<Department> departmentCombo = new JComboBox<Department>();
    private final JComboBox<EmployeeFilter.Status> statusCombo = new JComboBox<EmployeeFilter.Status>(
            EmployeeFilter.Status.values());
    private final JLabel pageLabel = new JLabel();
    private final JButton prevButton = UiUtil.button("< Prev", "employees.prevPage");
    private final JButton nextButton = UiUtil.button("Next >", "employees.nextPage");
    private final JButton newButton = UiUtil.button("New...", "employees.new");
    private final JButton editButton = UiUtil.button("Edit...", "employees.edit");
    private final JButton detailsButton = UiUtil.button("Details...", "employees.details");
    private final JButton deactivateButton = UiUtil.button("Deactivate", "employees.deactivate");

    private int page;
    private int pageCount = 1;

    public EmployeesPanel(AppContext ctx) {
        super(new BorderLayout(4, 4));
        this.ctx = ctx;
        setName("employeesPanel");
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        searchField.setName("employees.search");
        departmentCombo.setName("employees.departmentFilter");
        statusCombo.setName("employees.statusFilter");
        statusCombo.setSelectedItem(EmployeeFilter.Status.ACTIVE);
        pageLabel.setName("employees.pageLabel");
        table.setName("employees.table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);
        UiUtil.installMoneyRenderer(table);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(UiUtil.label("Search:", searchField));
        filters.add(searchField);
        filters.add(UiUtil.label("Department:", departmentCombo));
        filters.add(departmentCombo);
        filters.add(UiUtil.label("Status:", statusCombo));
        filters.add(statusCombo);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(newButton);
        actions.add(editButton);
        actions.add(detailsButton);
        actions.add(deactivateButton);

        JPanel paging = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paging.add(prevButton);
        paging.add(pageLabel);
        paging.add(nextButton);

        JPanel south = new JPanel(new BorderLayout());
        south.add(actions, BorderLayout.WEST);
        south.add(paging, BorderLayout.EAST);

        add(filters, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        boolean admin = !Session.isLoggedIn() || Session.hasRole(Role.ADMIN);
        newButton.setVisible(admin);
        editButton.setVisible(admin);
        deactivateButton.setVisible(admin);

        wireEvents();
    }

    private void wireEvents() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        departmentCombo.addActionListener(e -> applyFilter());
        statusCombo.addActionListener(e -> applyFilter());
        prevButton.addActionListener(e -> { page--; load(); });
        nextButton.addActionListener(e -> { page++; load(); });
        newButton.addActionListener(e -> openEditor(null));
        editButton.addActionListener(e -> openEditor(selected()));
        detailsButton.addActionListener(e -> openDetails());
        deactivateButton.addActionListener(e -> deactivate());
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openDetails();
                }
            }
        });
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col < 0) {
                    return;
                }
                EmployeeFilter.SortField field = EmployeeTableModel.SORT_FIELDS[table.convertColumnIndexToModel(col)];
                if (field == null) {
                    return;
                }
                if (filter.getSortField() == field) {
                    filter.setAscending(!filter.isAscending());
                } else {
                    filter.setSortField(field);
                    filter.setAscending(true);
                }
                load();
            }
        });
    }

    @Override
    public void refresh() {
        Object current = departmentCombo.getSelectedItem();
        DefaultComboBoxModel<Department> m = new DefaultComboBoxModel<Department>();
        m.addElement(ALL_DEPARTMENTS);
        for (Department d : ctx.getEmployeeController().departments()) {
            m.addElement(d);
        }
        departmentCombo.setModel(m);
        if (current != null && current != ALL_DEPARTMENTS) {
            departmentCombo.setSelectedItem(current);
        }
        load();
    }

    private void applyFilter() {
        page = 0;
        load();
    }

    private void load() {
        filter.setText(searchField.getText());
        Department d = (Department) departmentCombo.getSelectedItem();
        filter.setDepartmentId(d == null ? null : d.getId());
        filter.setStatus((EmployeeFilter.Status) statusCombo.getSelectedItem());

        int total = ctx.getEmployeeController().count(filter);
        pageCount = Math.max(1, total / PAGE_SIZE);
        if (page >= pageCount) {
            page = pageCount - 1;
        }
        if (page < 0) {
            page = 0;
        }
        List<Employee> rows = ctx.getEmployeeController().search(filter, page, PAGE_SIZE);
        model.setRows(rows);
        pageLabel.setText("Page " + (page + 1) + " of " + pageCount + "  (" + total + " employees)");
        prevButton.setEnabled(page > 0);
        nextButton.setEnabled(page < pageCount - 1);
        updateButtons();
    }

    private void updateButtons() {
        Employee e = selected();
        editButton.setEnabled(e != null);
        detailsButton.setEnabled(e != null);
        deactivateButton.setEnabled(e != null && e.isActive());
    }

    private Employee selected() {
        return model.getRow(table.getSelectedRow());
    }

    private void openEditor(Employee e) {
        EmployeeDialog dialog = new EmployeeDialog(this, ctx, e == null ? null : ctx.getEmployeeController().find(e.getId()));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            load();
        }
    }

    private void openDetails() {
        Employee e = selected();
        if (e != null) {
            new EmployeeDetailDialog(this, ctx, e.getId()).setVisible(true);
            load();
        }
    }

    private void deactivate() {
        Employee e = selected();
        if (e == null) {
            return;
        }
        if (!UiUtil.confirm(this, "Deactivate " + e.getFullName() + "?\nThe employee will no longer be included in pay runs.",
                "Deactivate employee")) {
            return;
        }
        try {
            ctx.getEmployeeController().deactivate(e.getId());
            load();
        } catch (RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
    }
}
