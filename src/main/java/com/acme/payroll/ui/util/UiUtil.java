package com.acme.payroll.ui.util;

import com.acme.payroll.service.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UiUtil {

    private static final Logger LOG = Logger.getLogger(UiUtil.class.getName());

    private UiUtil() {
    }

    public static JLabel label(String text, JComponent target) {
        JLabel l = new JLabel(text);
        l.setLabelFor(target);
        return l;
    }

    public static JButton button(String text, String name) {
        JButton b = new JButton(text);
        b.setName(name);
        return b;
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, l.getFont().getSize2D() + 4f));
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return l;
    }

    public static GridBagConstraints gbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = x == 0 ? GridBagConstraints.LINE_END : GridBagConstraints.LINE_START;
        c.fill = x == 0 ? GridBagConstraints.NONE : GridBagConstraints.HORIZONTAL;
        c.weightx = x == 0 ? 0 : 1;
        return c;
    }

    public static void showError(Component parent, Throwable t) {
        if (t instanceof ValidationException) {
            JOptionPane.showMessageDialog(parent, t.getMessage(), "Please check", JOptionPane.WARNING_MESSAGE);
        } else if (t instanceof IllegalArgumentException) {
            JOptionPane.showMessageDialog(parent, t.getMessage(), "Invalid input", JOptionPane.WARNING_MESSAGE);
        } else {
            LOG.log(Level.SEVERE, "Unexpected error", t);
            JOptionPane.showMessageDialog(parent, "Unexpected error: " + t.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Payroll", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message, String title) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Asks for a CSV target file; returns null if cancelled. */
    public static File chooseCsvFile(Component parent, String suggestedName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setName("csvFileChooser");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        chooser.setSelectedFile(new File(suggestedName));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) {
            f = new File(f.getParentFile(), f.getName() + ".csv");
        }
        if (f.exists() && !confirm(parent, f.getName() + " already exists. Overwrite?", "Export")) {
            return null;
        }
        return f;
    }

    /** Right-aligns money columns and formats them as currency. */
    public static void installMoneyRenderer(JTable table) {
        table.setDefaultRenderer(BigDecimal.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(RIGHT);
                setText(value == null ? "" : Formats.money((BigDecimal) value));
            }
        });
    }
}
