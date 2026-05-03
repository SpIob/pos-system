package ui;

import dao.ProductDAO;
import dao.TransactionDAO;
import model.Product;
import model.User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ReportsPanel extends JPanel {

    // DAOs
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ProductDAO     productDAO     = new ProductDAO();

    // Current user — set from MainFrame
    private User currentUser;

    // KPI labels
    private JLabel todaysSalesValue;
    private JLabel totalTxnValue;
    private JLabel lowStockValue;

    // Tables
    private DefaultTableModel txnModel;
    private JTable            txnTable;
    private DefaultTableModel inventoryModel;
    private JTable            inventoryTable;

    // Tracks transaction IDs and statuses per row
    private final java.util.List<Integer> txnIdList     = new java.util.ArrayList<>();
    private final java.util.List<String>  txnStatusList = new java.util.ArrayList<>();

    // Admin-only controls
    private JButton exportBtn;
    private JButton voidBtn;

    // Constructor
    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    // Set current user — called by MainFrame after login
    public void setCurrentUser(User user) {
        this.currentUser = user;
        boolean isAdmin = user != null && user.isAdmin();
        if (exportBtn != null) exportBtn.setVisible(isAdmin);
        if (voidBtn   != null) voidBtn.setVisible(isAdmin);
    }

    // Build UI
    private void buildUI() {
        add(buildStatsRow(),      BorderLayout.NORTH);
        add(buildCenterSection(), BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    // KPI stat cards — delegate entirely to UIHelper
    // ---------------------------------------------------------------
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel salesCard = UIHelper.statCard("\u20B10.00", "Today's Sales", UIHelper.GREEN, UIHelper.GREEN);
        todaysSalesValue = UIHelper.getValueLabel(salesCard);

        JPanel txnCard = UIHelper.statCard("0", "Total Transactions", UIHelper.NAVY, UIHelper.BLUE);
        totalTxnValue = UIHelper.getValueLabel(txnCard);

        JPanel lowCard = UIHelper.statCard("0 ⚠", "Low Stock Items", UIHelper.AMBER_TXT, new Color(0xFF8F00));
        lowStockValue = UIHelper.getValueLabel(lowCard);
        
        todaysSalesValue.setFont(new Font("Dialog", Font.BOLD, 26));
        totalTxnValue.setFont(   new Font("Dialog", Font.BOLD, 26));
        lowStockValue.setFont(   new Font("Dialog", Font.BOLD, 26));

        row.add(salesCard);
        row.add(txnCard);
        row.add(lowCard);
        return row;
    }

    // Center section
    private JPanel buildCenterSection() {
        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(buildTransactionsPanel(), BorderLayout.CENTER);
        center.add(buildInventoryPanel(),    BorderLayout.EAST);
        return center;
    }

    // ---------------------------------------------------------------
    // Transactions panel
    // ---------------------------------------------------------------
    private JPanel buildTransactionsPanel() {
        JPanel panel = UIHelper.card(new BorderLayout(), 20);

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("Recent Transactions");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));

        // Admin-only buttons
        JPanel adminBtns = new JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        adminBtns.setOpaque(false);

        voidBtn   = UIHelper.button("Void",   UIHelper.VOID_RED, UIHelper.VOID_HOV,   Color.WHITE);
        voidBtn.setEnabled(false);
        voidBtn.setVisible(false);
        voidBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (voidBtn.isEnabled()) handleVoidTransaction();
            }
        });

        exportBtn = UIHelper.button("Export", UIHelper.EXPORT_BG, UIHelper.EXPORT_HOV, new Color(0x333333));
        exportBtn.setVisible(false);
        exportBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { exportToCSV(); }
        });

        adminBtns.add(voidBtn);
        adminBtns.add(exportBtn);

        titleRow.add(title,     BorderLayout.WEST);
        titleRow.add(adminBtns, BorderLayout.EAST);

        // Table
        String[] cols = {"TXN #", "Date & Time", "Items", "Total", "Cashier", "Status"};
        txnModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        txnTable = new JTable(txnModel);
        UIHelper.styleTable(txnTable);

        // Enable void button only for selected completed rows
        txnTable.getSelectionModel().addListSelectionListener(e -> {
            int sel = txnTable.getSelectedRow();
            if (sel >= 0 && currentUser != null && currentUser.isAdmin()) {
                String status = txnStatusList.size() > sel
                        ? txnStatusList.get(sel) : "completed";
                voidBtn.setEnabled("completed".equals(status));
            } else {
                voidBtn.setEnabled(false);
            }
        });

        // Row renderer: voided rows get pink tint
        txnTable.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String status = row < txnStatusList.size()
                        ? txnStatusList.get(row) : "completed";
                boolean isVoided = "voided".equals(status);

                if (sel) {
                    setBackground(UIHelper.NAVY);
                    setForeground(Color.WHITE);
                } else if (isVoided) {
                    setBackground(UIHelper.ROW_VOID);
                    setForeground(new Color(0x999999));
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : UIHelper.ROW_ALT);
                    setForeground(col == 0 ? UIHelper.TXN_BLUE : new Color(0x333333));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                setFont(UIHelper.FONT_PLAIN_MD);
                return this;
            }
        });

        // Status column renderer — reuse UIHelper's statusRenderer
        txnTable.getColumnModel().getColumn(5)
                .setCellRenderer(UIHelper.statusRenderer("Completed", "Voided"));

        JScrollPane scroll = new JScrollPane(txnTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(titleRow, BorderLayout.NORTH);
        panel.add(scroll,   BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------------
    // Inventory panel
    // ---------------------------------------------------------------
    private JPanel buildInventoryPanel() {
        JPanel panel = UIHelper.card(new BorderLayout(), 20);
        panel.setPreferredSize(new Dimension(440, 0));

        JLabel title = new JLabel("Inventory Status");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Product", "Stock", "Status"};
        inventoryModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        inventoryTable = new JTable(inventoryModel);
        UIHelper.styleTable(inventoryTable);

        // Status column — LOW vs OK via UIHelper renderer
        inventoryTable.getColumnModel().getColumn(2)
                .setCellRenderer(UIHelper.statusRenderer("✔ OK", "⚠ LOW"));

        // Row renderer: amber tint for LOW rows
        inventoryTable.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (sel) {
                    setBackground(UIHelper.NAVY);
                    setForeground(Color.WHITE);
                } else {
                    String status = inventoryModel
                            .getValueAt(row, 2).toString();
                    setBackground("LOW".equals(status)
                            ? UIHelper.ROW_LOW
                            : (row % 2 == 0 ? Color.WHITE : UIHelper.ROW_ALT));
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

    // ---------------------------------------------------------------
    // refreshData() — loads all live data
    // ---------------------------------------------------------------
    public void refreshData() {
        new SwingWorker<Void, Void>() {
            double todayTotal;
            int    todayCount;
            int    lowStockCount;
            java.util.List<Object[]> richRows;
            java.util.List<Product>  allProducts;

            @Override
            protected Void doInBackground() {
                todayTotal    = transactionDAO.getTodayTotal();
                todayCount    = transactionDAO.getTodayCount();
                lowStockCount = productDAO.getLowStockCount();
                richRows      = transactionDAO.getRecentTransactionsRich(20);
                allProducts   = productDAO.getAllProducts();
                return null;
            }

            @Override
            protected void done() {
                SimpleDateFormat fmt = new SimpleDateFormat("MMM d hh:mm a");

                todaysSalesValue.setText(String.format("\u20B1%,.2f", todayTotal));
                totalTxnValue.setText(String.valueOf(todayCount));
                lowStockValue.setText(lowStockCount > 0 ? lowStockCount + " ⚠" : "0");

                txnModel.setRowCount(0);
                txnIdList.clear();
                txnStatusList.clear();

                for (Object[] row : richRows) {
                    int    txnId     = (int)    row[0];
                    java.sql.Timestamp date = (java.sql.Timestamp) row[1];
                    int    itemCount = (int)    row[2];
                    double total     = (double) row[3];
                    String username  = (String) row[4];
                    String status    = (String) row[5];

                    String itemLabel = itemCount == 1 ? "1 item" : itemCount + " items";
                    txnIdList.add(txnId);
                    txnStatusList.add(status != null ? status : "completed");

                    txnModel.addRow(new Object[]{
                        String.format("TXN-%04d", txnId),
                        fmt.format(date),
                        itemLabel,
                        String.format("\u20B1%,.2f", total),
                        username != null ? username : "—",
                        status != null ? status : "completed"
                    });
                }

                inventoryModel.setRowCount(0);
                for (Product p : allProducts) {
                    inventoryModel.addRow(new Object[]{
                        p.getProductName(),
                        p.getStockQuantity(),
                        p.isLowStock() ? "LOW" : "OK"
                    });
                }
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Void transaction handler (admin only)
    // ---------------------------------------------------------------
    private void handleVoidTransaction() {
        int viewRow = txnTable.getSelectedRow();
        if (viewRow < 0 || viewRow >= txnIdList.size()) return;

        int    txnId  = txnIdList.get(viewRow);
        String txnNum = String.format("TXN-%04d", txnId);

        if (currentUser == null) return;
        PasswordConfirmDialog pwd = new PasswordConfirmDialog(
                null, currentUser, "Void " + txnNum);
        pwd.setVisible(true);
        if (!pwd.isConfirmed()) return;

        int choice = JOptionPane.showConfirmDialog(this,
            "Void " + txnNum + "?\n\n"
            + "Stock quantities will be restored.\n"
            + "This action cannot be undone.",
            "Confirm Void Transaction",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return transactionDAO.voidTransaction(txnId);
            }

            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ReportsPanel.this,
                            txnNum + " has been voided.\nStock has been restored.",
                            "Void Successful", JOptionPane.INFORMATION_MESSAGE);
                        refreshData();
                    } else {
                        JOptionPane.showMessageDialog(ReportsPanel.this,
                            "Could not void this transaction.\nIt may already be voided.",
                            "Void Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Export to CSV
    // ---------------------------------------------------------------
    private void exportToCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("transactions_export.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();

        new SwingWorker<Void, Void>() {
            java.util.List<Object[]> rows;

            @Override
            protected Void doInBackground() {
                rows = transactionDAO.getRecentTransactionsRich(Integer.MAX_VALUE);
                return null;
            }

            @Override
            protected void done() {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write("TXN #,Date & Time,Items,Total,Cashier,Status\n");
                    for (Object[] row : rows) {
                        int    txnId  = (int)    row[0];
                        java.sql.Timestamp date = (java.sql.Timestamp) row[1];
                        int    items  = (int)    row[2];
                        double total  = (double) row[3];
                        String user   = (String) row[4];
                        String status = (String) row[5];
                        fw.write(String.format("TXN-%04d,%s,%d,%.2f,%s,%s\n",
                            txnId, fmt.format(date), items, total,
                            user   != null ? user   : "",
                            status != null ? status : "completed"));
                    }
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                        "Exported successfully to:\n" + file.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                        "Export failed: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}