package ui;

import databases.UserDao;
import models.*;
import models.SecurityUtils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private UserDao userDao = new UserDao();

    // Login components
    private JTextField loginEmailField;
    private JPasswordField loginPassField;
    private JComboBox<String> loginRoleCombo;

    // Register components
    private JTextField regNameField, regEmailField, regExtraField1, regExtraField2;
    private JPasswordField regPassField;
    private JComboBox<String> regRoleCombo;
    private JLabel regExtra1Label, regExtra2Label;

    public LoginFrame() {
        UITheme.applyGlobalDefaults();
        setTitle("ExcursionX — Smart Student Excursion Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);

        root.add(buildBrandPanel(), BorderLayout.WEST);
        root.add(buildAuthPanel(), BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
    }

    // ─── LEFT BRANDING PANEL ───────────────────────────────────────────────────
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_PANEL);
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(40, 55, 100)));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel icon = new JLabel("✈", SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 72));
        icon.setForeground(UITheme.ACCENT_GOLD);
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("ExcursionX", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(UITheme.TEXT_WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("<html><center>Smart Student<br>Excursion Portal</center></html>", SwingConstants.CENTER);
        tagline.setFont(UITheme.FONT_BODY);
        tagline.setForeground(UITheme.TEXT_GREY);
        tagline.setAlignmentX(CENTER_ALIGNMENT);

        JLabel divider = new JLabel("───────────────", SwingConstants.CENTER);
        divider.setForeground(new Color(50, 70, 120));
        divider.setAlignmentX(CENTER_ALIGNMENT);

        String[] bullets = {"🔄 Automated Seat Allocation", "⚡ Concurrent Booking", "💳 Payment Tracking", "📊 Budget Analytics", "👥 Role-Based Access"};
        inner.add(icon);
        inner.add(Box.createVerticalStrut(10));
        inner.add(title);
        inner.add(Box.createVerticalStrut(6));
        inner.add(tagline);
        inner.add(Box.createVerticalStrut(16));
        inner.add(divider);
        inner.add(Box.createVerticalStrut(16));
        for (String b : bullets) {
            JLabel lbl = new JLabel(b);
            lbl.setFont(UITheme.FONT_SMALL);
            lbl.setForeground(UITheme.TEXT_GREY);
            lbl.setAlignmentX(CENTER_ALIGNMENT);
            inner.add(lbl);
            inner.add(Box.createVerticalStrut(7));
        }

        panel.add(inner);
        return panel;
    }

    // ─── RIGHT AUTH PANEL (Tabbed Login / Register) ────────────────────────────
    private JPanel buildAuthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_DARK);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(490, 440));
        tabs.setBackground(UITheme.BG_PANEL);
        tabs.setForeground(UITheme.TEXT_WHITE);
        tabs.setFont(UITheme.FONT_SUB);

        tabs.addTab("  🔐 Login  ", buildLoginTab());
        tabs.addTab("  📝 Register  ", buildRegisterTab());

        panel.add(tabs);
        return panel;
    }

    // ─── LOGIN TAB ─────────────────────────────────────────────────────────────
    private JPanel buildLoginTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_PANEL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = gbc();

        JLabel heading = UITheme.headerLabel("Welcome Back");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        gbc.gridwidth = 2;
        form.add(heading, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        form.add(UITheme.label("Role"), gbc);
        gbc.gridx = 1;
        loginRoleCombo = roleCombo();
        form.add(loginRoleCombo, gbc);

        gbc.gridy++; gbc.gridx = 0;
        form.add(UITheme.label("Email"), gbc);
        gbc.gridx = 1;
        loginEmailField = UITheme.textField();
        loginEmailField.setPreferredSize(new Dimension(220, 36));
        form.add(loginEmailField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        form.add(UITheme.label("Password"), gbc);
        gbc.gridx = 1;
        loginPassField = UITheme.passwordField();
        loginPassField.setPreferredSize(new Dimension(220, 36));
        form.add(loginPassField, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        JButton loginBtn = UITheme.primaryButton("  LOGIN  ");
        loginBtn.setPreferredSize(new Dimension(310, 42));
        loginBtn.addActionListener(e -> doLogin());
        loginPassField.addActionListener(e -> doLogin());
        form.add(loginBtn, gbc);

        panel.add(form);
        return panel;
    }

    // ─── REGISTER TAB ──────────────────────────────────────────────────────────
    // FIX: All fields now use fill=HORIZONTAL + weightx=1.0 so they ALWAYS expand
    //      to fill available column space and are never rendered at minimum size.
    private JPanel buildRegisterTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_PANEL);

        // Card-style form container
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(22, 30, 52));
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 70, 120), 1),
            BorderFactory.createEmptyBorder(24, 32, 24, 32)
        ));

        // --- GBC for labels: anchor=WEST, no fill, fixed weight ---
        GridBagConstraints lGbc = new GridBagConstraints();
        lGbc.anchor = GridBagConstraints.WEST;
        lGbc.fill   = GridBagConstraints.NONE;
        lGbc.weightx = 0;
        lGbc.insets  = new Insets(8, 4, 8, 12);

        // --- GBC for input fields: HORIZONTAL fill, weight=1 so they expand ---
        GridBagConstraints fGbc = new GridBagConstraints();
        fGbc.fill    = GridBagConstraints.HORIZONTAL;
        fGbc.weightx = 1.0;
        fGbc.insets  = new Insets(8, 0, 8, 4);

        // --- Heading (spans 2 columns) ---
        JLabel heading = new JLabel("Create Account", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(UITheme.ACCENT_GOLD);
        GridBagConstraints hGbc = new GridBagConstraints();
        hGbc.gridx = 0; hGbc.gridy = 0; hGbc.gridwidth = 2;
        hGbc.fill = GridBagConstraints.HORIZONTAL;
        hGbc.insets = new Insets(0, 0, 20, 0);
        form.add(heading, hGbc);

        // Row 1 — Role
        lGbc.gridx = 0; lGbc.gridy = 1; form.add(mkLabel("Role"), lGbc);
        fGbc.gridx = 1; fGbc.gridy = 1;
        regRoleCombo = mkRegCombo();
        regRoleCombo.addActionListener(e -> updateRegFields());
        form.add(regRoleCombo, fGbc);

        // Row 2 — Full Name
        lGbc.gridy = 2; form.add(mkLabel("Full Name"), lGbc);
        fGbc.gridy = 2;
        regNameField = mkField();
        form.add(regNameField, fGbc);

        // Row 3 — Email
        lGbc.gridy = 3; form.add(mkLabel("Email"), lGbc);
        fGbc.gridy = 3;
        regEmailField = mkField();
        form.add(regEmailField, fGbc);

        // Row 4 — Password
        lGbc.gridy = 4; form.add(mkLabel("Password"), lGbc);
        fGbc.gridy = 4;
        regPassField = mkPasswordField();
        form.add(regPassField, fGbc);

        // Row 5 — Extra field 1 (Phone / Emergency Contact)
        lGbc.gridy = 5;
        regExtra1Label = mkLabel("Phone No.");
        form.add(regExtra1Label, lGbc);
        fGbc.gridy = 5;
        regExtraField1 = mkField();
        form.add(regExtraField1, fGbc);

        // Row 6 — Extra field 2 (Class — Student only)
        lGbc.gridy = 6;
        regExtra2Label = mkLabel("Class (e.g. 10-A)");
        regExtra2Label.setVisible(false);
        form.add(regExtra2Label, lGbc);
        fGbc.gridy = 6;
        regExtraField2 = mkField();
        regExtraField2.setVisible(false);
        form.add(regExtraField2, fGbc);

        // Row 7 — Register button (spans 2 columns)
        JButton regBtn = mkRegButton("REGISTER");
        regBtn.addActionListener(e -> doRegister());
        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.gridx = 0; btnGbc.gridy = 7; btnGbc.gridwidth = 2;
        btnGbc.fill = GridBagConstraints.HORIZONTAL;
        btnGbc.insets = new Insets(20, 0, 0, 0);
        form.add(regBtn, btnGbc);

        // Add form card, centred in the tab with padding
        GridBagConstraints pGbc = new GridBagConstraints();
        pGbc.insets = new Insets(20, 20, 20, 20);
        pGbc.anchor = GridBagConstraints.CENTER;
        panel.add(form, pGbc);

        updateRegFields();
        return panel;
    }

    // ─── REGISTER FORM HELPERS ─────────────────────────────────────────────────
    /** Label for the register form */
    private JLabel mkLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(UITheme.TEXT_GREY);
        return l;
    }

    /**
     * createStyledTextField — Single source of truth for all register-form text fields.
     * Uses 20 columns (not 0) so GridBagLayout has a meaningful preferred width,
     * AND fill=HORIZONTAL is set at the GBC level so fields always expand.
     */
    private JTextField mkField() {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setOpaque(true);
        tf.setBackground(new Color(18, 25, 45));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(UITheme.ACCENT_GOLD);
        tf.setSelectionColor(UITheme.ACCENT_GOLD);
        tf.setSelectedTextColor(UITheme.BG_DARK);
        tf.setPreferredSize(new Dimension(250, 34));
        tf.setMinimumSize(new Dimension(150, 30));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 80, 130), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    /**
     * createStyledPasswordField — Same spec as mkField() but for passwords.
     */
    private JPasswordField mkPasswordField() {
        JPasswordField pf = new JPasswordField(20);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setOpaque(true);
        pf.setBackground(new Color(18, 25, 45));
        pf.setForeground(Color.WHITE);
        pf.setCaretColor(UITheme.ACCENT_GOLD);
        pf.setSelectionColor(UITheme.ACCENT_GOLD);
        pf.setSelectedTextColor(UITheme.BG_DARK);
        pf.setPreferredSize(new Dimension(250, 34));
        pf.setMinimumSize(new Dimension(150, 30));
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 80, 130), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    /** Register button */
    private JButton mkRegButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(UITheme.ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(250, 40));
        btn.setMinimumSize(new Dimension(150, 36));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Combo-box for role selection in register form */
    private JComboBox<String> mkRegCombo() {
        JComboBox<String> cb = new JComboBox<>(new String[]{"Admin", "Teacher", "Student"});
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setBackground(new Color(18, 25, 45));
        cb.setForeground(Color.WHITE);
        cb.setPreferredSize(new Dimension(250, 34));
        return cb;
    }

    // ─── ACTIONS ───────────────────────────────────────────────────────────────
    private void doLogin() {
        String email = loginEmailField.getText().trim();
        String pass = new String(loginPassField.getPassword());
        int role = loginRoleCombo.getSelectedIndex() + 1;

        if (email.isEmpty() || pass.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        String hashed = SecurityUtils.hashPassword(pass);
        User user = userDao.loginUser(email, hashed, role);

        if (user == null) {
            showError("Invalid credentials. Please check email/password.");
            return;
        }

        dispose();
        if (user instanceof Admin)   new AdminDashboard((Admin) user);
        else if (user instanceof Teacher) new TeacherDashboard((Teacher) user);
        else if (user instanceof Student) new StudentDashboard((Student) user);
    }

    private void doRegister() {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String pass = new String(regPassField.getPassword());
        int role = regRoleCombo.getSelectedIndex() + 1;

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showError("Please fill all required fields.");
            return;
        }
        if (!email.matches("^(.+)@(.+)$")) {
            showError("Invalid email format.");
            return;
        }

        String hashed = SecurityUtils.hashPassword(pass);
        String uid;
        boolean saved = false;
        UserDao dao = new UserDao();

        if (role == 1) {
            uid = "AD-" + String.format("%05d", (int)(Math.random() * 89999 + 10000));
            saved = dao.registerAdmin(new Admin(uid, name, email, hashed));
        } else if (role == 2) {
            String phone = regExtraField1.getText().trim();
            if (!phone.matches("\\d{10,15}")) { showError("Phone must be 10\u201315 digits (numbers only)."); return; }
            uid = "TE-" + String.format("%05d", (int)(Math.random() * 89999 + 10000));
            saved = dao.registerTeacher(new Teacher(uid, name, email, hashed, phone));
        } else {
            String phone = regExtraField1.getText().trim();
            String cls   = regExtraField2.getText().trim();
            // FIX: allow 10\u201315 digit emergency contacts (accommodates +91 prefix stripped)
            if (!phone.matches("\\d{10,15}")) { showError("Emergency contact must be 10\u201315 digits (numbers only)."); return; }
            if (cls.isEmpty()) { showError("Please enter your class (e.g. 10-A)."); return; }
            // FIX: use random 5-digit suffix to avoid timestamp collisions
            uid = "ST-" + String.format("%05d", (int)(Math.random() * 89999 + 10000));
            saved = dao.registerStudent(new Student(uid, name, email, hashed, cls, phone));
        }

        if (saved) showSuccess("Registration successful! You can now login.");
        else showError("Registration failed. Email may already be in use.");
    }

    private void updateRegFields() {
        int role = regRoleCombo.getSelectedIndex() + 1;
        if (role == 1) { // Admin — no extra fields
            regExtra1Label.setText("—"); regExtraField1.setVisible(false); regExtra1Label.setVisible(false);
            regExtra2Label.setVisible(false); regExtraField2.setVisible(false);
        } else if (role == 2) { // Teacher — phone
            regExtra1Label.setText("Phone No."); regExtraField1.setVisible(true); regExtra1Label.setVisible(true);
            regExtra2Label.setVisible(false); regExtraField2.setVisible(false);
        } else { // Student — emergency contact + class
            regExtra1Label.setText("Emergency Contact"); regExtraField1.setVisible(true); regExtra1Label.setVisible(true);
            regExtra2Label.setText("Class"); regExtra2Label.setVisible(true); regExtraField2.setVisible(true);
        }
        revalidate(); repaint();
    }

    // ─── UTILITIES ─────────────────────────────────────────────────────────────
    private JComboBox<String> roleCombo() {
        JComboBox<String> cb = new JComboBox<>(new String[]{"Admin", "Teacher", "Student"});
        UITheme.styleCombo(cb);
        cb.setPreferredSize(new Dimension(220, 36));
        return cb;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(8, 6, 8, 14);
        return g;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
