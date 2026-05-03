package ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;

/**
 * UIHelper.java
 * Shared UI building methods used across all panels.
 * Centralises repeated code to reduce file lengths.
 */
public class UIHelper {

    // ---------------------------------------------------------------
    // Shared color palette — import these in panels as needed
    // ---------------------------------------------------------------
    public static final Color NAVY         = new Color(0x1C3557);
    public static final Color NAVY_MID     = new Color(0x2D6DA8);
    public static final Color PAGE_BG      = new Color(0xF5F5F5);
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color GREEN        = new Color(0x2E7D32);
    public static final Color GREEN_HOV    = new Color(0x1B5E20);
    public static final Color RED          = new Color(0xC62828);
    public static final Color RED_HOV      = new Color(0xB71C1C);
    public static final Color BLUE         = new Color(0x1565C0);
    public static final Color BLUE_HOV     = new Color(0x0D47A1);
    public static final Color GRAY         = new Color(0x9E9E9E);
    public static final Color GRAY_HOV     = new Color(0x757575);
    public static final Color GRAY_LABEL   = new Color(0x777777);
    public static final Color STATUS_GRAY  = new Color(0x888888);
    public static final Color AMBER        = new Color(0xFFFDE7);
    public static final Color AMBER_TXT    = new Color(0xE65100);
    public static final Color ROW_ALT      = new Color(0xF0F4F8);
    public static final Color ROW_LOW      = new Color(0xFFFDE7);
    public static final Color ROW_VOID     = new Color(0xFFEBEE);
    public static final Color OK_COLOR     = new Color(0x2E7D32);
    public static final Color LOW_COLOR    = new Color(0xE65100);
    public static final Color TXN_BLUE     = new Color(0x2D6DA8);
    
    // Login screen background gradient
    public static final Color BG_DARK      = new Color(0x1A2C4E);
    public static final Color BG_DARKER    = new Color(0x152340);

    // Form field styling
    public static final Color FIELD_BORDER = new Color(0xCCCCCC);
    public static final Color PLACEHOLDER  = new Color(0xAAAAAA);
    
    // Login button hover
    public static final Color BLUE_HOVER   = new Color(0x245A8E);

    // Used by SalesPanel total row
    public static final Color TOTAL_BG     = new Color(0xEEF4FA);

    // Used by ReportsPanel export/void buttons
    public static final Color EXPORT_BG    = new Color(0xEEEEEE);
    public static final Color EXPORT_HOV   = new Color(0xDDDDDD);
    public static final Color VOID_RED     = new Color(0xC62828);
    public static final Color VOID_HOV     = new Color(0xB71C1C);

    // Used by ProductsPanel low-stock banner border
    public static final Color AMBER_BORDER = new Color(0xFFB300);
    
    // ---------------------------------------------------------------
    // Font constants — Dialog maps to Segoe UI on Windows,
    // which supports ₱, ✔, ⚠, ✕ and other Unicode symbols.
    // Use these instead of new Font("Arial",...) wherever a
    // symbol character appears nearby.
    // ---------------------------------------------------------------
    public static final Font FONT_BOLD_LG  = new Font("Dialog", Font.BOLD,   16);
    public static final Font FONT_BOLD_MED = new Font("Dialog", Font.BOLD,   13);
    public static final Font FONT_BOLD_SM  = new Font("Dialog", Font.BOLD,   12);
    public static final Font FONT_PLAIN_MD = new Font("Dialog", Font.PLAIN,  13);
    public static final Font FONT_PLAIN_SM = new Font("Dialog", Font.PLAIN,  12);
    public static final Font FONT_PLAIN_XS = new Font("Dialog", Font.PLAIN,  11);

    // Prevent instantiation
    private UIHelper() {}

    // ---------------------------------------------------------------
    // Rounded button — used by every panel and dialog
    // ---------------------------------------------------------------
    public static JButton button(String text,
            Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : GRAY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(UIHelper.FONT_BOLD_MED);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(hover); btn.repaint();
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(bg); btn.repaint();
            }
        });
        return btn;
    }
    
    // Overload with forced height — used for full-width card buttons
    public static JButton button(String text,
            Color bg, Color hover, Color fg, int height) {
        JButton btn = button(text, bg, hover, fg);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        btn.setPreferredSize(new Dimension(0, height));
        return btn;
    }

    // ---------------------------------------------------------------
    // Standard table styling — applied once per JTable
    // ---------------------------------------------------------------
    public static void styleTable(JTable table) {
        JTableHeader h = table.getTableHeader();
        h.setBackground(NAVY);
        h.setForeground(Color.WHITE);
        h.setFont(UIHelper.FONT_BOLD_MED);
        h.setPreferredSize(new Dimension(0, 38));
        h.setReorderingAllowed(false);

        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(NAVY);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);

        table.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                setBackground(sel ? NAVY :
                        (row % 2 == 0 ? Color.WHITE : ROW_ALT));
                setForeground(sel ? Color.WHITE
                                  : new Color(0x333333));
                setBorder(BorderFactory
                        .createEmptyBorder(0, 12, 0, 0));
                setFont(UIHelper.FONT_PLAIN_MD);
                return this;
            }
        });
    }

    // ---------------------------------------------------------------
    // KPI stat card — used by Dashboard and Reports
    // Returns a JPanel; call getValueLabel(card) for the JLabel
    // ---------------------------------------------------------------
    public static JPanel statCard(String value, String desc,
            Color valueColor, Color accent) {
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
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Dialog", Font.BOLD, 26));
        val.setForeground(valueColor);
        val.setName("valueLabel"); // used by getValueLabel()

        JLabel lbl = new JLabel(desc);
        lbl.setFont(UIHelper.FONT_PLAIN_SM);
        lbl.setForeground(new Color(0x777777));

        JPanel stack = new JPanel(new BorderLayout(0, 4));
        stack.setOpaque(false);
        stack.add(val, BorderLayout.NORTH);
        stack.add(lbl, BorderLayout.SOUTH);
        card.add(stack, BorderLayout.CENTER);
        return card;
    }

    // Retrieves the large value JLabel from a stat card
    public static JLabel getValueLabel(JPanel card) {
        JPanel stack = (JPanel) card.getComponent(0);
        for (java.awt.Component c : stack.getComponents()) {
            if (c instanceof JLabel
                    && "valueLabel".equals(c.getName()))
                return (JLabel) c;
        }
        return (JLabel) stack.getComponent(0);
    }

    // ---------------------------------------------------------------
    // White rounded card panel — wraps content in all tab panels
    // ---------------------------------------------------------------
    public static JPanel card(java.awt.LayoutManager layout,
                               int pad) {
        JPanel card = new JPanel(layout) {
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
        if (pad > 0)
            card.setBorder(
                    BorderFactory.createEmptyBorder(pad, pad, pad, pad));
        return card;
    }

    // ---------------------------------------------------------------
    // Capitalize first letter, lowercase rest
    // ---------------------------------------------------------------
    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase()
             + s.substring(1).toLowerCase();
    }

    // ---------------------------------------------------------------
    // Status cell renderer — used in Products and Reports tables
    // col = column index that holds "OK" / "LOW" / "completed" / "voided"
    // ---------------------------------------------------------------
    public static DefaultTableCellRenderer statusRenderer(
            String completedText, String warningText) {
        return new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                String s = v == null ? "" : v.toString();
                if (sel) {
                    setBackground(NAVY);
                    setForeground(Color.WHITE);
                } else if ("LOW".equals(s)
                        || "voided".equalsIgnoreCase(s)) {
                    setBackground("voided".equalsIgnoreCase(s)
                            ? ROW_VOID : ROW_LOW);
                    setForeground("voided".equalsIgnoreCase(s)
                            ? RED : LOW_COLOR);
                    setText(warningText);
                } else {
                    setBackground(row % 2 == 0
                            ? Color.WHITE : ROW_ALT);
                    setForeground(OK_COLOR);
                    setText(completedText);
                }
                setFont(UIHelper.FONT_BOLD_SM);
                setBorder(BorderFactory
                        .createEmptyBorder(0, 12, 0, 0));
                return this;
            }
        };
    }
}