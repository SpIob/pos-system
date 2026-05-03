package ui;

import dao.ProductDAO;
import model.Product;
import model.User;
import util.GlobalSettings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ProductsPanel extends JPanel {

    // DAO and current user
    private final ProductDAO productDAO = new ProductDAO();
    private User             currentUser;

    // Components
    private JTextField        searchField;
    private JButton           addBtn;
    private JButton           editBtn;
    private JButton           deleteBtn;
    private JPanel            lowStockBanner;
    private JLabel            lowStockLabel;
    private JTable            productTable;
    private DefaultTableModel productModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // Tracks product_id for each table row
    private final List<Integer> productIdList = new ArrayList<>();

    // Constructor
    public ProductsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    // Called by MainFrame after login
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // Build UI
    private void buildUI() {
        JPanel mainCard = UIHelper.card(new BorderLayout(), 16);
        mainCard.add(buildToolbar(), BorderLayout.NORTH);
        mainCard.add(buildTable(),   BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    // Toolbar
    // ---------------------------------------------------------------
    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(0, 10));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Top row
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        // Search field
        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.FIELD_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        searchField.setForeground(UIHelper.PLACEHOLDER);
        searchField.setText("🔍  Search products...");
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().startsWith("🔍")) {
                    searchField.setText("");
                    searchField.setForeground(new Color(0x333333));
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("🔍  Search products...");
                    searchField.setForeground(UIHelper.PLACEHOLDER);
                }
            }
        });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        // Action buttons — use UIHelper.button()
        addBtn    = UIHelper.button("+ Add Product", UIHelper.GREEN, UIHelper.GREEN_HOV, Color.WHITE);
        editBtn   = UIHelper.button("✏  Edit", UIHelper.BLUE,  UIHelper.BLUE_HOV,  Color.WHITE);
        deleteBtn = UIHelper.button("🗑  Delete", UIHelper.RED,   UIHelper.RED_HOV,   Color.WHITE);

        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        addBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (addBtn.isEnabled()) openAddDialog();
            }
        });
        editBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (editBtn.isEnabled()) openEditDialog();
            }
        });
        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (deleteBtn.isEnabled()) deleteSelected();
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        topRow.add(searchField, BorderLayout.WEST);
        topRow.add(btnPanel,    BorderLayout.EAST);

        // Low stock banner
        lowStockBanner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(UIHelper.AMBER);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        lowStockBanner.setOpaque(false);
        lowStockBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, UIHelper.AMBER_BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        lowStockLabel = new JLabel("⚠  Low Stock Alert: loading...");
        lowStockLabel.setFont(UIHelper.FONT_PLAIN_MD);
        lowStockLabel.setForeground(new Color(0x5D4037));
        lowStockBanner.add(lowStockLabel, BorderLayout.CENTER);
        lowStockBanner.setVisible(false);

        toolbar.add(topRow,         BorderLayout.NORTH);
        toolbar.add(lowStockBanner, BorderLayout.SOUTH);
        return toolbar;
    }

    // ---------------------------------------------------------------
    // Product table
    // ---------------------------------------------------------------
    private JScrollPane buildTable() {
        String[] cols = {
            "Product Name", "Category", "Price", "Stock", "Threshold", "Status"
        };
        productModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        productTable = new JTable(productModel);
        sorter = new TableRowSorter<>(productModel);
        productTable.setRowSorter(sorter);
        productTable.setRowHeight(38);

        UIHelper.styleTable(productTable);
        
        productTable.getColumnModel().getColumn(5).setCellRenderer(UIHelper.statusRenderer("✔ OK", "⚠ LOW"));

        productTable.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = productTable.getSelectedRow() >= 0;
            editBtn.setEnabled(sel);
            deleteBtn.setEnabled(sel);
        });

        // Override the default renderer to add amber tint for LOW rows
        productTable.setDefaultRenderer(Object.class,
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
                    int mRow = productTable.convertRowIndexToModel(row);
                    String status = productModel.getValueAt(mRow, 5).toString();
                    setBackground(status.equals("LOW")
                            ? UIHelper.ROW_LOW
                            : (row % 2 == 0 ? Color.WHITE : UIHelper.ROW_ALT));
                    setForeground(new Color(0x333333));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                setFont(new Font("Arial", Font.PLAIN, 13));
                return this;
            }
        });

        // Status column — use UIHelper's statusRenderer
        productTable.getColumnModel().getColumn(5)
                .setCellRenderer(UIHelper.statusRenderer("✔ OK", "⚠ LOW"));

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // ---------------------------------------------------------------
    // Load live data from ProductDAO
    // ---------------------------------------------------------------
    public void refreshData() {
        new SwingWorker<Void, Void>() {
            List<Product> products;
            List<Product> lowStock;

            @Override
            protected Void doInBackground() {
                products = productDAO.getAllProducts();
                lowStock = productDAO.getLowStockProducts();
                return null;
            }

            @Override
            protected void done() {
                productModel.setRowCount(0);
                productIdList.clear();

                for (Product p : products) {
                    productIdList.add(p.getProductId());
                    productModel.addRow(new Object[]{
                        p.getProductName(),
                        UIHelper.capitalize(p.getCategory()),
                        String.format("\u20B1%.2f", p.getPrice()),
                        p.getStockQuantity(),
                        GlobalSettings.LOW_STOCK_THRESHOLD,
                        p.isLowStock() ? "LOW" : "OK"
                    });
                }

                if (lowStock.isEmpty()) {
                    lowStockBanner.setVisible(false);
                } else {
                    StringBuilder msg = new StringBuilder("⚠  Low Stock Alert: ");
                    for (int i = 0; i < lowStock.size(); i++) {
                        Product p = lowStock.get(i);
                        msg.append(p.getProductName())
                           .append(" (").append(p.getStockQuantity())
                           .append(" remaining)");
                        if (i < lowStock.size() - 1) msg.append(", ");
                    }
                    lowStockLabel.setText(msg.toString());
                    lowStockBanner.setVisible(true);
                }
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Add product
    // ---------------------------------------------------------------
    private void openAddDialog() {
        if (!confirmPassword("Add Product")) return;

        AddProductDialog dlg = new AddProductDialog(null, null);
        dlg.setOnSaveListener(data -> {
            Product p = new Product();
            p.setProductName(data[0]);
            p.setCategory(data[1].toLowerCase());
            p.setPrice(Double.parseDouble(data[2]));
            p.setStockQuantity(Integer.parseInt(data[3]));
            p.setLowStockThreshold(GlobalSettings.LOW_STOCK_THRESHOLD);

            if (productDAO.addProduct(p)) {
                refreshData();
            } else {
                JOptionPane.showMessageDialog(ProductsPanel.this,
                    "Failed to save product. Check your connection.",
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dlg.setVisible(true);
    }

    // ---------------------------------------------------------------
    // Edit product
    // ---------------------------------------------------------------
    private void openEditDialog() {
        int viewRow = productTable.getSelectedRow();
        if (viewRow < 0) return;
        if (!confirmPassword("Edit Product")) return;

        int mRow    = productTable.convertRowIndexToModel(viewRow);
        int pid     = getProductIdAtRow(mRow);
        String name     = productModel.getValueAt(mRow, 0).toString();
        String category = productModel.getValueAt(mRow, 1).toString();
        String price    = productModel.getValueAt(mRow, 2).toString().replace("\u20B1", "");
        String stock    = productModel.getValueAt(mRow, 3).toString();

        AddProductDialog dlg = new AddProductDialog(null,
                new String[]{name, category, price, stock});
        dlg.setOnSaveListener(data -> {
            Product p = new Product();
            p.setProductId(pid);
            p.setProductName(data[0]);
            p.setCategory(data[1].toLowerCase());
            p.setPrice(Double.parseDouble(data[2]));
            p.setStockQuantity(Integer.parseInt(data[3]));
            p.setLowStockThreshold(GlobalSettings.LOW_STOCK_THRESHOLD);

            if (productDAO.updateProduct(p)) {
                refreshData();
            } else {
                JOptionPane.showMessageDialog(ProductsPanel.this,
                    "Failed to update. Check your connection.",
                    "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dlg.setVisible(true);
    }

    // ---------------------------------------------------------------
    // Delete product
    // ---------------------------------------------------------------
    private void deleteSelected() {
        int viewRow = productTable.getSelectedRow();
        if (viewRow < 0) return;
        if (!confirmPassword("Delete Product")) return;

        int    mRow = productTable.convertRowIndexToModel(viewRow);
        int    pid  = getProductIdAtRow(mRow);
        String name = productModel.getValueAt(mRow, 0).toString();

        int choice = JOptionPane.showConfirmDialog(this,
            "Delete \"" + name + "\"? This cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            if (productDAO.deleteProduct(pid)) {
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete. It may be linked to existing transactions.",
                    "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Password confirmation helper
    private boolean confirmPassword(String action) {
        if (currentUser == null) return false;
        PasswordConfirmDialog dlg = new PasswordConfirmDialog(null, currentUser, action);
        dlg.setVisible(true);
        return dlg.isConfirmed();
    }

    // Search filter
    private void applyFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty() || text.startsWith("🔍")) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
        }
    }

    // Helpers
    private int getProductIdAtRow(int modelRow) {
        if (modelRow >= 0 && modelRow < productIdList.size())
            return productIdList.get(modelRow);
        return -1;
    }
}