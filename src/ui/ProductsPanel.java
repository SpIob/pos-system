package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.SwingWorker;
import dao.ProductDAO;

public class ProductsPanel extends JPanel {

    // Colors
    private static final Color PAGE_BG     = new Color(0xF5F5F5);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color NAVY_DARK   = new Color(0x1C3557);
    private static final Color GREEN_BTN   = new Color(0x2E7D32);
    private static final Color GREEN_HOVER = new Color(0x1B5E20);
    private static final Color BLUE_BTN    = new Color(0x1565C0);
    private static final Color BLUE_HOVER  = new Color(0x0D47A1);
    private static final Color RED_BTN     = new Color(0xC62828);
    private static final Color RED_HOVER   = new Color(0xB71C1C);
    private static final Color AMBER_BG    = new Color(0xFFFDE7);
    private static final Color AMBER_BORD  = new Color(0xFFB300);
    private static final Color ROW_ALT     = new Color(0xF0F4F8);
    private static final Color ROW_LOW     = new Color(0xFFFDE7);
    private static final Color OK_GREEN    = new Color(0x2E7D32);
    private static final Color LOW_AMBER   = new Color(0xE65100);

    // Components
    private JTextField         searchField;
    private JButton            addBtn;
    private JButton            editBtn;
    private JButton            deleteBtn;
    private JPanel             lowStockBanner;
    private JLabel             lowStockLabel;
    private JTable             productTable;
    private DefaultTableModel  productModel;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Connect to ProductDAO
    private final ProductDAO productDAO = new ProductDAO();

    // Constructor
    public ProductsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        refreshData();
    }

    // Build UI
    private void buildUI() {
        JPanel mainCard = new JPanel(new BorderLayout(0, 0)) {
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
        mainCard.setOpaque(false);
        mainCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        mainCard.add(buildToolbar(),   BorderLayout.NORTH);
        mainCard.add(buildTable(),     BorderLayout.CENTER);

        add(mainCard, BorderLayout.CENTER);
    }

    // Toolbar
    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(0, 10));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Top row: search left, buttons right
        JPanel topRow = new JPanel(new BorderLayout(0, 0));
        topRow.setOpaque(false);

        // Search field
        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCCCCCC), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        // Placeholder
        searchField.setForeground(new Color(0xAAAAAA));
        searchField.setText("🔍  Search products...");
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().startsWith("🔍")) {
                    searchField.setText("");
                    searchField.setForeground(new Color(0x333333));
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("🔍  Search products...");
                    searchField.setForeground(new Color(0xAAAAAA));
                }
            }
        });

        // Live search filter
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        // Buttons
        addBtn    = buildRoundedButton("+ Add Product", GREEN_BTN, GREEN_HOVER);
        editBtn   = buildRoundedButton("✏ Edit",        BLUE_BTN,  BLUE_HOVER);
        deleteBtn = buildRoundedButton("🗑 Delete",      RED_BTN,   RED_HOVER);

        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        addBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openAddDialog(); }
        });
        editBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openEditDialog(); }
        });
        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { deleteSelected(); }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        topRow.add(searchField, BorderLayout.WEST);
        topRow.add(btnPanel,    BorderLayout.EAST);

        // Low stock alert banner
        lowStockBanner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(AMBER_BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        lowStockBanner.setOpaque(false);
        lowStockBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, AMBER_BORD),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        lowStockLabel = new JLabel(
                "⚠  Low Stock Alert: Headset Rental (2 remaining),"
                + " USB per use (3 remaining)");
        lowStockLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        lowStockLabel.setForeground(new Color(0x5D4037));
        lowStockBanner.add(lowStockLabel, BorderLayout.CENTER);

        toolbar.add(topRow,         BorderLayout.NORTH);
        toolbar.add(lowStockBanner, BorderLayout.SOUTH);

        return toolbar;
    }

    // Products table
    private JScrollPane buildTable() {
        String[] cols = {
            "Product Name", "Category", "Price", "Stock", "Threshold", "Status"
        };
        productModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        productTable = new JTable(productModel);

        // Sorting
        sorter = new TableRowSorter<>(productModel);
        productTable.setRowSorter(sorter);

        // Enable/disable Edit+Delete based on selection
        productTable.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = productTable.getSelectedRow() >= 0;
            editBtn.setEnabled(selected);
            deleteBtn.setEnabled(selected);
        });

        // Header style
        JTableHeader header = productTable.getTableHeader();
        header.setBackground(NAVY_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        productTable.setFont(new Font("Arial", Font.PLAIN, 13));
        productTable.setRowHeight(38);
        productTable.setShowGrid(false);
        productTable.setIntercellSpacing(new Dimension(0, 0));
        productTable.setSelectionBackground(NAVY_DARK);
        productTable.setSelectionForeground(Color.WHITE);
        productTable.setFillsViewportHeight(true);

        // Default renderer with row coloring
        productTable.setDefaultRenderer(Object.class,
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
                    // LOW rows get amber tint
                    int modelRow = productTable.convertRowIndexToModel(row);
                    String status = productModel
                            .getValueAt(modelRow, 5).toString();
                    boolean isLow = status.equals("LOW");
                    setBackground(isLow ? ROW_LOW :
                                  (row % 2 == 0 ? Color.WHITE : ROW_ALT));
                    setForeground(new Color(0x333333));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                setFont(new Font("Arial", Font.PLAIN, 13));
                return this;
            }
        });

        // Status column — colored badge renderer
        productTable.getColumnModel().getColumn(5)
                .setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean focus, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, focus, row, col);
                String status = val == null ? "" : val.toString();
                if (sel) {
                    setBackground(NAVY_DARK);
                    setForeground(Color.WHITE);
                } else if (status.equals("LOW")) {
                    setBackground(ROW_LOW);
                    setForeground(LOW_AMBER);
                    setText("⚠ LOW");
                } else {
                    int modelRow = productTable.convertRowIndexToModel(row);
                    setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    setForeground(OK_GREEN);
                    setText("✔ OK");
                }
                setFont(new Font("Arial", Font.BOLD, 12));
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // Search filter
    private void applyFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty() || text.startsWith("🔍")) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                RowFilter.regexFilter("(?i)" + text, 0));
        }
    }

    // Load live data from ProductDAO
    public void refreshData() {
        new SwingWorker<Void, Void>() {
            java.util.List<model.Product> products;
            java.util.List<model.Product> lowStock;

            @Override
            protected Void doInBackground() {
                products = productDAO.getAllProducts();
                lowStock = productDAO.getLowStockProducts();
                return null;
            }

            @Override
            protected void done() {
                // Refresh product table
                productModel.setRowCount(0);
                productIdList.clear();
                for (model.Product p : products) {
                    productIdList.add(p.getProductId());
                    productModel.addRow(new Object[]{
                        p.getProductName(),
                        capitalize(p.getCategory()),
                        String.format("₱%.2f", p.getPrice()),
                        p.getStockQuantity(),
                        p.getLowStockThreshold(),
                        p.getStockStatus()
                    });
                }

                // Refresh low stock banner
                if (lowStock.isEmpty()) {
                    lowStockBanner.setVisible(false);
                } else {
                    StringBuilder msg = new StringBuilder(
                            "⚠  Low Stock Alert: ");
                    for (int i = 0; i < lowStock.size(); i++) {
                        model.Product p = lowStock.get(i);
                        msg.append(p.getProductName())
                           .append(" (")
                           .append(p.getStockQuantity())
                           .append(" remaining)");
                        if (i < lowStock.size() - 1) msg.append(", ");
                    }
                    lowStockLabel.setText(msg.toString());
                    lowStockBanner.setVisible(true);
                }
            }
        }.execute();
    }

    // Add product dialog — wired to ProductDAO on Save
    private void openAddDialog() {
        AddProductDialog dlg = new AddProductDialog(null, null);
        dlg.setOnSaveListener(data -> {
            model.Product p = new model.Product();
            p.setProductName(data[0]);
            p.setCategory(data[1].toLowerCase());
            p.setPrice(Double.parseDouble(data[2]));
            p.setStockQuantity(Integer.parseInt(data[3]));
            p.setLowStockThreshold(Integer.parseInt(data[4]));

            boolean saved = productDAO.addProduct(p);
            if (saved) {
                refreshData();
            } else {
                javax.swing.JOptionPane.showMessageDialog(
                    ProductsPanel.this,
                    "Failed to save product. Check your connection.",
                    "Save Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
        dlg.setVisible(true);
    }

    // Edit product dialog — pre-fills with selected row data
    private void openEditDialog() {
        int viewRow = productTable.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = productTable.convertRowIndexToModel(viewRow);

        // Read current values from the table model
        int    productId  = getProductIdAtRow(modelRow);
        String name       = productModel.getValueAt(modelRow, 0).toString();
        String category   = productModel.getValueAt(modelRow, 1).toString();
        String price      = productModel.getValueAt(modelRow, 2)
                                        .toString().replace("₱", "");
        String stock      = productModel.getValueAt(modelRow, 3).toString();
        String threshold  = productModel.getValueAt(modelRow, 4).toString();

        AddProductDialog dlg = new AddProductDialog(null,
                new String[]{name, category, price, stock, threshold});

        dlg.setOnSaveListener(data -> {
            model.Product p = new model.Product();
            p.setProductId(productId);
            p.setProductName(data[0]);
            p.setCategory(data[1].toLowerCase());
            p.setPrice(Double.parseDouble(data[2]));
            p.setStockQuantity(Integer.parseInt(data[3]));
            p.setLowStockThreshold(Integer.parseInt(data[4]));

            boolean saved = productDAO.updateProduct(p);
            if (saved) {
                refreshData();
            } else {
                javax.swing.JOptionPane.showMessageDialog(
                    ProductsPanel.this,
                    "Failed to update product. Check your connection.",
                    "Update Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
        dlg.setVisible(true);
    }

    // Delete selected product
    private void deleteSelected() {
        int viewRow = productTable.getSelectedRow();
        if (viewRow < 0) return;

        int    modelRow  = productTable.convertRowIndexToModel(viewRow);
        int    productId = getProductIdAtRow(modelRow);
        String name      = productModel.getValueAt(modelRow, 0).toString();

        int confirm = javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Delete \"" + name + "\"? This cannot be undone.",
            "Confirm Delete",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            boolean deleted = productDAO.deleteProduct(productId);
            if (deleted) {
                refreshData();
            } else {
                javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete product. It may be linked to"
                    + " existing transactions.",
                    "Delete Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------------------------------------------------------
    // Helper: product IDs are not shown in the table — track them
    // in a parallel list that gets rebuilt on every refreshData()
    // ---------------------------------------------------------------
    private java.util.List<Integer> productIdList = new java.util.ArrayList<>();

    private int getProductIdAtRow(int modelRow) {
        if (modelRow >= 0 && modelRow < productIdList.size()) {
            return productIdList.get(modelRow);
        }
        return -1;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase()
             + s.substring(1).toLowerCase();
    }

    // Reusable rounded button
    private JButton buildRoundedButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground()
                                        : new Color(0xBBBBBB));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(hover); btn.repaint();
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg); btn.repaint();
            }
        });
        return btn;
    }

    // Main testing code
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            javax.swing.JFrame f = new javax.swing.JFrame("Products Test");
            f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            f.setSize(1280, 720);
            f.setLocationRelativeTo(null);
            f.add(new ProductsPanel());
            f.setVisible(true);
        });
    }
}