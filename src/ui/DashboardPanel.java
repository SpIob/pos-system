package ui;

import dao.ProductDAO;
import dao.SessionDAO;
import dao.StationDAO;
import dao.TransactionDAO;
import model.Station;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final StationDAO     stationDAO     = new StationDAO();
    private final SessionDAO     sessionDAO     = new SessionDAO();
    private final ProductDAO     productDAO     = new ProductDAO();

    private JLabel todaysSalesValue, activeStationsValue, lowStockValue, activeSessionsValue;
    private JTable recentTxnTable;
    private DefaultTableModel recentTxnModel;
    private JPanel stationListPanel;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM d hh:mm a");

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(buildStatsRow(),      BorderLayout.NORTH);
        add(buildCenterSection(), BorderLayout.CENTER);
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel salesCard = UIHelper.statCard("Loading...", "Today's Sales", UIHelper.GREEN, new Color(0x2E7D32));
        todaysSalesValue = UIHelper.getValueLabel(salesCard);
        todaysSalesValue.setFont(new Font("Dialog", Font.BOLD, 26));

        JPanel stationsCard = UIHelper.statCard("...", "Active Stations", UIHelper.BLUE, new Color(0x1C3557));
        activeStationsValue = UIHelper.getValueLabel(stationsCard);

        JPanel lowStockCard = UIHelper.statCard("...", "Low Stock Items", UIHelper.AMBER_TXT, new Color(0xFF8F00));
        lowStockValue = UIHelper.getValueLabel(lowStockCard);

        JPanel sessionsCard = UIHelper.statCard("...", "Active Sessions", UIHelper.BLUE, new Color(0x1C3557));
        activeSessionsValue = UIHelper.getValueLabel(sessionsCard);

        row.add(salesCard); row.add(stationsCard); row.add(lowStockCard); row.add(sessionsCard);
        return row;
    }

    private JPanel buildCenterSection() {
        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(buildTransactionsPanel(), BorderLayout.CENTER);
        center.add(buildStationOverviewPanel(), BorderLayout.EAST);
        return center;
    }

    private JPanel buildTransactionsPanel() {
        JPanel panel = UIHelper.card(new BorderLayout(), 20);

        JLabel title = new JLabel("Recent Transactions");
        title.setFont(new Font("Dialog", Font.BOLD, 14));
        title.setForeground(new Color(0x222222));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(title, BorderLayout.NORTH);

        recentTxnModel = new DefaultTableModel(new String[]{"TXN #", "Date & Time", "Items", "Total", "Cashier"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        recentTxnTable = new JTable(recentTxnModel);
        UIHelper.styleTable(recentTxnTable);

        recentTxnTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setForeground(sel ? Color.WHITE : UIHelper.TXN_BLUE);
                setBackground(sel ? UIHelper.NAVY : (row % 2 == 0 ? UIHelper.CARD_BG : UIHelper.ROW_ALT));
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
        JPanel panel = UIHelper.card(new BorderLayout(), 20);
        panel.setPreferredSize(new Dimension(260, 0));

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

    public void refreshData() {
        new SwingWorker<Void, Void>() {
            double todayTotal; int occupiedCount, totalStations, lowStockCount, activeSessionCount;
            java.util.List<Object[]> richRows;
            java.util.List<Station>  stations;

            @Override protected Void doInBackground() {
                todayTotal         = transactionDAO.getTodayTotal();
                occupiedCount      = stationDAO.getOccupiedCount();
                totalStations      = stationDAO.getAllStations().size();
                lowStockCount      = productDAO.getLowStockCount();
                activeSessionCount = sessionDAO.getActiveSessionCount();
                richRows           = transactionDAO.getRecentTransactionsRich(5);
                stations           = stationDAO.getAllStations();
                return null;
            }

            @Override protected void done() {
                todaysSalesValue.setText(String.format("\u20B1%,.2f", todayTotal));
                activeStationsValue.setText(occupiedCount + " / " + totalStations);
                lowStockValue.setText(lowStockCount > 0 ? lowStockCount + " ⚠" : "0");
                activeSessionsValue.setText(String.valueOf(activeSessionCount));

                recentTxnModel.setRowCount(0);
                for (Object[] row : richRows) {
                    int txnId = (int) row[0];
                    java.sql.Timestamp date = (java.sql.Timestamp) row[1];
                    int itemCount = (int) row[2]; double total = (double) row[3];
                    String username = (String) row[4];
                    recentTxnModel.addRow(new Object[]{
                        String.format("TXN-%04d", txnId), DATE_FMT.format(date),
                        itemCount == 1 ? "1 item" : itemCount + " items",
                        String.format("₱%,.2f", total),
                        username != null ? username : "—"});
                }

                stationListPanel.removeAll();
                for (Station s : stations)
                    stationListPanel.add(buildStationRow(s.getStationName(), s.getStatus()));
                stationListPanel.revalidate();
                stationListPanel.repaint();
            }
        }.execute();
    }

    private JPanel buildStationRow(String name, String status) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        Color dotColor; String statusText;
        switch (status.toLowerCase()) {
            case "occupied":    dotColor = UIHelper.RED;         statusText = "Occupied"; break;
            case "maintenance": dotColor = UIHelper.STATUS_GRAY; statusText = "Maintenance"; break;
            default:            dotColor = UIHelper.OK_COLOR;    statusText = "Available"; break;
        }

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Dialog", Font.PLAIN, 14));
        dot.setForeground(dotColor);
        dot.setPreferredSize(new Dimension(20, 20));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        nameLabel.setForeground(new Color(0x222222));

        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(dotColor);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left.setOpaque(false);
        left.add(dot); left.add(nameLabel);

        row.add(left, BorderLayout.WEST);
        row.add(statusLabel, BorderLayout.EAST);
        return row;
    }
}