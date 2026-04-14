package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class ReportsPanel extends JPanel {

    // Colors
    private static final Color PAGE_BG    = new Color(0xF5F5F5);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color NAVY_DARK  = new Color(0x1C3557);
    private static final Color GREEN_VAL  = new Color(0x2E7D32);
    private static final Color BLUE_VAL   = new Color(0x1C3557);
    private static final Color ORANGE_VAL = new Color(0xE65100);
    private static final Color GRAY_LABEL = new Color(0x777777);
    private static final Color TXN_BLUE   = new Color(0x2D6DA8);
    private static final Color ROW_ALT    = new Color(0xF0F4F8);
    private static final Color ROW_LOW    = new Color(0xFFFDE7);
    private static final Color OK_GREEN   = new Color(0x2E7D32);
    private static final Color LOW_AMBER  = new Color(0xE65100);
    private static final Color EXPORT_BG  = new Color(0xEEEEEE);
    private static final Color EXPORT_HOV = new Color(0xDDDDDD);

    // Components
    private JLabel            todaysSalesValue;
    private JLabel            totalTxnValue;
    private JLabel            lowStockValue;
    private DefaultTableModel txnModel;
    private JTable            txnTable;
    private DefaultTableModel inventoryModel;
    private JTable            inventoryTable;

    // Constructor
    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadSampleData();
    }

    // Build UI
    private void buildUI() {
        add(buildStatsRow(),     BorderLayout.NORTH);
        add(buildCenterSection(), BorderLayout.CENTER);
    }

    // KPI stat cards
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel salesCard = buildStatCard("₱0.00", "Today's Sales",
                GREEN_VAL, new Color(0x2E7D32));
        todaysSalesValue = getValueLabel(salesCard);

        JPanel txnCard = buildStatCard("0", "Total Transactions",
                BLUE_VAL, new Color(0x1565C0));
        totalTxnValue = getValueLabel(txnCard);

        JPanel lowCard = buildStatCard("0 ⚠", "Low Stock Items",
                ORANGE_VAL, new Color(0xFF8F00));
        lowStockValue = getValueLabel(lowCard);

        row.add(salesCard);
        row.add(txnCard);
        row.add(lowCard);
        return row;
    }

    private JPanel buildStatCard(String value, String labelText,
                                  Color valueColor, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valLabel.setForeground(valueColor);

        JLabel descLabel = new JLabel(labelText);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(GRAY_LABEL);

        JPanel stack = new JPanel(new BorderLayout(0, 4));
        stack.setOpaque(false);
        stack.add(valLabel,  BorderLayout.NORTH);
        stack.add(descLabel, BorderLayout.SOUTH);
        card.add(stack, BorderLayout.CENTER);
        return card;
    }

    private JLabel getValueLabel(JPanel card) {
        JPanel stack = (JPanel) card.getComponent(0);
        return (JLabel) stack.getComponent(0);
    }

    // Center section
    private JPanel buildCenterSection() {
        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(buildTransactionsPanel(), BorderLayout.CENTER);
        center.add(buildInventoryPanel(),    BorderLayout.EAST);
        return center;
    }

    // Transactions panel + Export button
    private JPanel buildTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title row with Export button
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("Recent Transactions");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));

        JButton exportBtn = new JButton("⬇  Export") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(new Color(0xBBBBBB));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                super.paintComponent(g);
            }
        };
        exportBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        exportBtn.setForeground(new Color(0x333333));
        exportBtn.setBackground(EXPORT_BG);
        exportBtn.setOpaque(false);
        exportBtn.setContentAreaFilled(false);
        exportBtn.setBorderPainted(false);
        exportBtn.setFocusPainted(false);
        exportBtn.setPreferredSize(new Dimension(90, 30));
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                exportBtn.setBackground(EXPORT_HOV); exportBtn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                exportBtn.setBackground(EXPORT_BG); exportBtn.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                exportToCSV();
            }
        });

        titleRow.add(title,     BorderLayout.WEST);
        titleRow.add(exportBtn, BorderLayout.EAST);

        // Table
        String[] cols = {"TXN #","Date & Time","Items","Total","Cashier"};
        txnModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        txnTable = new JTable(txnModel);
        styleTable(txnTable);

        // TXN # blue
        txnTable.getColumnModel().getColumn(0)
                .setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean focus, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, focus, row, col);
                setForeground(sel ? Color.WHITE : TXN_BLUE);
                setBackground(sel ? NAVY_DARK :
                              (row % 2 == 0 ? Color.WHITE : ROW_ALT));
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(txnTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(titleRow, BorderLayout.NORTH);
        panel.add(scroll,   BorderLayout.CENTER);
        return panel;
    }

    // Inventory status panel
    private JPanel buildInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(440, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Inventory Status");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Product", "Stock", "Status"};
        inventoryModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        inventoryTable = new JTable(inventoryModel);
        styleTable(inventoryTable);

        // Status column colored renderer
        inventoryTable.getColumnModel().getColumn(2)
                .setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean focus, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, focus, row, col);
                String s = val == null ? "" : val.toString();
                if (sel) {
                    setBackground(NAVY_DARK);
                    setForeground(Color.WHITE);
                } else if (s.equals("LOW")) {
                    setBackground(ROW_LOW);
                    setForeground(LOW_AMBER);
                    setText("⚠ LOW");
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    setForeground(OK_GREEN);
                    setText("✔ OK");
                }
                setFont(new Font("Arial", Font.BOLD, 12));
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                return this;
            }
        });

        // LOW rows get amber row tint
        inventoryTable.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean focus, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, focus, row, col);
                if (sel) {
                    setBackground(NAVY_DARK);
                    setForeground(Color.WHITE);
                } else {
                    String status = inventoryModel
                            .getValueAt(row, 2).toString();
                    setBackground(status.equals("LOW") ? ROW_LOW :
                                  (row % 2 == 0 ? Color.WHITE : ROW_ALT));
                    setForeground(new Color(0x333333));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                setFont(new Font("Arial", Font.PLAIN, 13));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(inventoryTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // Shared table styling
    private void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(NAVY_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(NAVY_DARK);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);

        table.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean focus, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, focus, row, col);
                if (sel) {
                    setBackground(NAVY_DARK);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    setForeground(new Color(0x333333));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                setFont(new Font("Arial", Font.PLAIN, 13));
                return this;
            }
        });
    }

    // Load sample data
    private void loadSampleData() {
        todaysSalesValue.setText("₱2,340.00");
        totalTxnValue.setText("14");
        lowStockValue.setText("2 ⚠");

        Object[][] txns = {
            {"TXN-0042","Apr 9 10:34 AM","3 items","₱105.00","admin"},
            {"TXN-0041","Apr 9 10:10 AM","2 items","₱55.00", "cashier1"},
            {"TXN-0040","Apr 9 09:55 AM","5 items","₱195.00","cashier1"},
            {"TXN-0039","Apr 9 09:30 AM","1 item", "₱20.00", "admin"},
            {"TXN-0038","Apr 9 09:05 AM","4 items","₱130.00","cashier1"},
        };
        for (Object[] r : txns) txnModel.addRow(r);

        Object[][] inv = {
            {"Coke 500ml",    50, "OK"},
            {"Mineral Water", 60, "OK"},
            {"Iced Coffee",   30, "OK"},
            {"Chips (Regular)",40,"OK"},
            {"Cup Noodles",   25, "OK"},
            {"Chocolate Bar", 30, "OK"},
            {"Headset Rental", 2, "LOW"},
            {"USB (per use)",  3, "LOW"},
        };
        for (Object[] r : inv) inventoryModel.addRow(r);
    }

    // Export to CSV
    private void exportToCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(
                new java.io.File("transactions_export.csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
            // Header
            fw.write("TXN #,Date & Time,Items,Total,Cashier\n");
            // Rows
            for (int i = 0; i < txnModel.getRowCount(); i++) {
                for (int j = 0; j < txnModel.getColumnCount(); j++) {
                    fw.write(txnModel.getValueAt(i, j).toString());
                    if (j < txnModel.getColumnCount() - 1) fw.write(",");
                }
                fw.write("\n");
            }
            JOptionPane.showMessageDialog(this,
                "Exported successfully.",
                "Export Complete",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Export failed: " + ex.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Main testing code
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            javax.swing.JFrame f = new javax.swing.JFrame("Reports Test");
            f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            f.setSize(1280, 720);
            f.setLocationRelativeTo(null);
            f.add(new ReportsPanel());
            f.setVisible(true);
        });
    }
}