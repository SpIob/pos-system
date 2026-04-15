package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import dao.UserDAO;
import model.User;

public class LoginFrame extends JFrame {

    // Colors
    private static final Color BG_DARK    = new Color(0x1A2C4E);
    private static final Color BG_DARKER  = new Color(0x152340);
    private static final Color NAVY       = new Color(0x1C3557);
    private static final Color BLUE_BTN   = new Color(0x2D6DA8);
    private static final Color BLUE_HOVER = new Color(0x245A8E);
    private static final Color GRAY_TEXT  = new Color(0x888888);
    private static final Color FIELD_BORDER = new Color(0xCCCCCC);
    private static final Color PLACEHOLDER = new Color(0xAAAAAA);

    // Components
    private JLabel     logoLabel;
    private JLabel     titleLabel;
    private JLabel     subtitleLabel;
    private JSeparator divider;
    private JLabel     usernameLabel;
    private JTextField usernameField;
    private JLabel     passwordLabel;
    private JPasswordField passwordField;
    private JButton    loginButton;
    private JLabel     errorLabel;

    // Constructor
    public LoginFrame() {
        setTitle("ByteZone Café POS");
        setSize(1280, 720);
        setMinimumSize(new Dimension(1280, 720));
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();
    }

    // Manually build the full UI
    private void buildUI() {

        // --- Background panel (dark navy gradient) ---
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient: top-left darker, bottom-right slightly lighter
                g2.setPaint(new java.awt.GradientPaint(
                    0, 0, BG_DARKER,
                    getWidth(), getHeight(), BG_DARK
                ));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        // --- Card panel (white, rounded corners) ---
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(360, 460));
        cardPanel.setLayout(new GridBagLayout());

        // Build n add each component to cardPanel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // --- Logo ---
        java.net.URL logoUrl = getClass().getResource("/img/logo.png");
        if (logoUrl != null) {
            java.awt.Image rawImage = new javax.swing.ImageIcon(logoUrl).getImage();
            java.awt.Image scaledImage = rawImage.getScaledInstance(72, 72, java.awt.Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new javax.swing.ImageIcon(scaledImage), SwingConstants.CENTER);
        } else {
            logoLabel = new JLabel("BZ", SwingConstants.CENTER);
            logoLabel.setFont(new Font("Arial", Font.BOLD, 22));
            logoLabel.setForeground(new Color(0x7FA8C9));
        }
        logoLabel.setPreferredSize(new Dimension(72, 72));
        logoLabel.setOpaque(false);

        gbc.gridy = 0;
        gbc.insets = new Insets(32, 40, 12, 40);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        cardPanel.add(logoLabel, gbc);

        // --- App title ---
        titleLabel = new JLabel("ByteZone Café POS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(NAVY);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 4, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cardPanel.add(titleLabel, gbc);

        // --- Subtitle ---
        subtitleLabel = new JLabel("Your All-in-One Café Solution", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(GRAY_TEXT);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 40, 12, 40);
        cardPanel.add(subtitleLabel, gbc);

        // --- Divider ---
        divider = new JSeparator(SwingConstants.HORIZONTAL);
        divider.setForeground(new Color(0xE0E0E0));

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 40, 16, 40);
        cardPanel.add(divider, gbc);

        // --- Username label ---
        usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        usernameLabel.setForeground(new Color(0x333333));

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 40, 4, 40);
        cardPanel.add(usernameLabel, gbc);

        // --- Username field ---
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(280, 38));
        usernameField.setFont(new Font("Arial", Font.PLAIN, 13));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        addPlaceholder(usernameField, "Enter username");

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 40, 14, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cardPanel.add(usernameField, gbc);

        // --- Password label ---
        passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 13));
        passwordLabel.setForeground(new Color(0x333333));

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 40, 4, 40);
        cardPanel.add(passwordLabel, gbc);

        // --- Password field ---
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(280, 38));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        passwordField.setEchoChar('•');

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 40, 20, 40);
        cardPanel.add(passwordField, gbc);

        // --- Log In button ---
        loginButton = new JButton("Log In") {
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
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(BLUE_BTN);
        loginButton.setOpaque(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(280, 44));
        loginButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Hover effect
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(BLUE_HOVER);
                loginButton.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(BLUE_BTN);
                loginButton.repaint();
            }
        });

        gbc.gridy = 8;
        gbc.insets = new Insets(0, 40, 12, 40);
        cardPanel.add(loginButton, gbc);

        // --- Error label (hidden by default) ---
        errorLabel = new JLabel("Invalid username or password.", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(0xCC3333));
        errorLabel.setVisible(false);

        gbc.gridy = 9;
        gbc.insets = new Insets(0, 40, 24, 40);
        cardPanel.add(errorLabel, gbc);

        // Add card to background and background to frame
        backgroundPanel.add(cardPanel, new GridBagConstraints());
        setContentPane(backgroundPanel);
        
        // Wire login button to UserDAO
        loginButton.addActionListener(e -> handleLogin());
        // Also allow pressing Enter in the password field
        passwordField.addActionListener(e -> handleLogin());
    }
    
    // Login handler — replaces sample logic with real UserDAO call
    private void handleLogin() {
        String username = getUsername();
        String password = getPassword();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter your username and password.");
            return;
        }

        // Disable button while authenticating
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        // Run on a background thread so the UI doesn't freeze
        new Thread(() -> {
            dao.UserDAO userDAO = new dao.UserDAO();
            model.User user = userDAO.authenticate(username, password);

            // Update UI back on the Event Dispatch Thread
            java.awt.EventQueue.invokeLater(() -> {
                loginButton.setEnabled(true);
                loginButton.setText("Log In");

                if (user != null) {
                    hideError();
                    dispose();
                    new MainFrame(user).setVisible(true);
                } else {
                    showError("Invalid username or password.");
                    passwordField.setText("");
                    passwordField.requestFocus();
                }
            });
        }).start();
    }

    // Placeholder text
    private void addPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(PLACEHOLDER);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(0x333333));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER);
                }
            }
        });
    }

    // Temporary for login logic later
    public String getUsername() {
        String text = usernameField.getText();
        return text.equals("Enter username") ? "" : text;
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public void hideError() {
        errorLabel.setVisible(false);
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    // Main test code
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}