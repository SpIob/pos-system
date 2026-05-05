package ui;

import java.awt.*;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class ReceiptDialog extends JDialog {

    private JTextArea receiptArea;

    public ReceiptDialog(Frame parent, String txnNumber, String itemLines,
                          double total, double amountPaid) {
        super(parent, true);
        setTitle("Receipt — " + txnNumber);
        setSize(420, 580);
        setMinimumSize(new Dimension(420, 580));
        setResizable(false);
        setLocationRelativeTo(parent);
        buildUI(txnNumber, itemLines, total, amountPaid);
    }

    private void buildUI(String txnNumber, String itemLines, double total, double amountPaid) {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 1.0;

        java.net.URL logoUrl = getClass().getResource("/img/logo.png");
        JLabel icon;
        if (logoUrl != null) {
            java.awt.Image img = new ImageIcon(logoUrl).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            icon = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
        } else {
            icon = new JLabel("BZ", SwingConstants.CENTER);
            icon.setFont(new Font("Dialog", Font.BOLD, 18));
            icon.setForeground(UIHelper.NAVY);
        }
        g.gridy = 0; g.insets = new Insets(0, 0, 6, 0);
        content.add(icon, g);

        JLabel name = new JLabel("ByteZone Café POS", SwingConstants.CENTER);
        name.setFont(new Font("Arial", Font.BOLD, 15));
        name.setForeground(UIHelper.NAVY);
        g.gridy = 1; g.insets = new Insets(0, 0, 2, 0);
        content.add(name, g);

        JLabel addr = new JLabel("Quezon City, Metro Manila", SwingConstants.CENTER);
        addr.setFont(new Font("Arial", Font.PLAIN, 11));
        addr.setForeground(UIHelper.GRAY);
        g.gridy = 2; g.insets = new Insets(0, 0, 10, 0);
        content.add(addr, g);

        receiptArea = new JTextArea(buildReceiptText(txnNumber, itemLines, total, amountPaid));
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setBorder(BorderFactory.createEmptyBorder());
        g.gridy = 3; g.insets = new Insets(0, 0, 8, 0);
        content.add(receiptArea, g);

        JLabel thanks = new JLabel("Thank you for visiting ByteZone!", SwingConstants.CENTER);
        thanks.setFont(new Font("Arial", Font.ITALIC, 12));
        thanks.setForeground(UIHelper.GRAY);
        g.gridy = 4; g.insets = new Insets(0, 0, 16, 0);
        content.add(thanks, g);

        JPanel btnRow = new JPanel(new BorderLayout(10, 0));
        btnRow.setOpaque(false);
        JButton printBtn = new JButton("Print Receipt");
        printBtn.setBackground(UIHelper.NAVY);
        printBtn.setForeground(Color.WHITE);
        printBtn.setFocusPainted(false);
        printBtn.addActionListener(e -> {
            try { receiptArea.print(); }
            catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage());
            }
        });
        JButton closeBtn = new JButton("Close");
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());
        btnRow.add(printBtn, BorderLayout.CENTER);
        btnRow.add(closeBtn, BorderLayout.EAST);
        g.gridy = 5; g.insets = new Insets(0, 0, 0, 0);
        content.add(btnRow, g);

        setContentPane(content);
    }

    private String buildReceiptText(String txnNumber, String itemLines,
                                     double total, double amountPaid) {
        String date = new SimpleDateFormat("MMMM d, yyyy   hh:mm a").format(new Date());
        double change = amountPaid - total;
        StringBuilder sb = new StringBuilder();
        sb.append(dashes()).append("\n");
        sb.append("Transaction #: ").append(txnNumber).append("\n");
        sb.append("Date: ").append(date).append("\n");
        sb.append(dashes()).append("\n\n");
        for (String line : itemLines.split("\n"))
            if (!line.trim().isEmpty()) sb.append(padLine(line)).append("\n");
        sb.append("\n").append(dashes()).append("\n");
        sb.append(String.format("%-18s %10s%n", "TOTAL:",        String.format("₱%.2f", total)));
        sb.append(String.format("%-18s %10s%n", "Amount Paid:",  String.format("₱%.2f", amountPaid)));
        sb.append(String.format("%-18s %10s%n", "Change:",       String.format("₱%.2f", change)));
        sb.append(dashes()).append("\n");
        return sb.toString();
    }

    private String padLine(String line) {
        int last = line.lastIndexOf("  ");
        if (last < 0) return line;
        String left = line.substring(0, last).trim(), right = line.substring(last).trim();
        return left + " " + ".".repeat(Math.max(1, 30 - left.length() - right.length())) + " " + right;
    }

    private String dashes() { return "-".repeat(38); }
}