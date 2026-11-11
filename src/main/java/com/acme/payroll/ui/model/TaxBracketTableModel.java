package com.acme.payroll.ui.model;

import com.acme.payroll.model.TaxBracket;
import com.acme.payroll.ui.util.Formats;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Editable grid of tax brackets. Values are kept as text until saved. */
public class TaxBracketTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNS = {"From (monthly)", "To (blank = no limit)", "Rate %"};

    private final List<String[]> rows = new ArrayList<String[]>();

    public void setBrackets(List<TaxBracket> brackets) {
        rows.clear();
        for (TaxBracket b : brackets) {
            rows.add(new String[] {plain(b.getLowerBound()), plain(b.getUpperBound()), plain(b.getRate())});
        }
        fireTableDataChanged();
    }

    public List<TaxBracket> toBrackets() {
        List<TaxBracket> list = new ArrayList<TaxBracket>();
        for (String[] r : rows) {
            list.add(new TaxBracket(Formats.parseDecimal(r[0]), Formats.parseDecimal(r[1]), Formats.parseDecimal(r[2])));
        }
        return list;
    }

    public void addRow() {
        String lower = rows.isEmpty() ? "0" : rows.get(rows.size() - 1)[1];
        rows.add(new String[] {lower, "", ""});
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
    }

    public void removeRow(int index) {
        if (index >= 0 && index < rows.size()) {
            rows.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    private static String plain(java.math.BigDecimal v) {
        return v == null ? "" : v.stripTrailingZeros().toPlainString();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex)[columnIndex];
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        rows.get(rowIndex)[columnIndex] = value == null ? "" : value.toString().trim();
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
