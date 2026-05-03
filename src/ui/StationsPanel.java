package ui;

import dao.SessionDAO;
import dao.StationDAO;
import model.Session;
import model.Station;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;

public class StationsPanel extends JPanel {

    // DAOs
    private final StationDAO stationDAO = new StationDAO();
    private final SessionDAO sessionDAO = new SessionDAO();

    // State — keyed by station_id
    private final Map<Integer, Long>    sessionStartTimes = new HashMap<>();
    private final Map<Integer, Integer> activeSessionIds  = new HashMap<>();
    private final Map<Integer, JLabel>  timerLabels       = new HashMap<>();
    private final Map<Integer, Double>  stationRates      = new HashMap<>();

    // The current logged-in user — set from MainFrame
    private model.User currentUser;

    // The panel that holds all the cards
    private JPanel cardsPanel;

    // Single shared timer that ticks every second for all cards
    private Timer sessionTimer;

    // Constructor
    public StationsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        startGlobalTimer();
    }

    // Setter called by MainFrame after login
    public void setCurrentUser(model.User user) {
        this.currentUser = user;
    }

    // Build the scrollable card grid container
    private void buildUI() {
        cardsPanel = new JPanel(new GridLayout(0, 4, 16, 16));
        cardsPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UIHelper.PAGE_BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    // refreshData() — called every time the Stations tab is shown
    // ---------------------------------------------------------------
    public void refreshData() {
        new SwingWorker<Void, Void>() {
            List<Station> stations;
            Map<Integer, Session> activeSessions = new HashMap<>();

            @Override
            protected Void doInBackground() {
                stations = stationDAO.getAllStations();
                for (Station s : stations) {
                    if (s.getStatus().equals("occupied")) {
                        Session active = sessionDAO
                                .getActiveSessionByStation(s.getStationId());
                        if (active != null)
                            activeSessions.put(s.getStationId(), active);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                cardsPanel.removeAll();
                timerLabels.clear();
                sessionStartTimes.clear();
                activeSessionIds.clear();
                stationRates.clear();

                for (Station s : stations) {
                    stationRates.put(s.getStationId(), s.getRatePerHour());

                    Session active = activeSessions.get(s.getStationId());
                    if (active != null) {
                        sessionStartTimes.put(s.getStationId(),
                                active.getStartTime().getTime());
                        activeSessionIds.put(s.getStationId(),
                                active.getSessionId());
                    }
                    cardsPanel.add(buildStationCard(s));
                }

                cardsPanel.revalidate();
                cardsPanel.repaint();
            }
        }.execute();
    }

    // Build one station card from a Station object
    private JPanel buildStationCard(Station station) {
        int    id     = station.getStationId();
        String name   = station.getStationName();
        String type   = station.getStationType();
        double rate   = station.getRatePerHour();
        String status = station.getStatus();

        // ---- Card shell ----
        JPanel card = UIHelper.card(new BorderLayout(0, 8), 16);

        // ---- Station name ----
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(new Color(0x222222));

        // ---- Type badge ----
        boolean isVip = type.equalsIgnoreCase("vip");
        JLabel typeBadge = new JLabel(isVip ? "VIP" : "Regular",
                SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isVip ? UIHelper.NAVY : UIHelper.EXPORT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        typeBadge.setFont(new Font("Arial", Font.BOLD, 11));
        typeBadge.setForeground(isVip ? Color.WHITE : new Color(0x555555));
        typeBadge.setOpaque(false);
        typeBadge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

        // ---- Rate label ----
        JLabel rateLabel = new JLabel(
                String.format("\u20B1%.2f / hr", rate), SwingConstants.CENTER);
        rateLabel.setFont(UIHelper.FONT_PLAIN_SM);
        rateLabel.setForeground(new Color(0x666666));

        // ---- Top info block ----
        JPanel badgeWrapper = new JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(typeBadge);

        JPanel topInfo = new JPanel(new BorderLayout(0, 4));
        topInfo.setOpaque(false);
        topInfo.add(nameLabel,    BorderLayout.NORTH);
        topInfo.add(badgeWrapper, BorderLayout.CENTER);
        topInfo.add(rateLabel,    BorderLayout.SOUTH);

        // ---- Status display button (non-clickable) ----
        JButton statusBtn = UIHelper.button(
            UIHelper.capitalize(status),
            statusColor(status), statusColor(status), Color.WHITE, 38);
        statusBtn.setEnabled(false);
        statusBtn.setPreferredSize(new Dimension(0, 38));

        // ---- Timer panel ----
        JLabel timerLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        timerLabel.setForeground(UIHelper.AMBER_TXT);
        timerLabel.setBackground(UIHelper.AMBER);
        timerLabel.setOpaque(true);
        timerLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFFE082), 1),
            BorderFactory.createEmptyBorder(8, 0, 8, 0)
        ));

        JLabel timerHint = new JLabel("Session Timer", SwingConstants.CENTER);
        timerHint.setFont(new Font("Arial", Font.PLAIN, 11));
        timerHint.setForeground(new Color(0x999999));

        JPanel timerPanel = new JPanel(new BorderLayout(0, 2));
        timerPanel.setOpaque(false);
        timerPanel.add(timerLabel, BorderLayout.CENTER);
        timerPanel.add(timerHint,  BorderLayout.SOUTH);
        timerPanel.setVisible(status.equals("occupied"));

        timerLabels.put(id, timerLabel);

        // ---- Action button ----
        JButton actionBtn = buildActionButton(status, id, rate);

        // ---- Bottom section ----
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(statusBtn,  BorderLayout.NORTH);
        bottom.add(timerPanel, BorderLayout.CENTER);
        bottom.add(actionBtn,  BorderLayout.SOUTH);

        card.add(topInfo, BorderLayout.NORTH);
        card.add(bottom,  BorderLayout.SOUTH);
        return card;
    }

    // Build the correct action button based on current status
    private JButton buildActionButton(String status, int stationId, double rate) {
        switch (status.toLowerCase()) {

            case "available":
                JButton startBtn = UIHelper.button(
                    "Start Session", UIHelper.GREEN, UIHelper.GREEN_HOV,
                    Color.WHITE, 38);
                startBtn.setPreferredSize(new Dimension(0, 38));
                startBtn.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        if (startBtn.isEnabled()) handleStartSession(stationId);
                    }
                });
                return startBtn;

            case "occupied":
                JButton endBtn = UIHelper.button(
                    "End Session", UIHelper.RED, UIHelper.RED_HOV,
                    Color.WHITE, 38);
                endBtn.setPreferredSize(new Dimension(0, 38));
                endBtn.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        if (endBtn.isEnabled()) handleEndSession(stationId, rate);
                    }
                });
                return endBtn;

            default: // maintenance
                JButton unavailBtn = UIHelper.button(
                    "Unavailable", UIHelper.GRAY, UIHelper.GRAY,
                    Color.WHITE, 38);
                unavailBtn.setPreferredSize(new Dimension(0, 38));
                unavailBtn.setEnabled(false);
                return unavailBtn;
        }
    }

    // Start Session handler
    private void handleStartSession(int stationId) {
        int userId = (currentUser != null) ? currentUser.getUserId() : 1;

        int confirm = JOptionPane.showConfirmDialog(
            this, "Start a new session on this station?",
            "Start Session", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                int newSessionId = sessionDAO.startSession(stationId, userId);
                if (newSessionId < 0) return -1;
                stationDAO.updateStatus(stationId, "occupied");
                return newSessionId;
            }

            @Override
            protected void done() {
                try {
                    if (get() < 0) {
                        JOptionPane.showMessageDialog(StationsPanel.this,
                            "Could not start session. Check your connection.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    refreshData();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // End Session handler
    private void handleEndSession(int stationId, double ratePerHour) {
        Integer sessionId = activeSessionIds.get(stationId);
        Long    startMs   = sessionStartTimes.get(stationId);

        if (sessionId == null || startMs == null) { refreshData(); return; }

        long elapsedSeconds = (System.currentTimeMillis() - startMs) / 1000;
        long hours   = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;
        double charge = (elapsedSeconds / 3600.0) * ratePerHour;

        String timeStr   = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        String chargeStr = String.format("\u20B1%.2f", charge);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "End session?\n\nDuration:  " + timeStr + "\nCharge:    "
            + chargeStr + "\n\nThis will mark the station as available.",
            "End Session", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        final int    finalSessionId = sessionId;
        final double finalRate      = ratePerHour;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                boolean ended = sessionDAO.endSession(finalSessionId, finalRate);
                if (!ended) return false;
                stationDAO.updateStatus(stationId, "available");
                return true;
            }

            @Override
            protected void done() {
                try {
                    if (!get()) {
                        JOptionPane.showMessageDialog(StationsPanel.this,
                            "Could not end session. Check your connection.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    JOptionPane.showMessageDialog(StationsPanel.this,
                        "Session ended.\nDuration: " + timeStr
                        + "\nCharge:   " + chargeStr,
                        "Session Complete", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // Global timer — one Timer drives all active session clocks
    private void startGlobalTimer() {
        sessionTimer = new Timer(1000, e -> updateAllTimers());
        sessionTimer.setRepeats(true);
        sessionTimer.start();
    }

    private void updateAllTimers() {
        long now = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> entry : sessionStartTimes.entrySet()) {
            int  stationId = entry.getKey();
            long startMs   = entry.getValue();
            JLabel label   = timerLabels.get(stationId);
            if (label == null || !label.isVisible()) continue;

            long elapsed = (now - startMs) / 1000;
            label.setText(String.format("%02d:%02d:%02d",
                    elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60));
        }
    }

    // Helpers
    private Color statusColor(String status) {
        switch (status.toLowerCase()) {
            case "occupied":    return UIHelper.RED;
            case "maintenance": return UIHelper.GRAY;
            default:            return UIHelper.GREEN;
        }
    }
}