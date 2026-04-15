package ui;

import dao.SessionDAO;
import dao.StationDAO;
import model.Session;
import model.Station;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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

    // Colors
    private static final Color PAGE_BG      = new Color(0xF5F5F5);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color GREEN_BTN    = new Color(0x2E7D32);
    private static final Color GREEN_HOVER  = new Color(0x1B5E20);
    private static final Color RED_BTN      = new Color(0xC62828);
    private static final Color RED_HOVER    = new Color(0xB71C1C);
    private static final Color GRAY_BTN     = new Color(0x9E9E9E);
    private static final Color AMBER_TIMER  = new Color(0xFFFDE7);
    private static final Color AMBER_TEXT   = new Color(0xE65100);
    private static final Color BADGE_REG    = new Color(0xEEEEEE);
    private static final Color BADGE_VIP    = new Color(0x1C3557);

    // DAOs
    private final StationDAO stationDAO = new StationDAO();
    private final SessionDAO sessionDAO = new SessionDAO();

    // State — keyed by station_id
    
    // Maps station_id → session start time in milliseconds
    private final Map<Integer, Long> sessionStartTimes = new HashMap<>();
    // Maps station_id → active session_id (needed for endSession)
    private final Map<Integer, Integer> activeSessionIds = new HashMap<>();
    // Maps station_id → timer JLabel on its card
    private final Map<Integer, JLabel> timerLabels = new HashMap<>();
    // Maps station_id → rate per hour (needed to calculate charge)
    private final Map<Integer, Double> stationRates = new HashMap<>();

    // The current logged-in user — set from MainFrame
    private model.User currentUser;

    // The panel that holds all the cards
    private JPanel cardsPanel;

    // Single shared timer that ticks every second for all cards
    private Timer sessionTimer;
    
    // Constructor
    public StationsPanel() {
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
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
        scroll.getViewport().setBackground(PAGE_BG);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    // refreshData() — called every time the Stations tab is shown
    // Loads all stations and their active sessions from the database
    // ---------------------------------------------------------------
    public void refreshData() {
        new SwingWorker<Void, Void>() {
            List<Station> stations;
            // Maps station_id → its active Session (null if none)
            Map<Integer, Session> activeSessions = new HashMap<>();

            @Override
            protected Void doInBackground() {
                stations = stationDAO.getAllStations();
                for (Station s : stations) {
                    if (s.getStatus().equals("occupied")) {
                        Session active = sessionDAO
                                .getActiveSessionByStation(s.getStationId());
                        if (active != null) {
                            activeSessions.put(s.getStationId(), active);
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                // Clear previous state
                cardsPanel.removeAll();
                timerLabels.clear();
                sessionStartTimes.clear();
                activeSessionIds.clear();
                stationRates.clear();

                for (Station s : stations) {
                    // Store rate for charge calculation later
                    stationRates.put(s.getStationId(),
                                     s.getRatePerHour());

                    // If occupied, record the session start time
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
        String type   = station.getStationType();   // "regular" or "vip"
        double rate   = station.getRatePerHour();
        String status = station.getStatus();

        // ---- Card shell ----
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ---- Station name ----
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(new Color(0x222222));

        // ---- Type badge ----
        boolean isVip = type.equalsIgnoreCase("vip");
        JLabel typeBadge = new JLabel(
                isVip ? "VIP" : "Regular", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isVip ? BADGE_VIP : BADGE_REG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        typeBadge.setFont(new Font("Arial", Font.BOLD, 11));
        typeBadge.setForeground(isVip ? Color.WHITE
                                      : new Color(0x555555));
        typeBadge.setOpaque(false);
        typeBadge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

        // ---- Rate label ----
        JLabel rateLabel = new JLabel(
                String.format("₱%.2f / hr", rate),
                SwingConstants.CENTER);
        rateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
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

        // ---- Status display button (not clickable) ----
        JButton statusBtn = buildRoundedButton(
                capitalize(status),
                statusColor(status),
                statusColor(status),
                Color.WHITE
        );
        statusBtn.setEnabled(false);

        // ---- Timer panel ----
        JLabel timerLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        timerLabel.setForeground(AMBER_TEXT);
        timerLabel.setBackground(AMBER_TIMER);
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

        // Register label so the global timer can update it
        timerLabels.put(id, timerLabel);

        // ---- Action button ----
        JButton actionBtn = buildActionButton(status, id, rate, card);

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
    private JButton buildActionButton(String status, int stationId,
                                       double rate, JPanel card) {
        switch (status.toLowerCase()) {

            case "available":
                JButton startBtn = buildRoundedButton(
                        "Start Session",
                        GREEN_BTN, GREEN_HOVER, Color.WHITE);
                startBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (startBtn.isEnabled()) {
                            handleStartSession(stationId);
                        }
                    }
                });
                return startBtn;

            case "occupied":
                JButton endBtn = buildRoundedButton(
                        "End Session",
                        RED_BTN, RED_HOVER, Color.WHITE);
                endBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (endBtn.isEnabled()) {
                            handleEndSession(stationId, rate);
                        }
                    }
                });
                return endBtn;

            default: // maintenance
                JButton unavailBtn = buildRoundedButton(
                        "Unavailable",
                        GRAY_BTN, GRAY_BTN, Color.WHITE);
                unavailBtn.setEnabled(false);
                return unavailBtn;
        }
    }

    // Start Session handler
    private void handleStartSession(int stationId) {
        int userId = (currentUser != null) ? currentUser.getUserId() : 1;

        // Confirm before starting
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Start a new session on this station?",
            "Start Session",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                // 1. Insert new session row
                int newSessionId = sessionDAO.startSession(
                        stationId, userId);
                if (newSessionId < 0) return -1;

                // 2. Mark station as occupied
                stationDAO.updateStatus(stationId, "occupied");
                return newSessionId;
            }

            @Override
            protected void done() {
                try {
                    int newSessionId = get();
                    if (newSessionId < 0) {
                        JOptionPane.showMessageDialog(
                            StationsPanel.this,
                            "Could not start session."
                            + " Check your connection.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Reload all cards to reflect new state
                    refreshData();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // End Session handler
    private void handleEndSession(int stationId, double ratePerHour) {
        Integer sessionId = activeSessionIds.get(stationId);
        Long    startMs   = sessionStartTimes.get(stationId);

        if (sessionId == null || startMs == null) {
            // Session data not loaded yet — just refresh
            refreshData();
            return;
        }

        // Calculate elapsed time and charge for the confirmation dialog
        long elapsedSeconds = (System.currentTimeMillis() - startMs) / 1000;
        long hours          = elapsedSeconds / 3600;
        long minutes        = (elapsedSeconds % 3600) / 60;
        long seconds        = elapsedSeconds % 60;
        double charge       = (elapsedSeconds / 3600.0) * ratePerHour;

        String timeStr = String.format("%02d:%02d:%02d",
                hours, minutes, seconds);
        String chargeStr = String.format("₱%.2f", charge);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "End session?\n\n"
            + "Duration:  " + timeStr + "\n"
            + "Charge:    " + chargeStr + "\n\n"
            + "This will mark the station as available.",
            "End Session",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        final int    finalSessionId = sessionId;
        final double finalRate      = ratePerHour;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                // 1. Complete the session — calculates charge in SQL
                boolean ended = sessionDAO.endSession(
                        finalSessionId, finalRate);
                if (!ended) return false;

                // 2. Mark station as available again
                stationDAO.updateStatus(stationId, "available");
                return true;
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (!success) {
                        JOptionPane.showMessageDialog(
                            StationsPanel.this,
                            "Could not end session."
                            + " Check your connection.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    JOptionPane.showMessageDialog(
                        StationsPanel.this,
                        "Session ended.\n"
                        + "Duration: " + timeStr + "\n"
                        + "Charge:   " + chargeStr,
                        "Session Complete",
                        JOptionPane.INFORMATION_MESSAGE
                    );

                    // Reload all cards
                    refreshData();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
        for (Map.Entry<Integer, Long> entry
                : sessionStartTimes.entrySet()) {
            int  stationId = entry.getKey();
            long startMs   = entry.getValue();

            JLabel label = timerLabels.get(stationId);
            if (label == null || !label.isVisible()) continue;

            long elapsed = (now - startMs) / 1000;
            long h = elapsed / 3600;
            long m = (elapsed % 3600) / 60;
            long s = elapsed % 60;

            label.setText(String.format("%02d:%02d:%02d", h, m, s));
        }
    }

    // Reusable rounded button
    private JButton buildRoundedButton(String text,
            Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : GRAY_BTN);
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
        btn.setPreferredSize(new Dimension(0, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(hover);
                    btn.repaint();
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
                btn.repaint();
            }
        });

        return btn;
    }

    // Helpers
    private Color statusColor(String status) {
        switch (status.toLowerCase()) {
            case "occupied":    return RED_BTN;
            case "maintenance": return GRAY_BTN;
            default:            return GREEN_BTN;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase()
             + s.substring(1).toLowerCase();
    }

    // Main — for testing directly (Shift + F6)
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            javax.swing.JFrame f =
                    new javax.swing.JFrame("Stations Test");
            f.setDefaultCloseOperation(
                    javax.swing.JFrame.EXIT_ON_CLOSE);
            f.setSize(1280, 720);
            f.setLocationRelativeTo(null);
            StationsPanel panel = new StationsPanel();
            f.add(panel);
            f.setVisible(true);
            panel.refreshData();
        });
    }
}