package ui;

import dao.UserDAO;
import model.User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * UserManagementDialog.java
 * Admin-only dialog for creating accounts and changing passwords.
 * Accessible from the MainFrame top bar when logged in as admin.
 */
public class UserManagementDialog extends JDialog {

    private final UserDAO         userDAO = new UserDAO();
    private final User            currentUser;

    // Table
    private JTable            userTable;
    private DefaultTableModel userModel;
    private List<Integer>     userIdList = new java.util.ArrayList<>();

    // Add User form fields
    private JTextField         newUsernameField;
    private JPasswordField     newPasswordField;
    private JPasswordField     confirmPasswordField;
    private JComboBox<String>  roleCombo;

    // Change Password form fields
    private JPasswordField  chgNewField;
    private JPasswordField  chgConfirmField;

    // Right-side panel that swaps between Add and Change modes
    private JPanel          formPanel;
    private JPanel          addPanel;
    private JPanel          chgPanel;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public UserManagementDialog(Frame parent, User currentUser) {
        super(parent, true);
        this.currentUser = currentUser;

        setTitle("User Management");
        setSize(780, 480);
        setMinimumSize(new Dimension(780, 480));
        setResizable(false);
        setLocationRelativeTo(parent);
        buildUI();
        loadUsers();
    }

    // ---------------------------------------------------------------
    // Main layout: left = user table, right = forms
    // ---------------------------------------------------------------
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        root.add(buildTablePanel(), BorderLayout.CENTER);
        root.add(buildFormsPanel(), BorderLayout.EAST);

        setContentPane(root);
    }

    // ---------------------------------------------------------------
    // Left panel: user table + action buttons
    // ---------------------------------------------------------------
    private JPanel buildTablePanel() {
        JPanel panel = UIHelper.card(new BorderLayout(0, 10), 16);
        panel.setPreferredSize(new Dimension(380, 0));

        JLabel title = new JLabel("Existing Accounts");
        title.setFont(UIHelper.FONT_BOLD_MED);
        title.setForeground(UIHelper.NAVY);
        panel.add(title, BorderLayout.NORTH);

        // Table
        userModel = new DefaultTableModel(
                new String[]{"ID", "Username", "Role"}, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        userTable = new JTable(userModel);
        UIHelper.styleTable(userTable);
        userTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        userTable.getColumnModel().getColumn(0)
                .setMaxWidth(40);

        JScrollPane scroll = new JScrollPane(userTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        // Buttons below table
        JPanel btns = new JPanel(new BorderLayout(8, 0));
        btns.setOpaque(false);

        JButton addBtn = UIHelper.button("Add User",
                UIHelper.GREEN, UIHelper.GREEN_HOV, Color.WHITE);
        JButton chgBtn = UIHelper.button("Change Password",
                UIHelper.BLUE, UIHelper.BLUE_HOV, Color.WHITE);
        JButton closeBtn = UIHelper.button("Close",
                UIHelper.GRAY, UIHelper.GRAY_HOV, Color.WHITE);

        addBtn.addActionListener(e -> showAddPanel());
        chgBtn.addActionListener(e -> showChangePanel());
        closeBtn.addActionListener(e -> dispose());

        btns.add(addBtn,  BorderLayout.WEST);
        btns.add(chgBtn,  BorderLayout.CENTER);
        btns.add(closeBtn, BorderLayout.EAST);
        panel.add(btns, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------------------------------------------------------
    // Right panel: two swappable forms
    // ---------------------------------------------------------------
    private JPanel buildFormsPanel() {
        formPanel = new JPanel(new java.awt.CardLayout());
        formPanel.setPreferredSize(new Dimension(320, 0));
        formPanel.setOpaque(false);

        addPanel = buildAddForm();
        chgPanel = buildChangeForm();

        formPanel.add(addPanel, "ADD");
        formPanel.add(chgPanel, "CHG");

        showAddPanel(); // default view
        return formPanel;
    }

    // ---------------------------------------------------------------
    // Add User form
    // ---------------------------------------------------------------
    private JPanel buildAddForm() {
        JPanel panel = UIHelper.card(new GridBagLayout(), 20);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.weightx = 1.0;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        JLabel title = new JLabel("Add New User");
        title.setFont(UIHelper.FONT_BOLD_LG);
        title.setForeground(UIHelper.NAVY);
        panel.add(title, gbc);

        newUsernameField = styledField("Enter username");
        UIHelper.formRow(panel, gbc, 1,
                fieldLabel("Username"), newUsernameField);

        newPasswordField = new JPasswordField();
        stylePassField(newPasswordField);
        UIHelper.formRow(panel, gbc, 3,
                fieldLabel("Password"), newPasswordField);

        confirmPasswordField = new JPasswordField();
        stylePassField(confirmPasswordField);
        UIHelper.formRow(panel, gbc, 5,
                fieldLabel("Confirm Password"), confirmPasswordField);

        roleCombo = new JComboBox<>(new String[]{"cashier", "admin"});
        roleCombo.setFont(UIHelper.FONT_PLAIN_MD);
        roleCombo.setPreferredSize(new Dimension(0, 36));
        UIHelper.formRow(panel, gbc, 7,
                fieldLabel("Role"), roleCombo);

        JButton saveBtn = UIHelper.button("Create Account",
                UIHelper.GREEN, UIHelper.GREEN_HOV, Color.WHITE);
        gbc.gridy = 9;
        gbc.insets = new Insets(8, 0, 0, 0);
        saveBtn.addActionListener(e -> handleAddUser());
        panel.add(saveBtn, gbc);

        return panel;
    }

    // ---------------------------------------------------------------
    // Change Password form
    // ---------------------------------------------------------------
    private JPanel buildChangeForm() {
        JPanel panel = UIHelper.card(new GridBagLayout(), 20);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.weightx = 1.0;

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 6, 0);
        JLabel title = new JLabel("Change Password");
        title.setFont(UIHelper.FONT_BOLD_LG);
        title.setForeground(UIHelper.NAVY);
        panel.add(title, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 16, 0);
        JLabel sub = new JLabel(
                "Select a user from the table first.");
        sub.setFont(UIHelper.FONT_PLAIN_SM);
        sub.setForeground(UIHelper.GRAY_LABEL);
        panel.add(sub, gbc);

        chgNewField = new JPasswordField();
        stylePassField(chgNewField);
        UIHelper.formRow(panel, gbc, 2,
                fieldLabel("New Password"), chgNewField);

        chgConfirmField = new JPasswordField();
        stylePassField(chgConfirmField);
        UIHelper.formRow(panel, gbc, 4,
                fieldLabel("Confirm New Password"), chgConfirmField);

        JButton saveBtn = UIHelper.button("Update Password",
                UIHelper.BLUE, UIHelper.BLUE_HOV, Color.WHITE);
        gbc.gridy = 6; gbc.insets = new Insets(8, 0, 0, 0);
        saveBtn.addActionListener(e -> handleChangePassword());
        panel.add(saveBtn, gbc);

        return panel;
    }

    // ---------------------------------------------------------------
    // Show Add panel
    // ---------------------------------------------------------------
    private void showAddPanel() {
        ((java.awt.CardLayout) formPanel.getLayout())
                .show(formPanel, "ADD");
    }

    // ---------------------------------------------------------------
    // Show Change Password panel
    // ---------------------------------------------------------------
    private void showChangePanel() {
        if (userTable.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this,
                "Select a user from the table first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ((java.awt.CardLayout) formPanel.getLayout())
                .show(formPanel, "CHG");
    }

    // ---------------------------------------------------------------
    // Load users from DB into table
    // ---------------------------------------------------------------
    private void loadUsers() {
        new SwingWorker<List<User>, Void>() {
            @Override protected List<User> doInBackground() {
                return userDAO.getAllUsers();
            }
            @Override protected void done() {
                try {
                    List<User> users = get();
                    userModel.setRowCount(0);
                    userIdList.clear();
                    for (User u : users) {
                        userIdList.add(u.getUserId());
                        userModel.addRow(new Object[]{
                            u.getUserId(),
                            u.getUsername(),
                            UIHelper.capitalize(u.getRole())
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Handle Add User
    // ---------------------------------------------------------------
    private void handleAddUser() {
        String username = newUsernameField.getText().trim();
        String password = new String(newPasswordField.getPassword());
        String confirm  = new String(confirmPasswordField.getPassword());
        String role     = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || username.equals("Enter username")) {
            warn("Username cannot be empty."); return;
        }
        if (password.isEmpty()) {
            warn("Password cannot be empty."); return;
        }
        if (!password.equals(confirm)) {
            warn("Passwords do not match."); return;
        }
        if (password.length() < 6) {
            warn("Password must be at least 6 characters."); return;
        }

        // Admin creating another admin — require password confirm
        if ("admin".equals(role)) {
            PasswordConfirmDialog pwd = new PasswordConfirmDialog(
                    null, currentUser, "Create Admin Account");
            pwd.setVisible(true);
            if (!pwd.isConfirmed()) return;
        }

        final String u = username, p = password, r = role;
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return userDAO.addUser(u, p, r);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                            UserManagementDialog.this,
                            "Account created for: " + u,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        clearAddForm();
                        loadUsers();
                    } else {
                        warn("Username already exists.");
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Handle Change Password
    // ---------------------------------------------------------------
    private void handleChangePassword() {
        int viewRow = userTable.getSelectedRow();
        if (viewRow < 0) { warn("Select a user first."); return; }

        String newPass     = new String(chgNewField.getPassword());
        String confirmPass = new String(chgConfirmField.getPassword());

        if (newPass.isEmpty()) {
            warn("New password cannot be empty."); return;
        }
        if (!newPass.equals(confirmPass)) {
            warn("Passwords do not match."); return;
        }
        if (newPass.length() < 6) {
            warn("Password must be at least 6 characters."); return;
        }

        int    userId   = userIdList.get(viewRow);
        String username = userModel.getValueAt(viewRow, 1).toString();

        // Require current admin password before changing anyone's password
        PasswordConfirmDialog pwd = new PasswordConfirmDialog(
                null, currentUser,
                "Change password for " + username);
        pwd.setVisible(true);
        if (!pwd.isConfirmed()) return;

        final String np = newPass;
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return userDAO.updatePassword(userId, np);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                            UserManagementDialog.this,
                            "Password updated for: " + username,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        chgNewField.setText("");
                        chgConfirmField.setText("");
                    } else {
                        warn("Failed to update password.");
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private void clearAddForm() {
        newUsernameField.setText("Enter username");
        newUsernameField.setForeground(UIHelper.PLACEHOLDER);
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        roleCombo.setSelectedIndex(0);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIHelper.FONT_BOLD_SM);
        lbl.setForeground(new Color(0x333333));
        return lbl;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(UIHelper.FONT_PLAIN_MD);
        f.setForeground(UIHelper.PLACEHOLDER);
        f.setText(placeholder);
        f.setPreferredSize(new Dimension(0, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCCCCCC), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText(""); f.setForeground(new Color(0x333333));
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(UIHelper.PLACEHOLDER);
                }
            }
        });
        return f;
    }

    private void stylePassField(JPasswordField f) {
        f.setFont(UIHelper.FONT_PLAIN_MD);
        f.setPreferredSize(new Dimension(0, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCCCCCC), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }
}