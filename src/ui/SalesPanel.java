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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class SalesPanel extends JPanel {

    // Colors
    private static final Color PAGE_BG     = new Color(0xF5F5F5);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color NAVY_DARK   = new Color(0x1C3557);
    private static final Color BLUE_TEXT   = new Color(0x2D6DA8);
    private static final Color GREEN_BTN   = new Color(0x2E7D32);
    private static final Color GREEN_HOVER = new Color(0x1B5E20);
    private static final Color GRAY_BTN    = new Color(0xBBBBBB);
    private static final Color GRAY_HOVER  = new Color(0xAAAAAA);
    private static final Color ROW_ALT     = new Color(0xF0F4F8);
    private static final Color TOTAL_BG    = new Color(0xEEF4FA);

    // Sample Products
    private static final String[][] PRODUCTS = {
        {"Coke 500ml",    "25.00"},
        {"Mineral Water", "15.00"},
        {"Iced Coffee",   "35.00"},
        {"Chips (Regular)","20.00"},
        {"Cup Noodles",   "15.00"},
        {"Chocolate Bar", "18.00"},
        {"Headset Rental","10.00"},
        {"USB (per use)", "5.00"},
    };

    // Components
    private DefaultTableModel orderModel;
    private JTable            orderTable;
    private JLabel            totalLabel;
    private JTextField        amountPaidField;
    private JLabel            changeLabel;
    private JButton           chargeButton;
    private JButton           clearCartButton;
    private JComboBox<String> stationComboBox;

    // Constructor
    public SalesPanel() {
        setLayout(new BorderLayout(16, 0));
        setBackground(PAGE_BG);
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

        JLabel title = new JLabel("Products");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        panel.add(title, BorderLayout.NORTH);

        // Grid of product buttons — 3 columns
        JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
        grid.setOpaque(false);

        for (String[] product : PRODUCTS) {
            grid.add(buildProductButton(product[0], product[1]));
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_BG);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // Product button
    private JButton buildProductButton(String name, String price) {
        // HTML label for two-line layout
        String label = "<html><center><b>" + name + "</b><br>"
                     + "<font color='#2D6DA8'>₱" + price + "</font></center></html>";

        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Subtle border
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
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0xF0F4F8));
                btn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                addToCart(name, price);
            }
        });

        return btn;
    }

    // Order panel
    private JPanel buildOrderPanel() {
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
        panel.setPreferredSize(new Dimension(520, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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
        orderModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        orderTable = new JTable(orderModel);

        // Header
        JTableHeader header = orderTable.getTableHeader();
        header.setBackground(NAVY_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        orderTable.setFont(new Font("Arial", Font.PLAIN, 13));
        orderTable.setRowHeight(34);
        orderTable.setShowGrid(false);
        orderTable.setIntercellSpacing(new Dimension(0, 0));
        orderTable.setSelectionBackground(NAVY_DARK);
        orderTable.setSelectionForeground(Color.WHITE);
        orderTable.setFillsViewportHeight(true);
        orderTable.setFocusable(false);

        // Alternating rows
        orderTable.setDefaultRenderer(Object.class,
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
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                return this;
            }
        });

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
        totalRow.setBackground(TOTAL_BG);
        totalRow.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        totalLabel = new JLabel("Total:   ₱0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(NAVY_DARK);
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
            BorderFactory.createLineBorder(new Color(0xCCCCCC), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // Auto-calculate change as user types
        amountPaidField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { updateChange(); }
            @Override public void removeUpdate(DocumentEvent e)  { updateChange(); }
            @Override public void changedUpdate(DocumentEvent e) { updateChange(); }
        });

        // Change
        changeLabel = new JLabel("Change:   ₱0.00");
        changeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        changeLabel.setForeground(new Color(0x2E7D32));

        // CHARGE button
        chargeButton = buildRoundedButton("✓   CHARGE",
                GREEN_BTN, GREEN_HOVER, Color.WHITE, 48);
        chargeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCharge();
            }
        });

        // Clear Cart button
        clearCartButton = buildRoundedButton("✕   Clear Cart",
                GRAY_BTN, GRAY_HOVER, Color.WHITE, 38);
        clearCartButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clearCart();
            }
        });

        // Link to station
        JLabel stationLabel = new JLabel("Link to Station (optional)");
        stationLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stationLabel.setForeground(new Color(0x666666));

        stationComboBox = new JComboBox<>(new String[]{
            "Select station...",
            "PC-01", "PC-02", "PC-03", "PC-04",
            "VIP-01", "VIP-02"
        });
        stationComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        stationComboBox.setPreferredSize(new Dimension(0, 36));

        // Stack everything
        JPanel stack = new JPanel(new BorderLayout(0, 8));
        stack.setOpaque(false);

        JPanel fields = new JPanel(new BorderLayout(0, 6));
        fields.setOpaque(false);
        fields.add(amountLabel,    BorderLayout.NORTH);
        fields.add(amountPaidField, BorderLayout.CENTER);
        fields.add(changeLabel,    BorderLayout.SOUTH);

        JPanel buttons = new JPanel(new BorderLayout(0, 8));
        buttons.setOpaque(false);
        buttons.add(chargeButton,     BorderLayout.NORTH);
        buttons.add(clearCartButton,  BorderLayout.CENTER);

        JPanel stationSection = new JPanel(new BorderLayout(0, 4));
        stationSection.setOpaque(false);
        stationSection.add(stationLabel,   BorderLayout.NORTH);
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

        // Check if item already in cart — increment qty
        for (int i = 0; i < orderModel.getRowCount(); i++) {
            if (orderModel.getValueAt(i, 0).equals(name)) {
                int qty = (int) orderModel.getValueAt(i, 1);
                qty++;
                orderModel.setValueAt(qty, i, 1);
                orderModel.setValueAt(
                    String.format("₱%.2f", price * qty), i, 3);
                updateTotal();
                return;
            }
        }

        // New item
        orderModel.addRow(new Object[]{
            name, 1,
            String.format("₱%.2f", price),
            String.format("₱%.2f", price)
        });
        updateTotal();
    }

    // Update order total
    private void updateTotal() {
        double total = 0;
        for (int i = 0; i < orderModel.getRowCount(); i++) {
            String subtotalStr = orderModel.getValueAt(i, 3)
                                           .toString()
                                           .replace("₱", "")
                                           .replace(",", "");
            total += Double.parseDouble(subtotalStr);
        }
        totalLabel.setText(String.format("Total:   ₱%.2f", total));
        updateChange();
    }

    // Update change
    private void updateChange() {
        try {
            double total = parseTotal();
            double paid  = Double.parseDouble(
                    amountPaidField.getText().trim());
            double change = paid - total;
            changeLabel.setText(String.format("Change:   ₱%.2f", change));
            changeLabel.setForeground(change >= 0
                    ? new Color(0x2E7D32)
                    : new Color(0xC62828));
        } catch (NumberFormatException ex) {
            changeLabel.setText("Change:   ₱0.00");
            changeLabel.setForeground(new Color(0x2E7D32));
        }
    }

    // Handle charge (open ReceiptDialog)
    private void handleCharge() {
        if (orderModel.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please add items to the order first.",
                "Empty Order",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = parseTotal();
        double paid;
        try {
            paid = Double.parseDouble(amountPaidField.getText().trim());
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please enter a valid amount paid.",
                "Invalid Amount",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (paid < total) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Amount paid is less than the total.",
                "Insufficient Payment",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build receipt lines from order table
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < orderModel.getRowCount(); i++) {
            String item     = orderModel.getValueAt(i, 0).toString();
            int    qty      = (int) orderModel.getValueAt(i, 1);
            String subtotal = orderModel.getValueAt(i, 3).toString();
            lines.append(item).append(" x").append(qty)
                 .append("  ").append(subtotal).append("\n");
        }

        ReceiptDialog receipt = new ReceiptDialog(
            null,
            "TXN-" + String.format("%04d", (int)(Math.random() * 9999)),
            lines.toString(),
            total,
            paid
        );
        receipt.setVisible(true);

        // Clear cart after successful charge
        clearCart();
    }

    // Clear cart
    private void clearCart() {
        orderModel.setRowCount(0);
        totalLabel.setText("Total:   ₱0.00");
        amountPaidField.setText("");
        changeLabel.setText("Change:   ₱0.00");
        changeLabel.setForeground(new Color(0x2E7D32));
    }

    // Parse total
    private double parseTotal() {
        String raw = totalLabel.getText()
                               .replace("Total:", "")
                               .replace("₱", "")
                               .replace(",", "")
                               .trim();
        try { return Double.parseDouble(raw); }
        catch (NumberFormatException e) { return 0; }
    }

    // Reusable rounded button
    private JButton buildRoundedButton(String text,
            Color bg, Color hover, Color fg, int height) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : GRAY_BTN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, height));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(hover);
                    btn.repaint();
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
                btn.repaint();
            }
        });

        return btn;
    }

    // Main testing code
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            javax.swing.JFrame f = new javax.swing.JFrame("Sales Test");
            f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            f.setSize(1280, 720);
            f.setLocationRelativeTo(null);
            f.add(new SalesPanel());
            f.setVisible(true);
        });
    }
}