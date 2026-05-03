package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.SwingWorker;

import dao.ProductDAO;
import dao.TransactionDAO;

public class SalesPanel extends JPanel {

    private final ProductDAO     productDAO     = new ProductDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private model.User           currentUser;

    public void setCurrentUser(model.User user) {
        this.currentUser = user;
    }

    private final java.util.Map<String, Integer> productIdMap = new java.util.HashMap<>();

    // Components
    private JPanel             productsGridPanel;
    private javax.swing.table.DefaultTableModel  orderModel;
    private JTable             orderTable;
    private JLabel             totalLabel;
    private JTextField         amountPaidField;
    private JLabel             changeLabel;
    private JButton            chargeButton;
    private JButton            clearCartButton;
    private JComboBox<String>  stationComboBox;

    // Constructor
    public SalesPanel() {
        setLayout(new BorderLayout(16, 0));
        setBackground(UIHelper.PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    // Build UI
    private void buildUI() {
        add(buildProductsPanel(), BorderLayout.CENTER);
        add(buildOrderPanel(),    BorderLayout.EAST);
    }

    // Products panel
    private JPanel buildProductsPanel() {
        JPanel panel = UIHelper.card(new BorderLayout(), 20);

        JLabel title = new JLabel("Products");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        panel.add(title, BorderLayout.NORTH);

        productsGridPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        productsGridPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(productsGridPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UIHelper.PAGE_BG);
        panel.add(scroll, BorderLayout.CENTER);

        loadProducts();
        return panel;
    }

    public void loadProducts() {
        new SwingWorker<java.util.List<model.Product>, Void>() {
            @Override
            protected java.util.List<model.Product> doInBackground() {
                return productDAO.getAllProducts();
            }

            @Override
            protected void done() {
                try {
                    java.util.List<model.Product> products = get();
                    productsGridPanel.removeAll();
                    for (model.Product p : products) {
                        productsGridPanel.add(buildProductButton(
                            p.getProductName(),
                            String.format("%.2f", p.getPrice()),
                            p.getProductId()
                        ));
                    }
                    productsGridPanel.revalidate();
                    productsGridPanel.repaint();
                    loadStationDropdown();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // Product button
    private JButton buildProductButton(String name, String price, int productId) {
        productIdMap.put(name, productId);

        String label = "<html><center><b>" + name + "</b><br>"
                     + "<font color='#2D6DA8'>\u20B1" + price + "</font></center></html>";

        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(0xDDDDDD));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.PLAIN, 13));
        btn.setBackground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 72));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(UIHelper.ROW_ALT); btn.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE); btn.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                addToCart(name, price);
            }
        });
        return btn;
    }

    // Order panel
    private JPanel buildOrderPanel() {
        JPanel panel = UIHelper.card(new BorderLayout(), 20);
        panel.setPreferredSize(new Dimension(520, 0));

        JLabel title = new JLabel("Current Order");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        panel.add(title, BorderLayout.NORTH);

        panel.add(buildOrderTable(),   BorderLayout.CENTER);
        panel.add(buildOrderActions(), BorderLayout.SOUTH);
        return panel;
    }

    // Order table
    private JScrollPane buildOrderTable() {
        String[] cols = {"Item", "Qty", "Unit Price", "Subtotal"};
        orderModel = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        orderTable = new JTable(orderModel);
        orderTable.setRowHeight(34);
        UIHelper.styleTable(orderTable);

        JScrollPane scroll = new JScrollPane(orderTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // Total + payment actions below table
    private JPanel buildOrderActions() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Total row
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(UIHelper.TOTAL_BG);
        totalRow.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        totalLabel = new JLabel("Total:   \u20B10.00");
        totalLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        totalLabel.setForeground(UIHelper.NAVY);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalRow.add(totalLabel, BorderLayout.EAST);

        // Amount paid
        JLabel amountLabel = new JLabel("Amount Paid");
        amountLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        amountLabel.setForeground(new Color(0x444444));

        amountPaidField = new JTextField();
        amountPaidField.setFont(new Font("Arial", Font.PLAIN, 14));
        amountPaidField.setPreferredSize(new Dimension(0, 40));
        amountPaidField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.FIELD_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        amountPaidField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { updateChange(); }
            @Override public void removeUpdate(DocumentEvent e)  { updateChange(); }
            @Override public void changedUpdate(DocumentEvent e) { updateChange(); }
        });

        // Change
        changeLabel = new JLabel("Change:   \u20B10.00");
        changeLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        changeLabel.setForeground(UIHelper.GREEN);

        // CHARGE button
        chargeButton    = UIHelper.button("CHARGE",     UIHelper.GREEN, UIHelper.GREEN_HOV, Color.WHITE, 48);
        chargeButton.setPreferredSize(new Dimension(0, 48));
        chargeButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleCharge(); }
        });

        // Clear Cart button
        clearCartButton = UIHelper.button("Clear Cart", UIHelper.GRAY,  UIHelper.GRAY_HOV,  Color.WHITE, 38);
        clearCartButton.setPreferredSize(new Dimension(0, 38));
        clearCartButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { clearCart(); }
        });

        // Link to station
        JLabel stationLabel = new JLabel("Link to Station (optional)");
        stationLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stationLabel.setForeground(new Color(0x666666));

        stationComboBox = new JComboBox<>(new String[]{"Select station..."});
        stationComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        stationComboBox.setPreferredSize(new Dimension(0, 36));

        // Stack everything
        JPanel stack = new JPanel(new BorderLayout(0, 8));
        stack.setOpaque(false);

        JPanel fields = new JPanel(new BorderLayout(0, 6));
        fields.setOpaque(false);
        fields.add(amountLabel,     BorderLayout.NORTH);
        fields.add(amountPaidField, BorderLayout.CENTER);
        fields.add(changeLabel,     BorderLayout.SOUTH);

        JPanel buttons = new JPanel(new BorderLayout(0, 8));
        buttons.setOpaque(false);
        buttons.add(chargeButton,    BorderLayout.NORTH);
        buttons.add(clearCartButton, BorderLayout.CENTER);

        JPanel stationSection = new JPanel(new BorderLayout(0, 4));
        stationSection.setOpaque(false);
        stationSection.add(stationLabel,    BorderLayout.NORTH);
        stationSection.add(stationComboBox, BorderLayout.CENTER);

        stack.add(fields,         BorderLayout.NORTH);
        stack.add(buttons,        BorderLayout.CENTER);
        stack.add(stationSection, BorderLayout.SOUTH);

        panel.add(totalRow, BorderLayout.NORTH);
        panel.add(stack,    BorderLayout.CENTER);
        return panel;
    }

    // Add item to cart
    private void addToCart(String name, String priceStr) {
        double price = Double.parseDouble(priceStr);

        for (int i = 0; i < orderModel.getRowCount(); i++) {
            if (orderModel.getValueAt(i, 0).equals(name)) {
                int qty = (int) orderModel.getValueAt(i, 1) + 1;
                orderModel.setValueAt(qty, i, 1);
                orderModel.setValueAt(String.format("\u20B1%.2f", price * qty), i, 3);
                updateTotal();
                return;
            }
        }

        orderModel.addRow(new Object[]{
            name, 1,
            String.format("\u20B1%.2f", price),
            String.format("\u20B1%.2f", price)
        });
        updateTotal();
    }

    // Update order total
    private void updateTotal() {
        double total = 0;
        for (int i = 0; i < orderModel.getRowCount(); i++) {
            String sub = orderModel.getValueAt(i, 3).toString()
                                   .replace("\u20B1", "").replace(",", "");
            total += Double.parseDouble(sub);
        }
        totalLabel.setText(String.format("Total:   \u20B1%.2f", total));
        updateChange();
    }

    // Update change
    private void updateChange() {
        try {
            double total  = parseTotal();
            double paid   = Double.parseDouble(amountPaidField.getText().trim());
            double change = paid - total;
            changeLabel.setText(String.format("Change:   \u20B1%.2f", change));
            changeLabel.setForeground(change >= 0 ? UIHelper.GREEN : UIHelper.RED);
        } catch (NumberFormatException ex) {
            changeLabel.setText("Change:   \u20B10.00");
            changeLabel.setForeground(UIHelper.GREEN);
        }
    }

    // Handle charge
    private void handleCharge() {
        if (orderModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Please add items to the order first.",
                "Empty Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total;
        double paid;
        try {
            total = parseTotal();
            paid  = Double.parseDouble(amountPaidField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid amount paid.",
                "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (paid < total) {
            JOptionPane.showMessageDialog(this,
                "Amount paid is less than the total.",
                "Insufficient Payment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (currentUser != null) ? currentUser.getUserId() : 1;

        model.Transaction txn = new model.Transaction();
        txn.setUserId(userId);
        txn.setTotalAmount(total);
        txn.setAmountPaid(paid);
        txn.setChangeGiven(paid - total);

        java.util.List<model.TransactionItem> items = new java.util.ArrayList<>();
        StringBuilder receiptLines = new StringBuilder();

        for (int i = 0; i < orderModel.getRowCount(); i++) {
            String itemName  = orderModel.getValueAt(i, 0).toString();
            int    qty       = (int) orderModel.getValueAt(i, 1);
            double unitPrice = Double.parseDouble(
                orderModel.getValueAt(i, 2).toString()
                          .replace("\u20B1", "").replace(",", ""));
            double subtotal  = qty * unitPrice;

            model.TransactionItem item = new model.TransactionItem();
            item.setProductId(productIdMap.getOrDefault(itemName, 0));
            item.setItemDescription(itemName);
            item.setQuantity(qty);
            item.setUnitPrice(unitPrice);
            item.setSubtotal(subtotal);
            items.add(item);

            receiptLines.append(itemName).append(" x").append(qty)
                        .append("  \u20B1").append(String.format("%.2f", subtotal))
                        .append("\n");
        }

        final double finalTotal = total;
        final double finalPaid  = paid;
        final String finalLines = receiptLines.toString();

        chargeButton.setEnabled(false);
        chargeButton.setText("Processing...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                int newId = transactionDAO.saveTransaction(txn, items);
                if (newId > 0) {
                    for (model.TransactionItem item : items) {
                        if (item.getProductId() > 0)
                            productDAO.reduceStock(item.getProductId(), item.getQuantity());
                    }
                }
                return newId;
            }

            @Override
            protected void done() {
                chargeButton.setEnabled(true);
                chargeButton.setText("CHARGE");
                try {
                    int newTxnId = get();
                    if (newTxnId < 0) {
                        JOptionPane.showMessageDialog(SalesPanel.this,
                            "Transaction could not be saved. Check your connection.",
                            "Save Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    new ReceiptDialog(null,
                        String.format("TXN-%04d", newTxnId),
                        finalLines, finalTotal, finalPaid).setVisible(true);
                    clearCart();
                    loadProducts();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(SalesPanel.this,
                        "An unexpected error occurred.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Station dropdown loader
    private void loadStationDropdown() {
        java.util.List<model.Station> available =
                new dao.StationDAO().getAvailableStations();
        stationComboBox.removeAllItems();
        stationComboBox.addItem("Select station...");
        for (model.Station s : available)
            stationComboBox.addItem(s.getStationName());
    }

    // Clear cart
    private void clearCart() {
        orderModel.setRowCount(0);
        totalLabel.setText("Total:   \u20B10.00");
        amountPaidField.setText("");
        changeLabel.setText("Change:   \u20B10.00");
        changeLabel.setForeground(UIHelper.GREEN);
    }

    // Parse total
    private double parseTotal() {
        String raw = totalLabel.getText()
                               .replace("Total:", "")
                               .replace("\u20B1", "")
                               .replace(",", "")
                               .trim();
        try { return Double.parseDouble(raw); }
        catch (NumberFormatException e) { return 0; }
    }
}