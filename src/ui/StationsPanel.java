package ui;

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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class StationsPanel extends JPanel {

    // Colors
    private static final Color PAGE_BG       = new Color(0xF5F5F5);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color NAVY_DARK     = new Color(0x1C3557);
    private static final Color GREEN_BTN     = new Color(0x2E7D32);
    private static final Color GREEN_HOVER   = new Color(0x1B5E20);
    private static final Color RED_BTN       = new Color(0xC62828);
    private static final Color RED_HOVER     = new Color(0xB71C1C);
    private static final Color GRAY_BTN      = new Color(0x9E9E9E);
    private static final Color AMBER_TIMER   = new Color(0xFFFDE7);
    private static final Color AMBER_TEXT    = new Color(0xE65100);
    private static final Color BADGE_REGULAR = new Color(0xEEEEEE);
    private static final Color BADGE_VIP     = new Color(0x1C3557);

    // ---------------------------------------------------------------
    // Sample station data — replaced by StationDAO later
    // {name, type, rate, status}
    // ---------------------------------------------------------------
    private static final String[][] STATION_DATA = {
        {"PC-01",  "Regular", "20.00", "available"},
        {"PC-02",  "Regular", "20.00", "occupied"},
        {"PC-03",  "Regular", "20.00", "occupied"},
        {"PC-04",  "Regular", "20.00", "available"},
        {"PC-05",  "Regular", "20.00", "maintenance"},
        {"VIP-01", "VIP",     "40.00", "available"},
        {"VIP-02", "VIP",     "40.00", "occupied"},
    };

    // Session start times in ms (0 = no active session)
    private long[] sessionStartTimes;
    private JLabel[] timerLabels;
    private Timer sessionTimer;

    // Constructor
    public StationsPanel() {
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        sessionStartTimes = new long[STATION_DATA.length];
        timerLabels       = new JLabel[STATION_DATA.length];

        // Pre-set fake start times for occupied stations (for demo)
        long now = System.currentTimeMillis();
        for (int i = 0; i < STATION_DATA.length; i++) {
            if (STATION_DATA[i][3].equals("occupied")) {
                // Simulate sessions that started some time ago
                sessionStartTimes[i] = now - (long)(Math.random() * 7200000);
            }
        }

        buildUI();
        startTimers();
    }

    // Build UI
    private void buildUI() {
        JPanel grid = new JPanel(new GridLayout(0, 4, 16, 16));
        grid.setOpaque(false);

        for (int i = 0; i < STATION_DATA.length; i++) {
            grid.add(buildStationCard(i));
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PAGE_BG);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    // Build station card
    private JPanel buildStationCard(int index) {
        String name   = STATION_DATA[index][0];
        String type   = STATION_DATA[index][1];
        String rate   = STATION_DATA[index][2];
        String status = STATION_DATA[index][3];

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

        // --- Station name ---
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(new Color(0x222222));

        // --- Type badge ---
        boolean isVip = type.equalsIgnoreCase("VIP");
        JLabel typeBadge = new JLabel(type, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isVip ? BADGE_VIP : BADGE_REGULAR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        typeBadge.setFont(new Font("Arial", Font.BOLD, 11));
        typeBadge.setForeground(isVip ? Color.WHITE : new Color(0x555555));
        typeBadge.setOpaque(false);
        typeBadge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

        // --- Rate label ---
        JLabel rateLabel = new JLabel("₱" + rate + " / hr", SwingConstants.CENTER);
        rateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        rateLabel.setForeground(new Color(0x666666));

        // Center-align badge and rate together
        JPanel topInfo = new JPanel(new BorderLayout(0, 4));
        topInfo.setOpaque(false);

        JPanel badgeWrapper = new JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(typeBadge);

        topInfo.add(nameLabel,    BorderLayout.NORTH);
        topInfo.add(badgeWrapper, BorderLayout.CENTER);
        topInfo.add(rateLabel,    BorderLayout.SOUTH);

        // --- Status button (display only) ---
        JButton statusBtn = buildRoundedButton(
            capitalize(status),
            statusBgColor(status),
            statusBgColor(status),
            Color.WHITE
        );
        statusBtn.setEnabled(false);
        statusBtn.setDisabledIcon(null);

        // --- Timer panel (only for occupied) ---
        JPanel timerPanel = new JPanel(new BorderLayout(0, 2));
        timerPanel.setOpaque(false);

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

        timerPanel.add(timerLabel, BorderLayout.CENTER);
        timerPanel.add(timerHint,  BorderLayout.SOUTH);
        timerPanel.setVisible(status.equals("occupied"));

        timerLabels[index] = timerLabel;

        // --- Action button ---
        JButton actionBtn = buildActionButton(status);

        // --- Assemble card ---
        JPanel bottomSection = new JPanel(new BorderLayout(0, 8));
        bottomSection.setOpaque(false);
        bottomSection.add(statusBtn,   BorderLayout.NORTH);
        bottomSection.add(timerPanel,  BorderLayout.CENTER);
        bottomSection.add(actionBtn,   BorderLayout.SOUTH);

        card.add(topInfo,       BorderLayout.NORTH);
        card.add(bottomSection, BorderLayout.SOUTH);

        return card;
    }

    // Action button based on status
    private JButton buildActionButton(String status) {
        switch (status.toLowerCase()) {
            case "occupied":
                return buildRoundedButton("End Session",
                        RED_BTN, RED_HOVER, Color.WHITE);
            case "maintenance":
                JButton unavail = buildRoundedButton("Unavailable",
                        GRAY_BTN, GRAY_BTN, Color.WHITE);
                unavail.setEnabled(false);
                return unavail;
            default:
                return buildRoundedButton("Start Session",
                        GREEN_BTN, GREEN_HOVER, Color.WHITE);
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

    // Session timers
    private void startTimers() {
        sessionTimer = new Timer(1000, e -> updateAllTimers());
        sessionTimer.start();
    }

    private void updateAllTimers() {
        long now = System.currentTimeMillis();
        for (int i = 0; i < STATION_DATA.length; i++) {
            if (timerLabels[i] != null && sessionStartTimes[i] > 0) {
                long elapsed = (now - sessionStartTimes[i]) / 1000;
                long hours   = elapsed / 3600;
                long minutes = (elapsed % 3600) / 60;
                long seconds = elapsed % 60;
                timerLabels[i].setText(
                    String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }
        }
    }

    // Helpers
    private Color statusBgColor(String status) {
        switch (status.toLowerCase()) {
            case "occupied":    return RED_BTN;
            case "maintenance": return GRAY_BTN;
            default:            return GREEN_BTN;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    // Main testing code
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            javax.swing.JFrame f = new javax.swing.JFrame("Stations Test");
            f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            f.setSize(1280, 720);
            f.setLocationRelativeTo(null);
            f.add(new StationsPanel());
            f.setVisible(true);
        });
    }
}