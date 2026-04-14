package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class ReceiptDialog extends JDialog {

    // Colors
    private static final Color NAVY      = new Color(0x1C3557);
    private static final Color GRAY_TEXT = new Color(0x888888);
    private static final Color GREEN_CHG = new Color(0x2E7D32);
    private static final Color NAVY_BTN  = new Color(0x1C3557);
    private static final Color NAVY_HOV  = new Color(0x152340);
    private static final Color GRAY_BTN  = new Color(0xBBBBBB);
    private static final Color GRAY_HOV  = new Color(0xAAAAAA);

    // Receipt
    private final String txnNumber;
    private final String itemLines;
    private final double total;
    private final double amountPaid;
    private JTextArea receiptArea;

    // Constructor
    public ReceiptDialog(Frame parent, String txnNumber,
                          String itemLines, double total, double amountPaid) {
        super(parent, true);
        this.txnNumber  = txnNumber;
        this.itemLines  = itemLines;
        this.total      = total;
        this.amountPaid = amountPaid;

        setTitle("Receipt");
        setSize(420, 580);
        setMinimumSize(new Dimension(420, 580));
        setResizable(false);
        setLocationRelativeTo(parent);
        setModal(true);

        buildUI();
    }

    // Build UI
    private void buildUI() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridx   = 0;
        gbc.weightx = 1.0;

        // --- Monitor icon ---
        JLabel iconLabel = new JLabel("🖥", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        content.add(iconLabel, gbc);

        // --- Café name ---
        JLabel cafeNameLabel = new JLabel("ByteZone Café POS",
                SwingConstants.CENTER);
        cafeNameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        cafeNameLabel.setForeground(NAVY);
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 2, 0);
        content.add(cafeNameLabel, gbc);

        // --- Café address ---
        JLabel addressLabel = new JLabel("Quezon City, Metro Manila",
                SwingConstants.CENTER);
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        addressLabel.setForeground(GRAY_TEXT);
        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        content.add(addressLabel, gbc);

        // --- Receipt body (monospaced text area) ---
        receiptArea = new JTextArea();
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        receiptArea.setText(buildReceiptText());

        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 10, 0);
        content.add(receiptArea, gbc);

        // --- Thank you message ---
        JLabel thankYou = new JLabel(
                "Thank you for visiting ByteZone!",
                SwingConstants.CENTER);
        thankYou.setFont(new Font("Arial", Font.ITALIC, 12));
        thankYou.setForeground(GRAY_TEXT);
        gbc.gridy  = 4;
        gbc.insets = new Insets(0, 0, 16, 0);
        content.add(thankYou, gbc);

        // --- Buttons ---
        JPanel btnRow = new JPanel(new BorderLayout(12, 0));
        btnRow.setOpaque(false);

        JButton printBtn = buildRoundedButton(
                "🖨  Print Receipt", NAVY_BTN, NAVY_HOV, Color.WHITE);
        JButton closeBtn = buildRoundedButton(
                "✕  Close", GRAY_BTN, GRAY_HOV, Color.WHITE);

        printBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { handlePrint(); }
        });
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { dispose(); }
        });

        btnRow.add(printBtn, BorderLayout.CENTER);
        btnRow.add(closeBtn, BorderLayout.EAST);

        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(btnRow, gbc);

        setContentPane(content);
    }

    // Build receipt text
    private String buildReceiptText() {
        String date = new SimpleDateFormat("MMMM d, yyyy   hh:mm a")
                          .format(new Date());
        double change = amountPaid - total;

        StringBuilder sb = new StringBuilder();
        sb.append(dashes()).append("\n");
        sb.append("Transaction #: ").append(txnNumber).append("\n");
        sb.append("Date: ").append(date).append("\n");
        sb.append(dashes()).append("\n");
        sb.append("\n");

        // Item lines — pad with dots between name and price
        for (String line : itemLines.split("\n")) {
            if (!line.trim().isEmpty()) {
                sb.append(formatItemLine(line)).append("\n");
            }
        }

        sb.append("\n");
        sb.append(dashes()).append("\n");
        sb.append(String.format("%-18s %10s%n", "TOTAL:",
                String.format("₱%.2f", total)));
        sb.append(String.format("%-18s %10s%n", "Amount Paid:",
                String.format("₱%.2f", amountPaid)));
        sb.append(String.format("%-18s %10s%n", "Change:",
                String.format("₱%.2f", change)));
        sb.append(dashes()).append("\n");

        return sb.toString();
    }

    // Receipt format
    private String formatItemLine(String line) {
        // Expected format: "Name xQty  ₱Subtotal"
        int lastSpace = line.lastIndexOf("  ");
        if (lastSpace < 0) return line;

        String left  = line.substring(0, lastSpace).trim();
        String right = line.substring(lastSpace).trim();
        int    dots  = 30 - left.length() - right.length();
        if (dots < 1) dots = 1;

        return left + " " + ".".repeat(dots) + " " + right;
    }

    private String dashes() {
        return "-".repeat(38);
    }

    // Print handler
    private void handlePrint() {
        try {
            receiptArea.print();
        } catch (PrinterException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Printing failed: " + ex.getMessage(),
                "Print Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // Reusable rounded button
    private JButton buildRoundedButton(String text,
            Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hover); btn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg); btn.repaint();
            }
        });
        return btn;
    }
}