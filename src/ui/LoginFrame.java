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
    private JPanel buildRegisterTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_PANEL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(14, 40, 14, 40));

        GridBagConstraints gbc = gbc();

        JLabel heading = UITheme.headerLabel("Create Account");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        gbc.gridwidth = 2;
        form.add(heading, gbc);

        gbc.gridwidth = 1;

        gbc.gridy++; gbc.gridx = 0;
        form.add(UITheme.label("Role"), gbc);
        gbc.gridx = 1;
        regRoleCombo = roleCombo();
        regRoleCombo.addActionListener(e -> updateRegFields());
        form.add(regRoleCombo, gbc);

        gbc.gridy++; gbc.gridx = 0;
        form.add(UITheme.label("Full Name"), gbc);
        gbc.gridx = 1;
        regNameField = UITheme.textField();
        regNameField.setPreferredSize(new Dimension(220, 36));
        form.add(regNameField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        form.add(UITheme.label("Email"), gbc);
        gbc.gridx = 1;
        regEmailField = UITheme.textField();
        regEmailField.setPreferredSize(new Dimension(220, 36));
        form.add(regEmailField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        form.add(UITheme.label("Password"), gbc);
        gbc.gridx = 1;
        regPassField = UITheme.passwordField();
        regPassField.setPreferredSize(new Dimension(220, 36));
        form.add(regPassField, gbc);

        // Dynamic extra fields
        gbc.gridy++; gbc.gridx = 0;
        regExtra1Label = UITheme.label("Phone No.");
        form.add(regExtra1Label, gbc);
        gbc.gridx = 1;
        regExtraField1 = UITheme.textField();
        regExtraField1.setPreferredSize(new Dimension(220, 36));
        form.add(regExtraField1, gbc);

        gbc.gridy++; gbc.gridx = 0;
        regExtra2Label = UITheme.label("Class");
        regExtra2Label.setVisible(false);
        form.add(regExtra2Label, gbc);
        gbc.gridx = 1;
        regExtraField2 = UITheme.textField();
        regExtraField2.setPreferredSize(new Dimension(220, 36));
        regExtraField2.setVisible(false);
        form.add(regExtraField2, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 0, 0, 0);
        JButton regBtn = UITheme.secondaryButton("  REGISTER  ");
        regBtn.setPreferredSize(new Dimension(310, 42));
        regBtn.addActionListener(e -> doRegister());
        form.add(regBtn, gbc);

        updateRegFields();
        panel.add(form);
        return panel;
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
            uid = "AD-" + (System.currentTimeMillis() % 10000);
            saved = dao.registerAdmin(new Admin(uid, name, email, hashed));
        } else if (role == 2) {
            String phone = regExtraField1.getText().trim();
            if (!phone.matches("\\d{10}")) { showError("Phone must be 10 digits."); return; }
            uid = "TE-" + (System.currentTimeMillis() % 10000);
            saved = dao.registerTeacher(new Teacher(uid, name, email, hashed, phone));
        } else {
            String phone = regExtraField1.getText().trim();
            String cls   = regExtraField2.getText().trim();
            if (!phone.matches("\\d{10}")) { showError("Emergency contact must be 10 digits."); return; }
            if (cls.isEmpty()) { showError("Please enter your class."); return; }
            uid = "ST-" + (System.currentTimeMillis() % 10000);
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
