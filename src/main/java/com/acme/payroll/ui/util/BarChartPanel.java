package com.acme.payroll.ui.util;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Very small horizontal bar chart (no external charting library). */
public class BarChartPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color BAR = new Color(70, 120, 180);

    private final List<String> labels = new ArrayList<String>();
    private final List<BigDecimal> values = new ArrayList<BigDecimal>();
    private String emptyText = "No data";

    public BarChartPanel() {
        setPreferredSize(new Dimension(420, 200));
        setBackground(Color.WHITE);
    }

    public void setData(List<String> newLabels, List<BigDecimal> newValues) {
        labels.clear();
        values.clear();
        labels.addAll(newLabels);
        values.addAll(newValues);
        repaint();
    }

    public void setEmptyText(String emptyText) {
        this.emptyText = emptyText;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fm = g2.getFontMetrics();
        if (values.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString(emptyText, 10, 20);
            g2.dispose();
            return;
        }
        BigDecimal max = BigDecimal.ONE;
        int labelWidth = 0;
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).compareTo(max) > 0) {
                max = values.get(i);
            }
            labelWidth = Math.max(labelWidth, fm.stringWidth(labels.get(i)));
        }
        int left = labelWidth + 16;
        int valueWidth = fm.stringWidth("$000,000.00") + 10;
        int available = Math.max(10, getWidth() - left - valueWidth);
        int rowHeight = Math.max(18, (getHeight() - 10) / values.size());
        for (int i = 0; i < values.size(); i++) {
            int y = 5 + i * rowHeight;
            int barLen = (int) (available * values.get(i).doubleValue() / max.doubleValue());
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(labels.get(i), 8, y + rowHeight / 2 + fm.getAscent() / 2 - 2);
            g2.setColor(BAR);
            g2.fillRect(left, y + 3, barLen, rowHeight - 6);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(Formats.money(values.get(i)), left + barLen + 6, y + rowHeight / 2 + fm.getAscent() / 2 - 2);
        }
        g2.dispose();
    }
}
