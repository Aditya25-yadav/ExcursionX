package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class UITheme {

    // --- COLOUR PALETTE ---
    public static final Color BG_DARK       = new Color(10, 14, 26);
    public static final Color BG_PANEL      = new Color(20, 27, 48);
    public static final Color BG_CARD       = new Color(28, 38, 65);
    public static final Color ACCENT_GOLD   = new Color(245, 166, 35);
    public static final Color ACCENT_BLUE   = new Color(64, 150, 255);
    public static final Color TEXT_WHITE    = new Color(240, 244, 255);
    public static final Color TEXT_GREY     = new Color(150, 160, 190);
    public static final Color SUCCESS_GREEN = new Color(52, 211, 153);
    public static final Color ERROR_RED     = new Color(239, 68, 68);
    public static final Color TABLE_ROW_ODD = new Color(25, 34, 58);
    public static final Color TABLE_ROW_EVN = new Color(33, 45, 75);
    public static final Color TABLE_HDR     = new Color(15, 20, 40);

    // --- FONTS ---
    public static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD,  26);
    public static final Font FONT_SUB    = new Font("SansSerif", Font.BOLD,  14);
    public static final Font FONT_BODY   = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD,  13);

    // --- HELPERS ---

    /** Styled primary button (gold) */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SUB);
        btn.setBackground(ACCENT_GOLD);
        btn.setForeground(BG_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(ACCENT_GOLD.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(ACCENT_GOLD);
            }
        });
        return btn;
    }

    /** Styled danger button (red) */
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SUB);
        btn.setBackground(ERROR_RED);
        btn.setForeground(TEXT_WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(ERROR_RED.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(ERROR_RED);
            }
        });
        return btn;
    }

    /** Styled secondary button (blue outline) */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SUB);
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(TEXT_WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(ACCENT_BLUE.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(ACCENT_BLUE);
            }
        });
        return btn;
    }

    /** Styled text field */
    public static JTextField textField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBackground(BG_CARD);
        tf.setForeground(TEXT_WHITE);
        tf.setCaretColor(ACCENT_GOLD);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 80, 130), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return tf;
    }

    /** Styled password field */
    public static JPasswordField passwordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_BODY);
        pf.setBackground(BG_CARD);
        pf.setForeground(TEXT_WHITE);
        pf.setCaretColor(ACCENT_GOLD);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 80, 130), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return pf;
    }

    /** Styled label */
    public static JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_GREY);
        return lbl;
    }

    /** Section header label */
    public static JLabel headerLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(ACCENT_GOLD);
        return lbl;
    }

    /** Card panel with rounded look */
    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 70, 120), 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return p;
    }

    /** Style a JTable for the dark theme */
    public static void styleTable(JTable table) {
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_WHITE);
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setGridColor(new Color(40, 55, 90));
        table.setSelectionBackground(ACCENT_GOLD.darker());
        table.setSelectionForeground(BG_DARK);
        table.getTableHeader().setBackground(TABLE_HDR);
        table.getTableHeader().setForeground(ACCENT_GOLD);
        table.getTableHeader().setFont(FONT_HEADER);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? TABLE_ROW_ODD : TABLE_ROW_EVN);
                    setForeground(TEXT_WHITE);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    /** Style a JComboBox for dark theme */
    public static void styleCombo(JComboBox<?> cb) {
        cb.setFont(FONT_BODY);
        cb.setBackground(BG_CARD);
        cb.setForeground(TEXT_WHITE);
        cb.setBorder(BorderFactory.createLineBorder(new Color(60, 80, 130), 1));
    }

    /** Style a JScrollPane */
    public static JScrollPane scrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_PANEL);
        sp.getViewport().setBackground(BG_PANEL);
        sp.setBorder(BorderFactory.createLineBorder(new Color(40, 55, 90), 1));
        return sp;
    }

    /** Style a JTextArea */
    public static JTextArea textArea() {
        JTextArea ta = new JTextArea();
        ta.setFont(FONT_BODY);
        ta.setBackground(BG_CARD);
        ta.setForeground(TEXT_WHITE);
        ta.setCaretColor(ACCENT_GOLD);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        return ta;
    }

    /** Apply global dark Look-and-Feel defaults */
    public static void applyGlobalDefaults() {
        UIManager.put("TabbedPane.background", BG_PANEL);
        UIManager.put("TabbedPane.foreground", TEXT_WHITE);
        UIManager.put("TabbedPane.selected", BG_CARD);
        UIManager.put("TabbedPane.selectedForeground", ACCENT_GOLD);
        UIManager.put("TabbedPane.contentAreaColor", BG_PANEL);
        UIManager.put("TabbedPane.font", FONT_SUB);
        UIManager.put("Panel.background", BG_PANEL);
        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_WHITE);
    }
}
