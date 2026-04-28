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
 * TeacherDashboard â€” portal for Teacher role.
 * Tabs: My Trips | Schedule Editor | Attendance | Complaints | Emergency Log
 */
public class TeacherDashboard extends JFrame {

    private final Teacher    teacher;
    private final TeacherDao teacherDao = new TeacherDao();
    private final TripDao    tripDao    = new TripDao();

    public TeacherDashboard(Teacher teacher) {
        this.teacher = teacher;

        setTitle("ExcursionX â€” Teacher Portal (" + teacher.getName() + ")");
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

    // â”€â”€â”€ HEADER â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JLabel title = UITheme.headerLabel("Teacher Portal");
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

    // â”€â”€â”€ TABS â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_PANEL);
        tabs.setForeground(UITheme.TEXT_WHITE);
        tabs.setFont(UITheme.FONT_SUB);

        tabs.addTab("  My Trips  ",        buildMyTripsTab());
        tabs.addTab("  Schedule  ",        buildScheduleTab());
        tabs.addTab("  Attendance  ",      buildAttendanceTab());
        tabs.addTab("  Complaints  ",      buildComplaintTab());
        tabs.addTab("  Emergency Log  ",   buildEmergencyTab());
        return tabs;
    }

    // â”€â”€â”€ TAB 1: MY TRIPS â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private JPanel buildMyTripsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"Trip ID", "Destination", "Start Date", "End Date", "Seats Left", "Budget (Rs)"};
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
                    t.getCapacity(), "Rs." + t.getTotalBudget()
                });
            }
        };

        JPanel top = UITheme.cardPanel();
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JButton refreshBtn = UITheme.secondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());
        top.add(UITheme.label("Trips assigned to me:"));
        top.add(refreshBtn);

        refresh.run();
        panel.add(top, BorderLayout.NORTH);
        panel.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // â”€â”€â”€ TAB 2: SCHEDULE EDITOR â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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

        JButton saveBtn = UITheme.primaryButton("Save Schedule to File");
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

    // â”€â”€â”€ TAB 3: ATTENDANCE â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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

        JButton markBtn = UITheme.primaryButton("Mark Attendance");
        markBtn.addActionListener(e -> {
            try {
                int    tid    = Integer.parseInt(tripIdField.getText().trim());
                String sid    = studentIdField.getText().trim();
                String status = (String) statusCombo.getSelectedItem();
                if (sid.isEmpty()) { showError("Enter Student ID."); return; }
                teacherDao.markAttendance(tid, sid, status);
                showSuccess("Attendance marked: " + sid + " -> " + status);
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
            "<html><br>Note: Enter a Trip ID and Student ID to mark attendance.<br>"
            + "Student must have an existing booking for that trip.<br>"
            + "Status options: <b>Present</b> | <b>Absent</b> | <b>Excused</b></html>");
        hint.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        info.add(hint);
        return info;
    }

    // â”€â”€â”€ TAB 4: COMPLAINTS â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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

        JButton fileBtn = UITheme.dangerButton("File Complaint");
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

    // â”€â”€â”€ TAB 5: EMERGENCY LOG â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private JPanel buildEmergencyTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // â”€â”€ Load teacher's trips into combo â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        ArrayList<Trip> myTrips = tripDao.getTripsForTeacher(teacher.getId());
        java.util.Vector<String> tripItems = new java.util.Vector<>();
        if (myTrips.isEmpty()) {
            tripItems.add("No trips assigned \u2014 ask Admin to assign you a trip");
        } else {
            for (Trip t : myTrips) tripItems.add(t.getTripId() + "  \u2014  " + t.getDestination());
        }
        JComboBox<String> tripCombo = new JComboBox<>(tripItems);
        UITheme.styleCombo(tripCombo);
        tripCombo.setPreferredSize(new Dimension(280, 32));
        JButton reloadTripsBtn = UITheme.secondaryButton("Reload");
        reloadTripsBtn.setFont(UITheme.FONT_SMALL);

        // â”€â”€ Log Form card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(22, 30, 52));
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 70, 120), 1),
            BorderFactory.createEmptyBorder(18, 24, 18, 24)
        ));

        // label GBC: no fill, left-anchored, fixed width
        GridBagConstraints lG = new GridBagConstraints();
        lG.anchor  = GridBagConstraints.NORTHWEST;
        lG.fill    = GridBagConstraints.NONE;
        lG.weightx = 0;
        lG.insets  = new Insets(10, 4, 10, 12);

        // field GBC: FIX â€” HORIZONTAL fill + weightx=1.0 so fields always expand
        GridBagConstraints fG = new GridBagConstraints();
        fG.fill    = GridBagConstraints.HORIZONTAL;
        fG.weightx = 1.0;
        fG.insets  = new Insets(10, 0, 10, 4);

        // Heading
        JLabel heading = new JLabel("Log Emergency Incident", SwingConstants.LEFT);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        heading.setForeground(UITheme.ERROR_RED);
        GridBagConstraints hG = new GridBagConstraints();
        hG.gridx = 0; hG.gridy = 0; hG.gridwidth = 2;
        hG.fill  = GridBagConstraints.HORIZONTAL;
        hG.insets = new Insets(0, 0, 14, 0);
        form.add(heading, hG);

        // Row 1 \u2014 Select Trip
        lG.gridx = 0; lG.gridy = 1;
        form.add(emLabel("Select Trip:"), lG);
        fG.gridx = 1; fG.gridy = 1;
        JPanel comboRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        comboRow.setOpaque(false);
        comboRow.add(tripCombo);
        comboRow.add(reloadTripsBtn);
        form.add(comboRow, fG);

        // Row 2 \u2014 Contact No  (FIX: fill=HORIZONTAL ensures field expands)
        lG.gridy = 2; form.add(emLabel("Contact No:"), lG);
        fG.gridy = 2;
        JTextField contactField = emField();
        contactField.setText(teacher.getContactNo());
        form.add(contactField, fG);

        // Row 3 \u2014 Description label
        lG.gridy = 3; lG.anchor = GridBagConstraints.WEST;
        form.add(emLabel("Description:"), lG);

        // Row 4 \u2014 Description textarea  (FIX: JTextArea(5,25) fill=BOTH)
        GridBagConstraints taG = new GridBagConstraints();
        taG.gridx = 0; taG.gridy = 4; taG.gridwidth = 2;
        taG.fill    = GridBagConstraints.BOTH;
        taG.weightx = 1.0; taG.weighty = 1.0;
        taG.insets  = new Insets(2, 4, 10, 4);
        JTextArea descArea = emTextArea();
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.getViewport().setBackground(new Color(18, 25, 45));
        descScroll.setBackground(new Color(18, 25, 45));
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 80, 130), 1));
        descScroll.setMinimumSize(new Dimension(200, 110));
        descScroll.setPreferredSize(new Dimension(380, 120));
        form.add(descScroll, taG);

        // Row 5 \u2014 Log button
        JButton logBtn = UITheme.dangerButton("Log Emergency");
        logBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logBtn.setPreferredSize(new Dimension(180, 38));
        GridBagConstraints btnG = new GridBagConstraints();
        btnG.gridx = 0; btnG.gridy = 5; btnG.gridwidth = 2;
        btnG.fill   = GridBagConstraints.NONE;
        btnG.anchor = GridBagConstraints.CENTER;
        btnG.insets = new Insets(12, 0, 6, 0);
        form.add(logBtn, btnG);

        // â”€â”€ Historical logs table â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String[] cols = {"ID", "Reported By", "Description", "Contact", "Logged At"};
        emergencyModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(emergencyModel);
        UITheme.styleTable(table);
        table.setRowHeight(36);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);

        JPanel tableTop = UITheme.cardPanel();
        tableTop.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));
        JButton loadBtn = UITheme.secondaryButton("Load Logs for Selected Trip");
        tableTop.add(UITheme.label("Emergency history for selected trip:"));
        tableTop.add(loadBtn);

        JPanel tableSection = new JPanel(new BorderLayout(4, 4));
        tableSection.setOpaque(false);
        tableSection.add(tableTop, BorderLayout.NORTH);
        tableSection.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, form, tableSection);
        split.setDividerLocation(330);
        split.setBackground(UITheme.BG_DARK);
        split.setBorder(null);
        panel.add(split, BorderLayout.CENTER);

        // â”€â”€ Trip ID extractor â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        java.util.function.Supplier<Integer> getSelectedTripId = () -> {
            String sel = (String) tripCombo.getSelectedItem();
            if (sel == null || sel.contains("No trips")) return -1;
            try { return Integer.parseInt(sel.split("  \u2014  ")[0].trim()); }
            catch (NumberFormatException ex) { return -1; }
        };

        // â”€â”€ Reload trips â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        reloadTripsBtn.addActionListener(e -> {
            tripCombo.removeAllItems();
            ArrayList<Trip> fresh = tripDao.getTripsForTeacher(teacher.getId());
            if (fresh.isEmpty()) {
                tripCombo.addItem("No trips assigned \u2014 ask Admin to assign you a trip");
            } else {
                for (Trip t : fresh) tripCombo.addItem(t.getTripId() + "  \u2014  " + t.getDestination());
            }
        });

        // â”€â”€ Log Emergency â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        logBtn.addActionListener(e -> {
            int tid = getSelectedTripId.get();
            if (tid == -1) { showError("No valid trip selected.\nAsk the Admin to assign you a trip first."); return; }
            String desc    = descArea.getText().trim();
            String contact = contactField.getText().trim();
            if (desc.isEmpty()) { showError("Please type a description in the Description box."); return; }
            boolean ok = teacherDao.logEmergency(tid, teacher.getId(), desc, contact);
            if (ok) {
                showSuccess("Emergency logged successfully for Trip ID: " + tid);
                descArea.setText("");
                refreshEmergencyTable(tid, emergencyModel);
            } else {
                showError("Failed to log emergency.\nVerify Trip ID exists.\nSee console for SQL error details.");
            }
        });

        // â”€â”€ Load historical logs â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        loadBtn.addActionListener(e -> {
            int tid = getSelectedTripId.get();
            if (tid == -1) { showError("No valid trip selected."); return; }
            refreshEmergencyTable(tid, emergencyModel);
        });

        return panel;
    }

    // Held as field so logBtn can refresh the table after a successful insert
    private DefaultTableModel emergencyModel;

    private void refreshEmergencyTable(int tripId, DefaultTableModel model) {
        if (model == null) return;
        model.setRowCount(0);
        for (String[] row : teacherDao.getEmergencyLogs(tripId)) model.addRow(row);
    }

    // â”€â”€â”€ EMERGENCY LOG HELPERS â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Styled label for emergency form */
    private JLabel emLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(UITheme.TEXT_GREY);
        return l;
    }

    /**
     * createStyledTextField for emergency form.
     * 20 columns gives GridBagLayout a real preferred width base.
     * fill=HORIZONTAL on the GBC ensures it always expands to fill the column.
     */
    private JTextField emField() {
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
     * createStyledTextArea for emergency log description.
     * 5 rows x 25 columns. Line-wrap enabled. White text on dark BG.
     */
    private JTextArea emTextArea() {
        JTextArea ta = new JTextArea(5, 25);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ta.setOpaque(true);
        ta.setBackground(new Color(18, 25, 45));
        ta.setForeground(Color.WHITE);
        ta.setCaretColor(UITheme.ACCENT_GOLD);
        ta.setSelectionColor(UITheme.ACCENT_GOLD);
        ta.setSelectedTextColor(UITheme.BG_DARK);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return ta;
    }

    // â”€â”€â”€ UTILITIES â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private GridBagConstraints formGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0;
        g.anchor  = GridBagConstraints.WEST;
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.insets  = new Insets(7, 8, 7, 10);
        return g;
    }

    private void showError(String msg)   { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }
}
