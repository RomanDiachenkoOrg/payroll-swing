package com.acme.payroll.ui;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.Department;
import com.acme.payroll.ui.model.DepartmentTableModel;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

public class DepartmentsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final DepartmentTableModel model = new DepartmentTableModel();
    private final JTable table = new JTable(model);
    private final JButton newButton = UiUtil.button("New...", "departments.new");
    private final JButton editButton = UiUtil.button("Edit...", "departments.edit");
    private final JButton deleteButton = UiUtil.button("Delete", "departments.delete");

    public DepartmentsPanel(AppContext ctx) {
        super(new BorderLayout(4, 4));
        this.ctx = ctx;
        setName("departmentsPanel");
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        table.setName("departments.table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(newButton);
        actions.add(editButton);
        actions.add(deleteButton);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        newButton.addActionListener(e -> edit(new Department()));
        editButton.addActionListener(e -> {
            Department d = selected();
            if (d != null) {
                edit(new Department(d.getId(), d.getName(), d.getCostCenter()));
            }
        });
        deleteButton.addActionListener(e -> delete());
        updateButtons();
    }

    @Override
    public void refresh() {
        model.setRows(ctx.getDepartmentController().list());
        updateButtons();
    }

    private Department selected() {
        return model.getRow(table.getSelectedRow());
    }

    private void updateButtons() {
        boolean sel = selected() != null;
        editButton.setEnabled(sel);
        deleteButton.setEnabled(sel);
    }

    private void edit(Department d) {
        JTextField name = new JTextField(d.getName(), 20);
        name.setName("department.name");
        JTextField costCenter = new JTextField(d.getCostCenter(), 10);
        costCenter.setName("department.costCenter");
        JPanel p = new JPanel(new GridBagLayout());
        p.add(UiUtil.label("Name *:", name), UiUtil.gbc(0, 0));
        p.add(name, UiUtil.gbc(1, 0));
        p.add(UiUtil.label("Cost center:", costCenter), UiUtil.gbc(0, 1));
        p.add(costCenter, UiUtil.gbc(1, 1));
        while (true) {
            int answer = JOptionPane.showConfirmDialog(this, p, d.getId() == null ? "New department" : "Edit department",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }
            d.setName(name.getText());
            d.setCostCenter(costCenter.getText());
            try {
                ctx.getDepartmentController().save(d);
                refresh();
                return;
            } catch (RuntimeException ex) {
                UiUtil.showError(this, ex);
            }
        }
    }

    private void delete() {
        Department d = selected();
        if (d == null || !UiUtil.confirm(this, "Delete department '" + d.getName() + "'?", "Delete department")) {
            return;
        }
        try {
            ctx.getDepartmentController().delete(d.getId());
            refresh();
        } catch (RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
    }
}
