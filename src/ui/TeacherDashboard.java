package ui;

import databases.TeacherDao;
import databases.TripDao;
import models.Teacher;
import models.Trip;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * TeacherDashboard — portal for Teacher role.
 * Tabs: My Trips | Schedule Editor | Attendance | Complaints | Emergency Log
 */
public class TeacherDashboard extends JFrame {

    private final Teacher    teacher;
    private final TeacherDao teacherDao = new TeacherDao();
    private final TripDao    tripDao    = new TripDao();

    public TeacherDashboard(Teacher teacher) {
        this.teacher = teacher;

        setTitle("ExcursionX — Teacher Portal (" + teacher.getName() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 660);
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

        JLabel title = UITheme.headerLabel("👨‍🏫  Teacher Portal");
        JLabel sub   = UITheme.label("Welcome, " + teacher.getName() + "  |  Contact: " + teacher.getContactNo());
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

        tabs.addTab("  ✈ My Trips  ",        buildMyTripsTab());
        tabs.addTab("  📅 Schedule  ",        buildScheduleTab());
        tabs.addTab("  ✅ Attendance  ",      buildAttendanceTab());
        tabs.addTab("  📝 Complaints  ",      buildComplaintTab());
        tabs.addTab("  🚨 Emergency Log  ",   buildEmergencyTab());
        return tabs;
    }

    // ─── TAB 1: MY TRIPS ─────────────────────────────────────────────────────────
    private JPanel buildMyTripsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"Trip ID", "Destination", "Start Date", "End Date", "Seats Left", "Budget ₹"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Trip t : tripDao.getTripsForTeacher(teacher.getId())) {
                model.addRow(new Object[]{
                    t.getTripId(), t.getDestination(),
                    t.getStartDate(), t.getEndDate(),
                    t.getCapacity(), "₹" + t.getTotalBudget()
                });
            }
        };

        JPanel top = UITheme.cardPanel();
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JButton refreshBtn = UITheme.secondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refresh.run());
        top.add(UITheme.label("Trips assigned to me:"));
        top.add(refreshBtn);

        refresh.run();
        panel.add(top, BorderLayout.NORTH);
        panel.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ─── TAB 2: SCHEDULE EDITOR ──────────────────────────────────────────────────
    private JPanel buildScheduleTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel top = UITheme.cardPanel();
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JTextField tripNameField = UITheme.textField();
        tripNameField.setPreferredSize(new Dimension(200, 32));
        tripNameField.setToolTipText("Enter trip name for the file");
        top.add(UITheme.label("Trip Name:"));
        top.add(tripNameField);

        JTextArea scheduleArea = UITheme.textArea();
        scheduleArea.setText("Day 1: Departure from school @ 06:00 AM\n"
                           + "Day 1: Arrive at destination\n"
                           + "Day 2: Morning hike + activities\n"
                           + "Day 3: Cultural visit / sightseeing\n"
                           + "Day 4: Return journey\n");
        JScrollPane scroll = UITheme.scrollPane(scheduleArea);

        JButton saveBtn = UITheme.primaryButton("💾 Save Schedule to File");
        saveBtn.addActionListener(e -> {
            String name = tripNameField.getText().trim();
            if (name.isEmpty()) { showError("Enter a trip name first."); return; }
            teacherDao.writeScheduleFile(name, scheduleArea.getText());
            showSuccess("Schedule saved as: " + name.replaceAll("\\s+", "_") + "_Schedule.txt");
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(saveBtn);

        panel.add(top,    BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // ─── TAB 3: ATTENDANCE ───────────────────────────────────────────────────────
    private JPanel buildAttendanceTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel form = UITheme.cardPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = formGbc();

        JTextField tripIdField    = UITheme.textField(); tripIdField.setPreferredSize(new Dimension(120, 32));
        JTextField studentIdField = UITheme.textField(); studentIdField.setPreferredSize(new Dimension(180, 32));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present", "Absent", "Excused"});
        UITheme.styleCombo(statusCombo);

        form.add(UITheme.label("Mark Attendance"), g); g.gridwidth = 1;
        g.gridy++;
        form.add(UITheme.label("Trip ID:"),     g); g.gridx = 1; form.add(tripIdField, g);
        g.gridy++; g.gridx = 0;
        form.add(UITheme.label("Student ID:"),  g); g.gridx = 1; form.add(studentIdField, g);
        g.gridy++; g.gridx = 0;
        form.add(UITheme.label("Status:"),      g); g.gridx = 1; form.add(statusCombo, g);

        JButton markBtn = UITheme.primaryButton("✅ Mark Attendance");
        markBtn.addActionListener(e -> {
            try {
                int    tid    = Integer.parseInt(tripIdField.getText().trim());
                String sid    = studentIdField.getText().trim();
                String status = (String) statusCombo.getSelectedItem();
                if (sid.isEmpty()) { showError("Enter Student ID."); return; }
                teacherDao.markAttendance(tid, sid, status);
                showSuccess("Attendance marked: " + sid + " → " + status);
            } catch (NumberFormatException ex) { showError("Trip ID must be a number."); }
        });

        g.gridy++; g.gridx = 0; g.gridwidth = 2;
        g.insets = new Insets(16, 8, 8, 8);
        form.add(markBtn, g);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(form);

        panel.add(wrapper, BorderLayout.NORTH);
        panel.add(buildAttendanceInfo(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAttendanceInfo() {
        JPanel info = UITheme.cardPanel();
        info.setLayout(new BorderLayout());
        JLabel hint = UITheme.label(
            "<html><br>ℹ️  Enter a Trip ID and Student ID to mark attendance.<br>"
            + "Student must have an existing booking for that trip.<br>"
            + "Status options: <b>Present</b> | <b>Absent</b> | <b>Excused</b></html>");
        hint.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        info.add(hint);
        return info;
    }

    // ─── TAB 4: COMPLAINTS ───────────────────────────────────────────────────────
    private JPanel buildComplaintTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel top = UITheme.cardPanel();
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JTextField studentIdField = UITheme.textField();
        studentIdField.setPreferredSize(new Dimension(170, 32));
        top.add(UITheme.label("Student ID:"));
        top.add(studentIdField);

        JTextArea issueArea = UITheme.textArea();
        issueArea.setRows(6);
        issueArea.setToolTipText("Describe the conduct issue in detail");
        JScrollPane scroll = UITheme.scrollPane(issueArea);

        JButton fileBtn = UITheme.dangerButton("⚠ File Complaint");
        fileBtn.addActionListener(e -> {
            String sid   = studentIdField.getText().trim();
            String issue = issueArea.getText().trim();
            if (sid.isEmpty() || issue.isEmpty()) { showError("Fill in both fields."); return; }
            teacherDao.fileComplaint(teacher.getId(), sid, issue);
            showSuccess("Complaint filed against student: " + sid);
            issueArea.setText("");
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(fileBtn);

        panel.add(top,    BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // ─── TAB 5: EMERGENCY LOG ────────────────────────────────────────────────────
    private JPanel buildEmergencyTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Log Form ──
        JPanel form = UITheme.cardPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = formGbc();

        JTextField tripIdField  = UITheme.textField(); tripIdField.setPreferredSize(new Dimension(120, 32));
        JTextField contactField = UITheme.textField(); contactField.setPreferredSize(new Dimension(160, 32));
        contactField.setText(teacher.getContactNo());
        JTextArea  descArea     = UITheme.textArea(); descArea.setRows(3);
        descArea.setToolTipText("Describe the emergency in detail");
        JScrollPane descScroll  = UITheme.scrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(400, 90));

        g.gridwidth = 2;
        JLabel heading = UITheme.label("🚨  Log Emergency Incident");
        heading.setFont(UITheme.FONT_SUB);
        heading.setForeground(UITheme.ERROR_RED);
        form.add(heading, g);

        g.gridy++; g.gridwidth = 1;
        form.add(UITheme.label("Trip ID:"),   g); g.gridx = 1; form.add(tripIdField, g);
        g.gridy++; g.gridx = 0;
        form.add(UITheme.label("Contact No:"), g); g.gridx = 1; form.add(contactField, g);
        g.gridy++; g.gridx = 0; g.gridwidth = 2;
        form.add(UITheme.label("Description:"), g);
        g.gridy++;
        form.add(descScroll, g);

        JButton logBtn = UITheme.dangerButton("🚨 Log Emergency");
        logBtn.addActionListener(e -> {
            try {
                int    tid     = Integer.parseInt(tripIdField.getText().trim());
                String desc    = descArea.getText().trim();
                String contact = contactField.getText().trim();
                if (desc.isEmpty()) { showError("Describe the emergency."); return; }
                boolean ok = teacherDao.logEmergency(tid, teacher.getId(), desc, contact);
                if (ok) {
                    showSuccess("Emergency logged for Trip ID: " + tid);
                    descArea.setText("");
                    refreshEmergencyTable(tid, emergencyModel);
                } else showError("Failed to log emergency. Check Trip ID.");
            } catch (NumberFormatException ex) { showError("Trip ID must be a number."); }
        });

        g.gridy++; g.gridwidth = 2; g.insets = new Insets(12, 8, 8, 8);
        form.add(logBtn, g);

        // ── Historical logs table ──
        String[] cols = {"ID", "Reported By", "Description", "Contact", "Logged At"};
        emergencyModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(emergencyModel);
        UITheme.styleTable(table);
        table.setRowHeight(36);

        JPanel tableTop = UITheme.cardPanel();
        tableTop.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));
        JTextField viewTripField = UITheme.textField(); viewTripField.setPreferredSize(new Dimension(90, 30));
        JButton loadBtn = UITheme.secondaryButton("📋 Load Logs for Trip");
        loadBtn.addActionListener(e -> {
            try {
                int tid = Integer.parseInt(viewTripField.getText().trim());
                refreshEmergencyTable(tid, emergencyModel);
            } catch (NumberFormatException ex) { showError("Enter a valid Trip ID."); }
        });
        tableTop.add(UITheme.label("Trip ID:")); tableTop.add(viewTripField); tableTop.add(loadBtn);

        JPanel tableSection = new JPanel(new BorderLayout(4, 4));
        tableSection.setOpaque(false);
        tableSection.add(tableTop, BorderLayout.NORTH);
        tableSection.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, form, tableSection);
        split.setDividerLocation(280);
        split.setBackground(UITheme.BG_DARK);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // Held as field so the Log button can refresh it
    private DefaultTableModel emergencyModel;

    private void refreshEmergencyTable(int tripId, DefaultTableModel model) {
        if (model == null) return;
        model.setRowCount(0);
        for (String[] row : teacherDao.getEmergencyLogs(tripId)) model.addRow(row);
    }

    // ─── UTILITIES ────────────────────────────────────────────────────────────────
    private GridBagConstraints formGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(7, 8, 7, 10);
        return g;
    }

    private void showError(String msg)   { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }
}
