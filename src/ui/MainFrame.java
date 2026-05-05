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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import ui.DashboardPanel;
import ui.StationsPanel;
import ui.SalesPanel;
import ui.ProductsPanel;
import ui.ReportsPanel;

public class MainFrame extends JFrame {

    // Colors
    private static final Color NAVY_DARK    = new Color(0x1C3557);
    private static final Color NAVY_MID     = new Color(0x2D5F8A);
    private static final Color TAB_BG       = new Color(0xF5F5F5);
    private static final Color TAB_SELECTED = new Color(0x1C3557);
    private static final Color LOGOUT_BG    = new Color(0x2D5F8A);
    private static final Color LOGOUT_HOVER = new Color(0x1C3557);
    private static final Color BADGE_BG     = new Color(0x2D5F8A);

    // Components
    private JPanel       topBar;
    private JLabel       appTitleLabel;
    private JLabel       loggedUserLabel;
    private JLabel       roleBadgeLabel;
    private JButton      logoutButton;
    private JTabbedPane  mainTabbedPane;

    // Tab panels
    private DashboardPanel dashboardPanel;
    private StationsPanel stationsPanel;
    private SalesPanel salesPanel;
    private ProductsPanel productsPanel;
    private ReportsPanel reportsPanel;

    // The logged-in user info (set via constructor)
    private model.User currentUser;

    // Constructor
    public MainFrame(model.User user) {
        this.currentUser = user;

        setTitle("ByteZone Café POS");
        setSize(1280, 720);
        setMinimumSize(new Dimension(1280, 720));
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                database.DBConnection.shutdown();
                System.exit(0);
            }
        });

        buildUI();
    }

    // Build Full UI
    private void buildUI() {
        setLayout(new BorderLayout());

        buildTopBar();
        buildTabbedPane();
    }

    // Top navbar
    private void buildTopBar() {
        topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(NAVY_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topBar.setPreferredSize(new Dimension(1280, 52));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // --- Left side: logo icon + app title ---
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        // Small monitor icon drawn in code
        JLabel monitorIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Monitor body
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(1, 1, 20, 14, 3, 3);
                // Screen area
                g2.setColor(NAVY_DARK);
                g2.fillRoundRect(3, 3, 16, 10, 2, 2);
                // Stand
                g2.setColor(Color.WHITE);
                g2.fillRect(9, 15, 4, 3);
                g2.fillRect(6, 17, 10, 2);
            }
        };
        monitorIcon.setPreferredSize(new Dimension(22, 20));
        monitorIcon.setOpaque(false);

        appTitleLabel = new JLabel("ByteZone Café POS");
        appTitleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        appTitleLabel.setForeground(Color.WHITE);

        leftPanel.add(monitorIcon);
        leftPanel.add(appTitleLabel);

        // --- Right side: username + role badge + logout button ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        loggedUserLabel = new JLabel(currentUser.getUsername());
        loggedUserLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
        loggedUserLabel.setForeground(Color.WHITE);

        // Role badge (rounded pill)
        roleBadgeLabel = new JLabel(capitalize(currentUser.getRole())) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BADGE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        roleBadgeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        roleBadgeLabel.setForeground(Color.WHITE);
        roleBadgeLabel.setOpaque(false);
        roleBadgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleBadgeLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

        // Logout button
        logoutButton = new JButton("Logout") {
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
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 12));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(LOGOUT_BG);
        logoutButton.setOpaque(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(LOGOUT_HOVER);
                logoutButton.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(LOGOUT_BG);
                logoutButton.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                handleLogout();
            }
        });

        rightPanel.add(loggedUserLabel);    
        // "Users" button — visible only to admin
        if (currentUser.isAdmin()) {
            JButton usersBtn = UIHelper.button("Users",
                    UIHelper.NAVY_MID, UIHelper.NAVY, Color.WHITE);
            usersBtn.addActionListener(e ->
                new UserManagementDialog(MainFrame.this, currentUser)
                        .setVisible(true));
            rightPanel.add(usersBtn);
        }
        rightPanel.add(roleBadgeLabel);
        rightPanel.add(logoutButton);

        // Center the left/right panels vertically inside topBar
        JPanel topBarInner = new JPanel(new BorderLayout());
        topBarInner.setOpaque(false);
        topBarInner.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        topBarInner.add(leftPanel,  BorderLayout.WEST);
        topBarInner.add(rightPanel, BorderLayout.EAST);

        topBar.add(topBarInner, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);
    }

    // Tab bar & tabbed pane
    private void buildTabbedPane() {

        // Thin separator line between topBar and tabs
        JPanel separatorLine = new JPanel();
        separatorLine.setBackground(new Color(0xDDDDDD));
        separatorLine.setPreferredSize(new Dimension(1280, 1));

        // Tab strip background panel
        JPanel tabStrip = new JPanel(new BorderLayout());
        tabStrip.setBackground(Color.WHITE);
        tabStrip.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                           new Color(0xE0E0E0)));

        // The actual JTabbedPane
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        mainTabbedPane.setBackground(Color.WHITE);
        mainTabbedPane.setFont(new Font("Arial", Font.PLAIN, 13));
        mainTabbedPane.setFocusable(false);

        // Remove the default ugly border around tab content
        mainTabbedPane.setBorder(BorderFactory.createEmptyBorder());

        // Style the tab pane via UI properties
        mainTabbedPane.putClientProperty("JTabbedPane.tabAreaBackground",
                                          Color.WHITE);

        // Panels for each tab
        dashboardPanel = new DashboardPanel();
        stationsPanel  = new StationsPanel();
        salesPanel     = new SalesPanel();
        productsPanel  = new ProductsPanel();
        reportsPanel   = new ReportsPanel();

        // Add tabs (index order matters for role-based access above)
        mainTabbedPane.addTab("Dashboard", dashboardPanel);  // index 0
        mainTabbedPane.addTab("Stations",  stationsPanel);   // index 1
        mainTabbedPane.addTab("Sales",     salesPanel);      // index 2
        mainTabbedPane.addTab("Products", productsPanel); // index 3
        mainTabbedPane.addTab("Reports",  reportsPanel);  // index 4
        
        // Refresh data every time a tab becomes visible
        mainTabbedPane.addChangeListener(e -> {
            int selected = mainTabbedPane.getSelectedIndex();
            switch (selected) {
                case 0:
                    dashboardPanel.refreshData();
                    break;
                case 1:
                    stationsPanel.refreshData();
                    break;
                case 2:
                    salesPanel.loadProducts();
                    break;
                case 3:
                    productsPanel.refreshData();
                    updateTabBadges();
                    break;
                case 4:
                    reportsPanel.refreshData();
                    updateTabBadges();
                    break;
            }
        });

        // Also pass the current user down to other panels
        productsPanel.setCurrentUser(currentUser);
        reportsPanel.setCurrentUser(currentUser);
        salesPanel.setCurrentUser(currentUser);
        stationsPanel.setCurrentUser(currentUser);

        // Load dashboard immediately on open
        dashboardPanel.refreshData();
        updateTabBadges();

        // Custom tab renderer for active underline indicator
        mainTabbedPane.addChangeListener(e -> mainTabbedPane.repaint());

        // Wrap pane in a panel with light gray content background
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(new Color(0xF5F5F5));
        contentWrapper.add(separatorLine, BorderLayout.NORTH);
        contentWrapper.add(mainTabbedPane, BorderLayout.CENTER);

        add(contentWrapper, BorderLayout.CENTER);
    }


    // Logout Handler
    private void handleLogout() {
        int choice = javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out?",
            "Confirm Logout",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE
        );
        if (choice == javax.swing.JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // Utility
    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    // Swap placeholder panels for real ones
    public JTabbedPane getMainTabbedPane() {
        return mainTabbedPane;
    }

    public void setTabPanel(int index, JPanel panel) {
        String title = mainTabbedPane.getTitleAt(index);
        mainTabbedPane.setComponentAt(index, panel);
        mainTabbedPane.setTitleAt(index, title);
    }

    // Main test code
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            model.User testUser = new model.User(1, "admin", "", "admin", null);
            new MainFrame(testUser).setVisible(true);
        });
    }
    
    // ---------------------------------------------------------------
    // Updates the Products and Reports tab titles based on live
    // low-stock count. Call this after any product or sale change.
    // ---------------------------------------------------------------
    public void updateTabBadges() {
        new Thread(() -> {
            int count = new dao.ProductDAO().getLowStockCount();
            java.awt.EventQueue.invokeLater(() -> {
                String badge = count > 0 ? " (!)" : "";
                mainTabbedPane.setTitleAt(3, "Products" + badge);
                mainTabbedPane.setTitleAt(4, "Reports"  + badge);
            });
        }).start();
    }
}