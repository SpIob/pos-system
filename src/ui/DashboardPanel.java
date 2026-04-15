package ui;

import dao.ProductDAO;
import dao.SessionDAO;
import dao.StationDAO;
import dao.TransactionDAO;
import model.Product;
import model.Station;
import model.Transaction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
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

    // DAOs
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final StationDAO     stationDAO     = new StationDAO();
    private final SessionDAO     sessionDAO     = new SessionDAO();
    private final ProductDAO     productDAO     = new ProductDAO();

    // KPI labels
    private JLabel todaysSalesValue;
    private JLabel activeStationsValue;
    private JLabel lowStockValue;
    private JLabel activeSessionsValue;

    // Tables
    private JTable            recentTxnTable;
    private DefaultTableModel recentTxnModel;

    // Station overview
    private JPanel stationListPanel;

    // Date formatter for the transactions table
    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("MMM d hh:mm a");

    // Constructor
    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    // Build UI structure (no data yet)
    private void buildUI() {
        add(buildStatsRow(),     BorderLayout.NORTH);
        add(buildCenterSection(), BorderLayout.CENTER);
    }

    // KPI stat cards row
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel salesCard = buildStatCard("Loading...", "Today's Sales",
                GREEN_VAL, new Color(0x2E7D32));
        todaysSalesValue = getValueLabel(salesCard);

        JPanel stationsCard = buildStatCard("...", "Active Stations",
                BLUE_VAL, new Color(0x1C3557));
        activeStationsValue = getValueLabel(stationsCard);

        JPanel lowStockCard = buildStatCard("...", "Low Stock Items",
                ORANGE_VAL, new Color(0xFF8F00));
        lowStockValue = getValueLabel(lowStockCard);

        JPanel sessionsCard = buildStatCard("...", "Active Sessions",
                BLUE_VAL, new Color(0x1C3557));
        activeSessionsValue = getValueLabel(sessionsCard);

        row.add(salesCard);
        row.add(stationsCard);
        row.add(lowStockCard);
        row.add(sessionsCard);
        return row;
    }

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
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valLabel.setForeground(valueColor);

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

    private JLabel getValueLabel(JPanel card) {
        JPanel textStack = (JPanel) card.getComponent(0);
        return (JLabel) textStack.getComponent(0);
    }

    // Center section
    private JPanel buildCenterSection() {
        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(buildTransactionsPanel(), BorderLayout.CENTER);
        center.add(buildStationOverviewPanel(), BorderLayout.EAST);
        return center;
    }

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

        JLabel title = new JLabel("Recent Transactions");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"TXN #", "Date & Time", "Items", "Total", "Cashier"};
        recentTxnModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        recentTxnTable = new JTable(recentTxnModel);
        styleTable(recentTxnTable);

        // TXN # column blue
        recentTxnTable.getColumnModel().getColumn(0)
                .setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc,
                    int row, int col) {
                super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                setForeground(sel ? Color.WHITE : TXN_BLUE);
                setBackground(sel ? NAVY_DARK :
                        (row % 2 == 0 ? ROW_NORMAL : ROW_ALT));
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(recentTxnTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

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

        JLabel title = new JLabel("Station Overview");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(title, BorderLayout.NORTH);

        stationListPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        stationListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(stationListPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------------
    // refreshData() — call this every time the tab is shown
    // Loads all data from DAOs on a background thread
    // ---------------------------------------------------------------
    public void refreshData() {
        new SwingWorker<Void, Void>() {

            double         todayTotal;
            int            occupiedCount;
            int            totalStations;
            int            lowStockCount;
            int            activeSessionCount;
            java.util.List<Object[]> richRows;
            java.util.List<Station>  stations;

            @Override
            protected Void doInBackground() {
                todayTotal         = transactionDAO.getTodayTotal();
                occupiedCount      = stationDAO.getOccupiedCount();
                totalStations      = stationDAO.getAllStations().size();
                lowStockCount      = productDAO.getLowStockCount();
                activeSessionCount = sessionDAO.getActiveSessionCount();
                richRows           = transactionDAO
                                         .getRecentTransactionsRich(5);
                stations           = stationDAO.getAllStations();
                return null;
            }

            @Override
            protected void done() {
                // ---- KPI cards ----
                todaysSalesValue.setText(
                        String.format("₱%,.2f", todayTotal));
                activeStationsValue.setText(
                        occupiedCount + " / " + totalStations);
                lowStockValue.setText(
                        lowStockCount > 0 ? lowStockCount + " ⚠" : "0");
                activeSessionsValue.setText(
                        String.valueOf(activeSessionCount));

                // ---- Transactions table ----
                recentTxnModel.setRowCount(0);
                for (Object[] row : richRows) {
                    int    txnId     = (int)    row[0];
                    java.sql.Timestamp date =
                            (java.sql.Timestamp) row[1];
                    int    itemCount = (int)    row[2];
                    double total     = (double) row[3];
                    String username  = (String) row[4];

                    String itemLabel = itemCount == 1
                            ? "1 item" : itemCount + " items";
                    String dateStr   = DATE_FMT.format(date);

                    recentTxnModel.addRow(new Object[]{
                        String.format("TXN-%04d", txnId),
                        dateStr,
                        itemLabel,
                        String.format("₱%,.2f", total),
                        username != null ? username : "—"
                    });
                }

                // ---- Station overview ----
                stationListPanel.removeAll();
                for (Station s : stations) {
                    stationListPanel.add(
                            buildStationRow(s.getStationName(),
                                            s.getStatus()));
                }
                stationListPanel.revalidate();
                stationListPanel.repaint();
            }
        }.execute();
    }

    // One row in the station overview list
    private JPanel buildStationRow(String name, String status) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

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
            default:
                dotColor   = STATUS_GREEN;
                textColor  = STATUS_GREEN;
                statusText = "Available";
                break;
        }

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Arial", Font.PLAIN, 14));
        dot.setForeground(dotColor);
        dot.setPreferredSize(new Dimension(20, 20));

        JLabel nameLabel = new JLabel(name);
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

    // Table styling
    private void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(NAVY_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setSelectionBackground(NAVY_DARK);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);

        table.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc,
                    int row, int col) {
                super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                if (sel) {
                    setBackground(NAVY_DARK);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? ROW_NORMAL : ROW_ALT);
                    setForeground(new Color(0x333333));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                setFont(new Font("Arial", Font.PLAIN, 13));
                return this;
            }
        });
    }

    // Main — testing
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            javax.swing.JFrame f =
                    new javax.swing.JFrame("Dashboard Test");
            f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            f.setSize(1280, 720);
            f.setLocationRelativeTo(null);
            DashboardPanel panel = new DashboardPanel();
            f.add(panel);
            f.setVisible(true);
            panel.refreshData();
        });
    }
}