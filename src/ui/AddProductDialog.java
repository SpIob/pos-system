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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class AddProductDialog extends JDialog {

    // Colors
    private static final Color NAVY       = new Color(0x1C3557);
    private static final Color GRAY_TEXT  = new Color(0x666666);
    private static final Color FIELD_BDR  = new Color(0xCCCCCC);
    private static final Color GREEN_BTN  = new Color(0x2E7D32);
    private static final Color GREEN_HOV  = new Color(0x1B5E20);
    private static final Color GRAY_BTN   = new Color(0xBBBBBB);
    private static final Color GRAY_HOV   = new Color(0xAAAAAA);

    // Components
    private JTextField          nameField;
    private JComboBox<String>   categoryCombo;
    private JTextField          priceField;
    private JSpinner            stockSpinner;
    private JSpinner            thresholdSpinner;

    // If non-null, dialog is in Edit mode
    private final String[] existingValues;

    // ---------------------------------------------------------------
    // Constructor
    // existingValues: {name, category, price, stock, threshold}
    // Pass null for Add mode, filled array for Edit mode
    // ---------------------------------------------------------------
    public AddProductDialog(Frame parent, String[] existingValues) {
        super(parent, true);
        this.existingValues = existingValues;

        setTitle(existingValues == null ? "Add New Product" : "Edit Product");
        setSize(480, 540);
        setMinimumSize(new Dimension(480, 540));
        setResizable(false);
        setLocationRelativeTo(parent);
        setModal(true);

        buildUI();

        if (existingValues != null) {
            populateFields();
        }
    }

    // Build UI
    private void buildUI() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.gridx     = 0;
        gbc.weightx   = 1.0;

        boolean isEdit = existingValues != null;

        // --- Title ---
        JLabel titleLabel = new JLabel(
                isEdit ? "Edit Product" : "Add New Product",
                SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(NAVY);
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        content.add(titleLabel, gbc);

        // --- Subtitle ---
        JLabel subtitle = new JLabel("Fill in all fields below.",
                SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitle.setForeground(GRAY_TEXT);
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        content.add(subtitle, gbc);

        // --- Separator ---
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(0xE0E0E0));
        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 0, 16, 0);
        content.add(sep, gbc);

        // --- Product Name ---
        content.add(fieldLabel("Product Name"), gridRow(gbc, 3, 0, 4));

        nameField = styledTextField("e.g. Coke 500ml");
        gbc.gridy  = 4;
        gbc.insets = new Insets(0, 0, 14, 0);
        content.add(nameField, gbc);

        // --- Category ---
        content.add(fieldLabel("Category"), gridRow(gbc, 5, 0, 4));

        categoryCombo = new JComboBox<>(
                new String[]{"Beverage", "Snack", "Other"});
        categoryCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        categoryCombo.setPreferredSize(new Dimension(0, 38));
        gbc.gridy  = 6;
        gbc.insets = new Insets(0, 0, 14, 0);
        content.add(categoryCombo, gbc);

        // --- Price ---
        content.add(fieldLabel("Price (₱)"), gridRow(gbc, 7, 0, 4));

        priceField = styledTextField("0.00");
        gbc.gridy  = 8;
        gbc.insets = new Insets(0, 0, 14, 0);
        content.add(priceField, gbc);

        // --- Stock Quantity ---
        content.add(fieldLabel("Stock Quantity"), gridRow(gbc, 9, 0, 4));

        stockSpinner = new JSpinner(
                new SpinnerNumberModel(0, 0, 99999, 1));
        stockSpinner.setFont(new Font("Arial", Font.PLAIN, 13));
        stockSpinner.setPreferredSize(new Dimension(0, 38));
        gbc.gridy  = 10;
        gbc.insets = new Insets(0, 0, 14, 0);
        content.add(stockSpinner, gbc);

        // --- Low-Stock Threshold ---
        content.add(fieldLabel("Low-Stock Threshold"), gridRow(gbc, 11, 0, 4));

        thresholdSpinner = new JSpinner(
                new SpinnerNumberModel(5, 0, 99999, 1));
        thresholdSpinner.setFont(new Font("Arial", Font.PLAIN, 13));
        thresholdSpinner.setPreferredSize(new Dimension(0, 38));
        gbc.gridy  = 12;
        gbc.insets = new Insets(0, 0, 4, 0);
        content.add(thresholdSpinner, gbc);

        JLabel hint = new JLabel(
                "An alert shows when stock falls below this number.");
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        hint.setForeground(GRAY_TEXT);
        gbc.gridy  = 13;
        gbc.insets = new Insets(0, 0, 20, 0);
        content.add(hint, gbc);

        // --- Buttons ---
        JPanel btnRow = new JPanel(new BorderLayout(12, 0));
        btnRow.setOpaque(false);

        JButton saveBtn   = buildRoundedButton("Save",
                GREEN_BTN, GREEN_HOV, Color.WHITE);
        JButton cancelBtn = buildRoundedButton("Cancel",
                GRAY_BTN,  GRAY_HOV,  Color.WHITE);

        saveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { handleSave(); }
        });
        cancelBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { dispose(); }
        });

        btnRow.add(saveBtn,   BorderLayout.CENTER);
        btnRow.add(cancelBtn, BorderLayout.EAST);

        gbc.gridy  = 14;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(btnRow, gbc);

        setContentPane(content);
    }

    // Pre-fill field edits for Edit Mode
    private void populateFields() {
        nameField.setText(existingValues[0]);
        nameField.setForeground(new Color(0x333333));

        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            if (categoryCombo.getItemAt(i)
                             .equalsIgnoreCase(existingValues[1])) {
                categoryCombo.setSelectedIndex(i);
                break;
            }
        }

        priceField.setText(existingValues[2]);
        priceField.setForeground(new Color(0x333333));

        stockSpinner.setValue(Integer.parseInt(existingValues[3]));
        thresholdSpinner.setValue(Integer.parseInt(existingValues[4]));
    }

    // Save handler
    private void handleSave() {
        String name  = nameField.getText().trim();
        String price = priceField.getText().trim();

        if (name.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Product name cannot be empty.",
                "Validation Error",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        try {
            double p = Double.parseDouble(price);
            if (p < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please enter a valid price (e.g. 25.00).",
                "Validation Error",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            priceField.requestFocus();
            return;
        }

        // All valid — close dialog
        // When DAOs are ready, call ProductDAO here before dispose()
        dispose();
    }

    // Helpers
    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(new Color(0x333333));
        return lbl;
    }

    private GridBagConstraints gridRow(GridBagConstraints gbc,
                                        int row, int top, int bottom) {
        gbc.gridy  = row;
        gbc.insets = new Insets(top, 0, bottom, 0);
        return gbc;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setForeground(new Color(0xAAAAAA));
        field.setText(placeholder);
        field.setPreferredSize(new Dimension(0, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BDR, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(0x333333));
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(0xAAAAAA));
                }
            }
        });
        return field;
    }

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