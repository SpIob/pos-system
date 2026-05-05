package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AddProductDialog extends JDialog {

    private JTextField        nameField;
    private JComboBox<String> categoryCombo;
    private JTextField        priceField;
    private JSpinner          stockSpinner;
    private final String[]    existingValues;

    public interface OnSaveListener { void onSave(String[] data); }
    private OnSaveListener onSaveListener;
    public void setOnSaveListener(OnSaveListener l) { this.onSaveListener = l; }

    public AddProductDialog(Frame parent, String[] existingValues) {
        super(parent, true);
        this.existingValues = existingValues;
        setTitle(existingValues == null ? "Add New Product" : "Edit Product");
        setSize(480, 540);
        setMinimumSize(new Dimension(480, 540));
        setResizable(false);
        setLocationRelativeTo(parent);
        buildUI();
        if (existingValues != null) populateFields();
    }

    private void buildUI() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.weightx = 1.0;

        boolean isEdit = existingValues != null;

        JLabel titleLabel = new JLabel(isEdit ? "Edit Product" : "Add New Product", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(UIHelper.NAVY);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
        content.add(titleLabel, gbc);

        JLabel subtitle = new JLabel("Fill in all fields below.", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitle.setForeground(UIHelper.GRAY_LABEL);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 10, 0);
        content.add(subtitle, gbc);

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(0xE0E0E0));
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 16, 0);
        content.add(sep, gbc);

        nameField = styledTextField("e.g. Coke 500ml");
        UIHelper.formRow(content, gbc, 3, fieldLabel("Product Name"), nameField);

        categoryCombo = new JComboBox<>(new String[]{"Beverage", "Snack", "Other"});
        categoryCombo.setFont(UIHelper.FONT_PLAIN_MD);
        categoryCombo.setPreferredSize(new Dimension(0, 38));
        UIHelper.formRow(content, gbc, 5, fieldLabel("Category"), categoryCombo);

        priceField = styledTextField("0.00");
        UIHelper.formRow(content, gbc, 7, fieldLabel("Price (\u20B1)"), priceField);

        stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
        stockSpinner.setFont(UIHelper.FONT_PLAIN_MD);
        stockSpinner.setPreferredSize(new Dimension(0, 38));
        UIHelper.formRow(content, gbc, 9, fieldLabel("Stock Quantity"), stockSpinner);

        JPanel btnRow = new JPanel(new BorderLayout(12, 0));
        btnRow.setOpaque(false);
        JButton saveBtn   = UIHelper.button("Save",   UIHelper.GREEN, UIHelper.GREEN_HOV, Color.WHITE);
        JButton cancelBtn = UIHelper.button("Cancel", UIHelper.GRAY,  UIHelper.GRAY_HOV,  Color.WHITE);
        saveBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleSave(); }
        });
        cancelBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dispose(); }
        });
        btnRow.add(saveBtn, BorderLayout.CENTER);
        btnRow.add(cancelBtn, BorderLayout.EAST);
        gbc.gridy = 11; gbc.insets = new Insets(0, 0, 0, 0);
        content.add(btnRow, gbc);

        setContentPane(content);
    }

    private void populateFields() {
        nameField.setText(existingValues[0]);
        nameField.setForeground(new Color(0x333333));
        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            if (categoryCombo.getItemAt(i).equalsIgnoreCase(existingValues[1])) {
                categoryCombo.setSelectedIndex(i); break;
            }
        }
        priceField.setText(existingValues[2]);
        priceField.setForeground(new Color(0x333333));
        stockSpinner.setValue(Integer.parseInt(existingValues[3]));
    }

    private void handleSave() {
        String name     = nameField.getText().trim();
        String price    = priceField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();

        if (name.isEmpty() || name.equals("e.g. Coke 500ml")) {
            JOptionPane.showMessageDialog(this, "Product name cannot be empty.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus(); return;
        }
        try {
            double p = Double.parseDouble(price);
            if (p < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid price (e.g. 25.00).",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            priceField.requestFocus(); return;
        }
        if (onSaveListener != null)
            onSaveListener.onSave(new String[]{name, category, price,
                String.valueOf((int) stockSpinner.getValue())});
        dispose();
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(new Color(0x333333));
        return lbl;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setForeground(new Color(0xAAAAAA));
        field.setText(placeholder);
        field.setPreferredSize(new Dimension(0, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.FIELD_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText(""); field.setForeground(new Color(0x333333));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder); field.setForeground(new Color(0xAAAAAA));
                }
            }
        });
        return field;
    }
}