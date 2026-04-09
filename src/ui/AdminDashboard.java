package ui;

import databases.BookingDao;
import databases.TripDao;
import databases.TeacherDao;
import models.Admin;
import models.Trip;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * AdminDashboard — full control panel for Admin role.
 * Tabs: Manage Trips | Trip Manifest | Payments | Analytics | User Management | Emergency Logs
 */
public class AdminDashboard extends JFrame {

    private final Admin      admin;
    private final TripDao    tripDao    = new TripDao();
    private final BookingDao bookingDao = new BookingDao();
    private final TeacherDao teacherDao = new TeacherDao();

    public AdminDashboard(Admin admin) {
        this.admin = admin;

        setTitle("ExcursionX — Admin Control Panel (" + admin.getName() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        setContentPane(root);
        setVisible(true);
    }

    // ─── HEADER ──────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JLabel title = UITheme.headerLabel("✈  Admin Control Panel");
        JLabel sub   = UITheme.label("Logged in as: " + admin.getName() + "  |  " + admin.getEmail());
        JPanel left  = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title); left.add(sub);

        JButton logoutBtn = UITheme.secondaryButton("Logout");
        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame(); });
        header.add(left,      BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);
        return header;
    }

    // ─── TABS ─────────────────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_PANEL);
        tabs.setForeground(UITheme.TEXT_WHITE);
        tabs.setFont(UITheme.FONT_SUB);

        tabs.addTab("  🗺 Manage Trips  ",  buildManageTripsTab());
        tabs.addTab("  📋 Manifest  ",      buildManifestTab());
        tabs.addTab("  💳 Payments  ",      buildPaymentsTab());
        tabs.addTab("  📊 Analytics  ",     buildAnalyticsTab());
        tabs.addTab("  👥 Users  ",         buildUserManagementTab());
        tabs.addTab("  🚨 Emergencies  ",   buildEmergencyLogsTab());
        return tabs;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  TAB 1 — MANAGE TRIPS
    // ═══════════════════════════════════════════════════════════════════════════════
    private JPanel buildManageTripsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // ── FORM using BoxLayout rows ─────────────────────────────────────────────
        JPanel form = UITheme.cardPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // Row 1 — Destination | Start Date | End Date
        JPanel row1 = formRow();
        JTextField destField  = styledField(200, "e.g.  Manali Snow Adventure");
        JTextField startField = styledField(120, "yyyy-MM-dd  (optional)");
        JTextField endField   = styledField(120, "yyyy-MM-dd  (optional)");
        row1.add(fieldLabel("Destination *")); row1.add(destField);
        row1.add(hgap()); row1.add(fieldLabel("Start Date")); row1.add(startField);
        row1.add(hgap()); row1.add(fieldLabel("End Date"));   row1.add(endField);

        // Row 2 — Capacity | Budget | Trip ID (for edit ops)
        JPanel row2 = formRow();
        JTextField capField    = styledField(90,  "e.g.  40");
        JTextField budgetField = styledField(120, "e.g.  35000");
        JTextField tidField    = styledField(80,  "click a row");
        row2.add(fieldLabel("Capacity *")); row2.add(capField);
        row2.add(hgap()); row2.add(fieldLabel("Budget ₹ *")); row2.add(budgetField);
        row2.add(hgap()); row2.add(fieldLabel("Trip ID  (Update / Delete)")); row2.add(tidField);

        // Row 3 — Action Buttons
        JPanel row3 = formRow();
        JButton createBtn = UITheme.primaryButton("  ➕  Add Trip  ");
        JButton updateBtn = UITheme.secondaryButton("  ✏  Update Trip  ");
        JButton deleteBtn = UITheme.dangerButton("  🗑  Delete Trip  ");

        JButton clrBtn    = new JButton("✖  Clear Form");
        clrBtn.setFont(UITheme.FONT_BODY);
        clrBtn.setBackground(new Color(45, 55, 85));
        clrBtn.setForeground(UITheme.TEXT_GREY);
        clrBtn.setFocusPainted(false);
        clrBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        clrBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clrBtn.setOpaque(true);
        row3.add(createBtn); row3.add(Box.createHorizontalStrut(10));
        row3.add(updateBtn); row3.add(Box.createHorizontalStrut(10));
        row3.add(deleteBtn); row3.add(Box.createHorizontalStrut(10));
        row3.add(clrBtn);

        form.add(row1);
        form.add(Box.createVerticalStrut(10));
        form.add(row2);
        form.add(Box.createVerticalStrut(12));
        form.add(row3);

        // ── TABLE ─────────────────────────────────────────────────────────────────
        String[] cols = {"Trip ID", "Destination", "Start Date", "End Date", "Seats Left", "Budget ₹", "Teacher ID"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);

        // Refresh helper
        Runnable refresh = () -> {
            model.setRowCount(0);
            ArrayList<Trip> trips = tripDao.getAllTrips();
            for (Trip t : trips) {
                model.addRow(new Object[]{
                    t.getTripId(),
                    t.getDestination(),
                    (t.getStartDate() != null && !t.getStartDate().isEmpty()) ? t.getStartDate() : "—",
                    (t.getEndDate()   != null && !t.getEndDate().isEmpty())   ? t.getEndDate()   : "—",
                    t.getCapacity(),
                    "₹" + (long) t.getTotalBudget(),
                    (t.getTeacherId() != null && !t.getTeacherId().isEmpty()) ? t.getTeacherId() : "—"
                });
            }
        };

        // Click a row → auto-fill form fields
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row < 0) return;
            tidField.setText(String.valueOf(model.getValueAt(row, 0)));
            destField.setText(String.valueOf(model.getValueAt(row, 1)));
            String sd = model.getValueAt(row, 2).toString(); startField.setText(sd.equals("—") ? "" : sd);
            String ed = model.getValueAt(row, 3).toString(); endField.setText(ed.equals("—") ? "" : ed);
            capField.setText(String.valueOf(model.getValueAt(row, 4)));
            budgetField.setText(model.getValueAt(row, 5).toString().replace("₹", ""));
        });

        // ── CREATE ──────────────────────────────────────────────────────────────
        createBtn.addActionListener(e -> {
            String dest = destField.getText().trim();
            String cap  = capField.getText().trim();
            String bgt  = budgetField.getText().trim();

            if (dest.isEmpty())  { showError("Destination cannot be empty."); return; }
            if (cap.isEmpty())   { showError("Capacity cannot be empty."); return; }
            if (bgt.isEmpty())   { showError("Budget cannot be empty."); return; }

            int    capacity;
            double budget;
            try { capacity = Integer.parseInt(cap); }
            catch (NumberFormatException ex) { showError("Capacity must be a whole number (e.g. 40)."); return; }
            try { budget = Double.parseDouble(bgt); }
            catch (NumberFormatException ex) { showError("Budget must be a number (e.g. 35000)."); return; }
            if (capacity <= 0) { showError("Capacity must be greater than 0."); return; }
            if (budget   <= 0) { showError("Budget must be greater than 0."); return; }

            Trip t = new Trip(0, dest,
                    startField.getText().trim(), endField.getText().trim(),
                    capacity, budget, "");
            boolean ok = tripDao.createTrip(t);
            if (ok) {
                showSuccess("✅  Trip \"" + dest + "\" created successfully!");
                clearForm(destField, startField, endField, capField, budgetField, tidField);
                refresh.run();
            } else {
                showError("❌  Failed to create trip.\n\nCheck that MySQL is running and the 'excursionx' database exists.\nSee console for SQL error details.");
            }
        });

        // ── UPDATE ──────────────────────────────────────────────────────────────
        updateBtn.addActionListener(e -> {
            String tidTxt = tidField.getText().trim();
            String dest   = destField.getText().trim();
            String cap    = capField.getText().trim();
            String bgt    = budgetField.getText().trim();

            if (tidTxt.isEmpty()) { showError("Trip ID is required for Update.\nClick a row in the table to select it."); return; }
            if (dest.isEmpty())   { showError("Destination cannot be empty."); return; }
            if (cap.isEmpty())    { showError("Capacity cannot be empty."); return; }
            if (bgt.isEmpty())    { showError("Budget cannot be empty."); return; }

            int tripId, capacity;
            double budget;
            try { tripId   = Integer.parseInt(tidTxt); }
            catch (NumberFormatException ex) { showError("Trip ID must be a number. Select a row from the table."); return; }
            try { capacity = Integer.parseInt(cap); }
            catch (NumberFormatException ex) { showError("Capacity must be a whole number (e.g. 40)."); return; }
            try { budget   = Double.parseDouble(bgt); }
            catch (NumberFormatException ex) { showError("Budget must be a number (e.g. 35000)."); return; }

            boolean ok = tripDao.updateTrip(tripId, dest,
                    startField.getText().trim(), endField.getText().trim(),
                    capacity, budget);
            if (ok) {
                showSuccess("✅  Trip ID " + tripId + " updated successfully!");
                refresh.run();
            } else {
                showError("❌  Update failed for Trip ID " + tripId + ".\nMake sure that Trip ID exists.");
            }
        });

        // ── DELETE ──────────────────────────────────────────────────────────────
        deleteBtn.addActionListener(e -> {
            // Use selected row first; fall back to typed ID
            int selectedRow = table.getSelectedRow();
            int tripId;
            String tripName;

            if (selectedRow >= 0) {
                tripId   = (int) model.getValueAt(selectedRow, 0);
                tripName = (String) model.getValueAt(selectedRow, 1);
            } else {
                String tidTxt = tidField.getText().trim();
                if (tidTxt.isEmpty()) {
                    showError("Select a row in the table OR enter a Trip ID in the 'Trip ID' field.");
                    return;
                }
                try { tripId = Integer.parseInt(tidTxt); tripName = "ID " + tripId; }
                catch (NumberFormatException ex) {
                    showError("Trip ID must be a number.");
                    return;
                }
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "<html>Delete trip: <b>" + tripName + "</b> (ID: " + tripId + ")?<br>"
                    + "<font color='red'>All bookings for this trip will also be removed.</font></html>",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean ok = tripDao.deleteTrip(tripId);
            if (ok) {
                showSuccess("✅  Trip \"" + tripName + "\" deleted.");
                clearForm(destField, startField, endField, capField, budgetField, tidField);
                refresh.run();
            } else {
                showError("❌  Delete failed for Trip ID " + tripId + ".\nCheck console for details.");
            }
        });

        // ── CLEAR ───────────────────────────────────────────────────────────────
        clrBtn.addActionListener(e -> {
            clearForm(destField, startField, endField, capField, budgetField, tidField);
            table.clearSelection();
        });

        refresh.run();
        panel.add(form,                        BorderLayout.NORTH);
        panel.add(UITheme.scrollPane(table),   BorderLayout.CENTER);

        // Status hint below table
        JLabel hint = UITheme.label("  💡  Click a row to auto-fill the form, then press Update or Delete.");
        hint.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    /** Clears all form fields */
    private void clearForm(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  TAB 2 — MANIFEST
    // ═══════════════════════════════════════════════════════════════════════════════
    private JPanel buildManifestTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = UITheme.cardPanel();
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 8));
        JTextField tripIdField = styledField(90, "Trip ID");
        JButton viewBtn = UITheme.primaryButton("📋 Load Manifest");
        top.add(fieldLabel("Trip ID:")); top.add(tripIdField); top.add(viewBtn);

        String[] cols = {"Student Name", "Email", "Class", "Payment", "Attendance"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        viewBtn.addActionListener(ev -> {
            String t = tripIdField.getText().trim();
            if (t.isEmpty()) { showError("Enter a Trip ID."); return; }
            try {
                int id = Integer.parseInt(t);
                model.setRowCount(0);
                ArrayList<String[]> data = tripDao.getManifestList(id);
                for (String[] row : data) model.addRow(row);
                if (data.isEmpty()) showInfo("No bookings found for Trip ID " + id + ".");
            } catch (NumberFormatException ex) { showError("Trip ID must be a number."); }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  TAB 3 — PAYMENTS
    // ═══════════════════════════════════════════════════════════════════════════════
    private JPanel buildPaymentsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = UITheme.cardPanel();
        form.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
        JTextField tripIdField    = styledField(80, "Trip ID");
        JTextField studentIdField = styledField(130, "Student ID");
        String[]   statuses       = {"Pending", "Paid", "Refunded"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        UITheme.styleCombo(statusCombo);
        JButton updateBtn = UITheme.primaryButton("💳 Override Status");
        form.add(fieldLabel("Trip ID:")); form.add(tripIdField);
        form.add(fieldLabel("Student ID:")); form.add(studentIdField);
        form.add(fieldLabel("Status:")); form.add(statusCombo);
        form.add(updateBtn);

        String[] cols = {"Pay ID", "Student", "Destination", "Amount", "Method", "Status", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (String[] row : bookingDao.getAllPayments()) model.addRow(row);
        };

        updateBtn.addActionListener(ev -> {
            String tid = tripIdField.getText().trim();
            String sid = studentIdField.getText().trim();
            if (tid.isEmpty() || sid.isEmpty()) { showError("Enter both Trip ID and Student ID."); return; }
            try {
                tripDao.updatePaymentStatus(sid, Integer.parseInt(tid), (String) statusCombo.getSelectedItem());
                showSuccess("Payment status updated!");
                refresh.run();
            } catch (NumberFormatException ex) { showError("Trip ID must be a number."); }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JButton refreshBtn = UITheme.secondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refresh.run());
        JPanel rp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rp.setOpaque(false); rp.add(refreshBtn);
        topPanel.add(form, BorderLayout.NORTH);
        topPanel.add(rp,   BorderLayout.SOUTH);

        refresh.run();
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  TAB 4 — ANALYTICS
    // ═══════════════════════════════════════════════════════════════════════════════
    private JPanel buildAnalyticsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel cards = new JPanel(new GridLayout(2, 3, 16, 16));
        cards.setOpaque(false);

        HashMap<String, String> stats = bookingDao.getAnalytics();
        cards.add(statCard("✈  Total Trips",      stats.getOrDefault("totalTrips",     "—"), UITheme.ACCENT_GOLD));
        cards.add(statCard("🎓 Students",          stats.getOrDefault("totalStudents",  "—"), UITheme.ACCENT_BLUE));
        cards.add(statCard("👨‍🏫 Teachers",          stats.getOrDefault("totalTeachers",  "—"), UITheme.SUCCESS_GREEN));
        cards.add(statCard("📅 Total Bookings",    stats.getOrDefault("totalBookings",  "—"), new Color(160, 100, 240)));
        cards.add(statCard("💰 Revenue Collected", stats.getOrDefault("totalRevenue",   "—"), UITheme.SUCCESS_GREEN));
        cards.add(statCard("⏳ Pending Payments",  stats.getOrDefault("pendingPayments","—"), UITheme.ERROR_RED));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        JButton refreshBtn = UITheme.primaryButton("🔄 Refresh Analytics");
        refreshBtn.addActionListener(e -> { dispose(); new AdminDashboard(admin); });
        bottom.add(refreshBtn);

        panel.add(cards,  BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel statCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("SansSerif", Font.BOLD, 36));
        val.setForeground(accent);
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(UITheme.FONT_SUB);
        lbl.setForeground(UITheme.TEXT_GREY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(4, 4, 4, 4);
        card.add(val, gbc); gbc.gridy = 1; card.add(lbl, gbc);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  TAB 5 — USER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════════
    private JPanel buildUserManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        String[] cols = {"ID", "Name", "Email", "Role", "Contact / Class"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (String[] u : teacherDao.getAllUsers()) model.addRow(u);
        };

        JPanel top = UITheme.cardPanel();
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JButton refreshBtn = UITheme.secondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refresh.run());
        top.add(fieldLabel("All registered teachers and students:"));
        top.add(refreshBtn);

        refresh.run();
        panel.add(top, BorderLayout.NORTH);
        panel.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  TAB 6 — EMERGENCY LOGS
    // ═══════════════════════════════════════════════════════════════════════════════
    private JPanel buildEmergencyLogsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        String[] cols = {"ID", "Trip Destination", "Reported By", "Description", "Contact", "Logged At"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);
        table.setRowHeight(36);
        table.getColumnModel().getColumn(3).setPreferredWidth(320);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (String[] row : teacherDao.getAllEmergencyLogs()) model.addRow(row);
        };

        JPanel top = UITheme.cardPanel();
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JButton refreshBtn = UITheme.secondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refresh.run());
        top.add(fieldLabel("Emergency incidents across all trips:"));
        top.add(refreshBtn);

        refresh.run();
        panel.add(top, BorderLayout.NORTH);
        panel.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  FORM HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════════

    /** A horizontal FlowLayout row for form fields */
    private JPanel formRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    /** Fixed-width text field with placeholder tooltip */
    private JTextField styledField(int width, String tooltip) {
        JTextField tf = UITheme.textField();
        tf.setPreferredSize(new Dimension(width, 34));
        tf.setToolTipText(tooltip);
        return tf;
    }

    /** Styled label for form rows */
    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_GREY);
        return lbl;
    }

    /** Horizontal gap spacer */
    private Component hgap() { return Box.createHorizontalStrut(18); }

    // ─── DIALOGS ─────────────────────────────────────────────────────────────────
    private void showError(String msg)   { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }
    private void showInfo(String msg)    { JOptionPane.showMessageDialog(this, msg, "Info",    JOptionPane.INFORMATION_MESSAGE); }
}
