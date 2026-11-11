package com.acme.payroll.ui.model;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only table model backed by a list of beans. Subclasses define columns and cell values.
 */
public abstract class ListTableModel<T> extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final String[] columns;
    private final Class<?>[] types;
    private List<T> rows = new ArrayList<T>();

    protected ListTableModel(String[] columns, Class<?>[] types) {
        this.columns = columns;
        this.types = types;
    }

    public void setRows(List<T> newRows) {
        this.rows = new ArrayList<T>(newRows);
        fireTableDataChanged();
    }

    public List<T> getRows() {
        return rows;
    }

    public T getRow(int index) {
        return index < 0 || index >= rows.size() ? null : rows.get(index);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return types[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return value(rows.get(rowIndex), columnIndex);
    }

    protected abstract Object value(T row, int column);
}
