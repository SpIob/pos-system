package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class DashboardPanel extends JPanel {

    // Colors
    private static final Color NAVY_DARK    = new Color(0x1C3557);
    private static final Color PAGE_BG      = new Color(0xF5F5F5);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color GREEN_VAL    = new Color(0x2E7D32);
    private static final Color BLUE_VAL     = new Color(0x1C3557);
    private static final Color ORANGE_VAL   = new Color(0xE65100);
    private static final Color GRAY_LABEL   = new Color(0x777777);
    private static final Color TXN_BLUE     = new Color(0x2D6DA8);
    private static final Color ROW_ALT      = new Color(0xF0F4F8);
    private static final Color ROW_NORMAL   = Color.WHITE;
    private static final Color STATUS_GREEN = new Color(0x2E7D32);
    private static final Color STATUS_RED   = new Color(0xC62828);
    private static final Color STATUS_GRAY  = new Color(0x888888);

    // KPI lavel values
    private JLabel todaysSalesValue;
    private JLabel activeStationsValue;
    private JLabel lowStockValue;
    private JLabel activeSessionsValue;

    // Tables
    private JTable           recentTxnTable;
    private DefaultTableModel recentTxnModel;

    // Station overview panel
    private JPanel stationListPanel;

    // Constructor
    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadSampleData();
    }

    // Build full UI
    private void buildUI() {
        // Top: 4 KPI stat cards
        add(buildStatsRow(), BorderLayout.NORTH);

        // Center: transactions (left) + station overview (right)
        add(buildCenterSection(), BorderLayout.CENTER);
    }

    // KPI stat cards row
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Card 1 — Today's Sales (green accent)
        JPanel salesCard = buildStatCard("₱0.00", "Today's Sales",
                                          GREEN_VAL,  new Color(0x2E7D32));
        todaysSalesValue = getValueLabel(salesCard);

        // Card 2 — Active Stations (blue accent)
        JPanel stationsCard = buildStatCard("0 / 7", "Active Stations",
                                             BLUE_VAL, new Color(0x1C3557));
        activeStationsValue = getValueLabel(stationsCard);

        // Card 3 — Low Stock Items (orange accent)
        JPanel lowStockCard = buildStatCard("0 ⚠", "Low Stock Items",
                                             ORANGE_VAL, new Color(0xFF8F00));
        lowStockValue = getValueLabel(lowStockCard);

        // Card 4 — Active Sessions (dark blue accent)
        JPanel sessionsCard = buildStatCard("0", "Active Sessions",
                                             BLUE_VAL, new Color(0x1C3557));
        activeSessionsValue = getValueLabel(sessionsCard);

        row.add(salesCard);
        row.add(stationsCard);
        row.add(lowStockCard);
        row.add(sessionsCard);

        return row;
    }

    // ---------------------------------------------------------------
    // Builds one KPI card
    // accentColor = left border color
    // valueColor  = color of the large number
    // ---------------------------------------------------------------
    private JPanel buildStatCard(String value, String labelText,
                                  Color valueColor, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout()) {
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
        card.setOpaque(false);
        // Left colored accent border
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        // Value label (large)
        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valLabel.setForeground(valueColor);
        valLabel.putClientProperty("isValueLabel", Boolean.TRUE);

        // Description label (small, gray)
        JLabel descLabel = new JLabel(labelText);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(GRAY_LABEL);

        JPanel textStack = new JPanel(new BorderLayout(0, 4));
        textStack.setOpaque(false);
        textStack.add(valLabel,  BorderLayout.NORTH);
        textStack.add(descLabel, BorderLayout.SOUTH);

        card.add(textStack, BorderLayout.CENTER);
        return card;
    }

    // Retrieve value JLabel from stat card
    private JLabel getValueLabel(JPanel card) {
        // The value label is inside the textStack inside the card
        JPanel textStack = (JPanel) card.getComponent(0);
        return (JLabel) textStack.getComponent(0);
    }

    // Transactions table + overview
    private JPanel buildCenterSection() {
        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);

        center.add(buildTransactionsPanel(), BorderLayout.CENTER);
        center.add(buildStationOverviewPanel(), BorderLayout.EAST);

        return center;
    }

    // Recent transactions
    private JPanel buildTransactionsPanel() {
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

        // Section title
        JLabel title = new JLabel("Recent Transactions");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(title, BorderLayout.NORTH);

        // Table model — not editable
        String[] columns = {"TXN #", "Date & Time", "Items", "Total", "Cashier"};
        recentTxnModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        recentTxnTable = new JTable(recentTxnModel);
        styleTable(recentTxnTable);

        // TXN # column: blue text
        recentTxnTable.getColumnModel().getColumn(0)
            .setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int col) {
                    super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, col);
                    setForeground(isSelected ? Color.WHITE : TXN_BLUE);
                    setBackground(isSelected ? NAVY_DARK :
                                  (row % 2 == 0 ? ROW_NORMAL : ROW_ALT));
                    setFont(new Font("Arial", Font.PLAIN, 13));
                    setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                    return this;
                }
            });

        JScrollPane scrollPane = new JScrollPane(recentTxnTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // Station Overview panel
    private JPanel buildStationOverviewPanel() {
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
        panel.setPreferredSize(new Dimension(260, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Section title
        JLabel title = new JLabel("Station Overview");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(title, BorderLayout.NORTH);

        // Station rows container
        stationListPanel = new JPanel();
        stationListPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 4));
        stationListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(stationListPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------------
    // Builds one station row for the overview
    // status: "available", "occupied", "maintenance"
    // ---------------------------------------------------------------
    private JPanel buildStationRow(String stationName, String status) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        // Colored dot
        Color dotColor;
        Color textColor;
        String statusText;

        switch (status.toLowerCase()) {
            case "occupied":
                dotColor   = STATUS_RED;
                textColor  = STATUS_RED;
                statusText = "Occupied";
                break;
            case "maintenance":
                dotColor   = STATUS_GRAY;
                textColor  = STATUS_GRAY;
                statusText = "Maintenance";
                break;
            default: // available
                dotColor   = STATUS_GREEN;
                textColor  = STATUS_GREEN;
                statusText = "Available";
                break;
        }

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Arial", Font.PLAIN, 14));
        dot.setForeground(dotColor);
        dot.setPreferredSize(new Dimension(20, 20));

        JLabel nameLabel = new JLabel(stationName);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        nameLabel.setForeground(new Color(0x222222));

        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(textColor);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left.setOpaque(false);
        left.add(dot);
        left.add(nameLabel);

        row.add(left,        BorderLayout.WEST);
        row.add(statusLabel, BorderLayout.EAST);

        return row;
    }

    // Style each table
    private void styleTable(JTable table) {
        // Header
        JTableHeader header = table.getTableHeader();
        header.setBackground(NAVY_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        // Rows
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setSelectionBackground(NAVY_DARK);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);

        // Alternating row colors + cell padding
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, col);
                if (isSelected) {
                    setBackground(NAVY_DARK);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? ROW_NORMAL : ROW_ALT);
                    setForeground(new Color(0x333333));
                }
                setFont(new Font("Arial", Font.PLAIN, 13));
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                return this;
            }
        });
    }

    // Load sample data (will replace with real data later)
    private void loadSampleData() {
        // KPI cards
        todaysSalesValue.setText("₱2,340.00");
        activeStationsValue.setText("3 / 7");
        lowStockValue.setText("2 ⚠");
        activeSessionsValue.setText("3");

        // Recent transactions
        Object[][] txns = {
            {"TXN-0042", "Apr 9 10:34 AM", "3 items", "₱105.00", "admin"},
            {"TXN-0041", "Apr 9 10:10 AM", "2 items", "₱55.00",  "cashier1"},
            {"TXN-0040", "Apr 9 09:55 AM", "5 items", "₱195.00", "cashier1"},
            {"TXN-0039", "Apr 9 09:30 AM", "1 item",  "₱20.00",  "admin"},
            {"TXN-0038", "Apr 9 09:05 AM", "4 items", "₱130.00", "cashier1"},
        };
        for (Object[] row : txns) {
            recentTxnModel.addRow(row);
        }

        // Station overview
        String[][] stations = {
            {"PC-01",  "available"},
            {"PC-02",  "occupied"},
            {"PC-03",  "occupied"},
            {"PC-04",  "available"},
            {"PC-05",  "maintenance"},
            {"VIP-01", "available"},
            {"VIP-02", "occupied"},
        };
        for (String[] s : stations) {
            stationListPanel.add(buildStationRow(s[0], s[1]));
        }
    }

    // ---------------------------------------------------------------
    // refreshData() — call this when tab becomes visible
    // Replace sample data with real DAO calls here later
    // ---------------------------------------------------------------
    public void refreshData() {
        // Example (uncomment when DAOs are ready):
        // recentTxnModel.setRowCount(0);
        // TransactionDAO txnDao = new TransactionDAO(DBConnection.getConnection());
        // for (Transaction t : txnDao.getRecentTransactions(5)) {
        //     recentTxnModel.addRow(new Object[]{...});
        // }
    }

    // Main test code
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            javax.swing.JFrame f = new javax.swing.JFrame("Dashboard Test");
            f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            f.setSize(1280, 720);
            f.setLocationRelativeTo(null);
            f.add(new DashboardPanel());
            f.setVisible(true);
        });
    }
}