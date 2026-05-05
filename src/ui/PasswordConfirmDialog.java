package ui;

import dao.UserDAO;
import model.User;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PasswordConfirmDialog extends JDialog {

    private final User   currentUser;
    private final String actionLabel;
    private boolean      confirmed = false;
    private JPasswordField passwordField;
    private JLabel         errorLabel;

    public PasswordConfirmDialog(Frame parent, User currentUser, String actionLabel) {
        super(parent, true);
        this.currentUser = currentUser;
        this.actionLabel = actionLabel;
        setTitle("Confirm Identity");
        setSize(380, 300);
        setMinimumSize(new Dimension(380, 300));
        setResizable(false);
        setLocationRelativeTo(parent);
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.weightx = 1.0;

        JLabel title = new JLabel("Confirm Identity", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(UIHelper.NAVY);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 6, 0);
        content.add(title, gbc);

        JLabel sub = new JLabel("Enter your password to: " + actionLabel, SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(new Color(0x666666));
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 20, 0);
        content.add(sub, gbc);

        JLabel userLabel = new JLabel("Logged in as: " + currentUser.getUsername(), SwingConstants.CENTER);
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        userLabel.setForeground(UIHelper.NAVY);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 12, 0);
        content.add(userLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 13));
        passwordField.setPreferredSize(new Dimension(0, 38));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.FIELD_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        passwordField.addActionListener(e -> handleConfirm());
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 8, 0);
        content.add(passwordField, gbc);

        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(0xCC3333));
        errorLabel.setVisible(false);
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 16, 0);
        content.add(errorLabel, gbc);

        JButton confirmBtn = UIHelper.button("Confirm", UIHelper.NAVY_MID, UIHelper.NAVY, Color.WHITE);
        JButton cancelBtn  = UIHelper.button("Cancel",  UIHelper.GRAY,     UIHelper.GRAY_HOV, Color.WHITE);
        confirmBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleConfirm(); }
        });
        cancelBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { confirmed = false; dispose(); }
        });

        JPanel btnRow = new JPanel(new BorderLayout(10, 0));
        btnRow.setOpaque(false);
        btnRow.add(confirmBtn, BorderLayout.CENTER);
        btnRow.add(cancelBtn,  BorderLayout.EAST);
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 0, 0);
        content.add(btnRow, gbc);

        setContentPane(content);
        passwordField.requestFocusInWindow();
    }

    private void handleConfirm() {
        String entered = new String(passwordField.getPassword());
        if (entered.isEmpty()) { showError("Please enter your password."); return; }
        String hashed = UserDAO.sha256(entered);
        if (hashed != null && hashed.equals(currentUser.getPassword())) {
            confirmed = true; dispose();
        } else {
            showError("Incorrect password. Try again.");
            passwordField.setText(""); passwordField.requestFocus();
        }
    }

    private void showError(String msg) { errorLabel.setText(msg); errorLabel.setVisible(true); }

    public boolean isConfirmed() { return confirmed; }
}