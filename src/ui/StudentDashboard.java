package ui;

import databases.BookingDao;
import databases.TeacherDao;
import databases.TripDao;
import models.Student;
import models.Trip;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * StudentDashboard — self-service portal for Student role.
 * Tabs: Browse Trips (book + pay) | My Bookings | My Conduct Record
 */
public class StudentDashboard extends JFrame {

    private final Student    student;
    private final TripDao    tripDao    = new TripDao();
    private final BookingDao bookingDao = new BookingDao();
    private final TeacherDao teacherDao = new TeacherDao();

    public StudentDashboard(Student student) {
        this.student = student;

        setTitle("ExcursionX — Student Portal (" + student.getName() + ")");
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

        JLabel title = UITheme.headerLabel("🎓  Student Portal");
        JLabel sub   = UITheme.label("Welcome, " + student.getName()
                + "  |  Class: " + student.getStudentClass()
                + "  |  ID: " + student.getId());
        JPanel left = new JPanel(new GridLayout(2, 1));
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

        tabs.addTab("  ✈ Browse & Book  ",  buildBrowseTab());
        tabs.addTab("  📋 My Bookings  ",   buildBookingsTab());
        tabs.addTab("  📁 My Record  ",     buildRecordTab());
        return tabs;
    }

    // ─── TAB 1: BROWSE & BOOK ────────────────────────────────────────────────────
    private JPanel buildBrowseTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Trips table
        String[] cols = {"Trip ID", "Destination", "Start Date", "End Date", "Seats Left", "Budget ₹"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        Runnable refreshTrips = () -> {
            model.setRowCount(0);
            for (Trip t : tripDao.getAllTrips()) {
                model.addRow(new Object[]{
                    t.getTripId(), t.getDestination(),
                    t.getStartDate(), t.getEndDate(),
                    t.getCapacity(), "₹" + t.getTotalBudget()
                });
            }
        };

        // Action buttons
        JButton bookBtn    = UITheme.primaryButton("📌 Book Selected Trip");
        JButton refreshBtn = UITheme.secondaryButton("🔄 Refresh");

        bookBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Select a trip from the table first."); return; }

            int    tripId      = (int)    model.getValueAt(row, 0);
            String destination = (String) model.getValueAt(row, 1);
            String startDate   = (String) model.getValueAt(row, 2);
            String endDate     = (String) model.getValueAt(row, 3);
            int    seatsLeft   = (Integer) model.getValueAt(row, 4);
            String budgetStr   = model.getValueAt(row, 5).toString().replace("₹", "");

            if (seatsLeft <= 0) {
                showError("This trip is fully booked — no seats available!");
                return;
            }

            // Confirm dialog
            String msg = "<html><b>Confirm Booking</b><br><br>"
                       + "Destination: <b>" + destination + "</b><br>"
                       + "Dates: " + startDate + " → " + endDate + "<br>"
                       + "Budget: ₹" + budgetStr + "<br><br>"
                       + "Proceed to book?</html>";
            int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirm Booking",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            // ── Thread-safe booking ──
            Thread bookingThread = new Thread(() -> {
                boolean success = bookingDao.bookTrip(student.getId(), tripId);
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        refreshTrips.run();
                        // Offer immediate payment
                        int payNow = JOptionPane.showConfirmDialog(this,
                                "Seat booked successfully! 🎉\n\nMake payment now?",
                                "Booking Confirmed", JOptionPane.YES_NO_OPTION);
                        if (payNow == JOptionPane.YES_OPTION) {
                            showPaymentDialog(tripId, Double.parseDouble(budgetStr));
                        }
                    } else {
                        showError("Booking failed — seat may be full or already booked.");
                    }
                });
            });
            bookingThread.setName("BookingThread-" + student.getId());
            bookingThread.start();
        });

        refreshBtn.addActionListener(e -> refreshTrips.run());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.setOpaque(false);
        bottom.add(refreshBtn);
        bottom.add(bookBtn);

        refreshTrips.run();
        panel.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        panel.add(bottom,                    BorderLayout.SOUTH);
        return panel;
    }

    // ─── PAYMENT DIALOG ──────────────────────────────────────────────────────────
    /**
     * Simulates a payment flow with method selection.
     * Demonstrates polymorphism: same dialog for any booking amount.
     */
    private void showPaymentDialog(int tripId, double amount) {
        // Check if already paid
        if (bookingDao.isAlreadyPaid(student.getId(), tripId)) {
            showInfo("This booking has already been paid for.");
            return;
        }

        JDialog dialog = new JDialog(this, "💳 Make Payment", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 6, 6, 6);
        g.gridwidth = 2;

        JLabel heading = UITheme.headerLabel("Payment");
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(heading, g);

        g.gridy = 1; g.gridwidth = 1;
        panel.add(UITheme.label("Trip ID:"), g);
        g.gridx = 1;
        JLabel tripLabel = UITheme.label("  #" + tripId);
        tripLabel.setForeground(UITheme.TEXT_WHITE);
        panel.add(tripLabel, g);

        g.gridy = 2; g.gridx = 0;
        panel.add(UITheme.label("Amount:"), g);
        g.gridx = 1;
        JLabel amtLabel = new JLabel("  ₹" + String.format("%.0f", amount));
        amtLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        amtLabel.setForeground(UITheme.SUCCESS_GREEN);
        panel.add(amtLabel, g);

        g.gridy = 3; g.gridx = 0;
        panel.add(UITheme.label("Method:"), g);
        g.gridx = 1;
        JComboBox<String> methodCombo = new JComboBox<>(new String[]{"UPI", "Card", "Cash", "Net Banking"});
        UITheme.styleCombo(methodCombo);
        panel.add(methodCombo, g);

        g.gridy = 4; g.gridx = 0; g.gridwidth = 2;
        g.insets = new Insets(18, 6, 6, 6);
        JButton payBtn = UITheme.primaryButton("  ✅ PAY NOW  ");
        payBtn.setPreferredSize(new Dimension(280, 42));
        payBtn.addActionListener(ev -> {
            String method = (String) methodCombo.getSelectedItem();
            boolean ok = bookingDao.makePayment(student.getId(), tripId, amount, method);
            dialog.dispose();
            if (ok) showSuccess("Payment of ₹" + String.format("%.0f", amount) + " via " + method + " — CONFIRMED! ✅");
            else    showError("Payment failed. Please try again.");
        });
        panel.add(payBtn, g);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ─── TAB 2: MY BOOKINGS ───────────────────────────────────────────────────────
    private JPanel buildBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"Destination", "Start Date", "End Date", "Payment", "Attendance"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (String[] row : bookingDao.getMyBookings(student.getId())) model.addRow(row);
        };

        // Pay Now button for pending bookings
        JButton payBtn     = UITheme.primaryButton("💳 Pay Now");
        JButton refreshBtn = UITheme.secondaryButton("🔄 Refresh");

        payBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Select a booking to pay."); return; }
            String payStatus = (String) model.getValueAt(row, 3);
            if ("Paid".equalsIgnoreCase(payStatus)) { showInfo("This booking is already paid."); return; }

            // Find the trip by matching destination
            String dest = (String) model.getValueAt(row, 0);
            for (Trip t : tripDao.getAllTrips()) {
                if (t.getDestination().equals(dest)) {
                    showPaymentDialog(t.getTripId(), t.getTotalBudget());
                    refresh.run();
                    return;
                }
            }
            showError("Could not find trip details. Please try again.");
        });

        refreshBtn.addActionListener(e -> refresh.run());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.setOpaque(false);
        bottom.add(refreshBtn);
        bottom.add(payBtn);

        refresh.run();
        panel.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        panel.add(bottom,                    BorderLayout.SOUTH);
        return panel;
    }

    // ─── TAB 3: MY CONDUCT RECORD ────────────────────────────────────────────────
    private JPanel buildRecordTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel top = UITheme.cardPanel();
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JButton refreshBtn = UITheme.secondaryButton("🔄 Refresh Record");

        String[] cols = {"Date Filed", "Incident / Complaint"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);
        table.setRowHeight(40);
        // Make second column wider
        table.getColumnModel().getColumn(1).setPreferredWidth(500);

        Runnable refresh = () -> {
            model.setRowCount(0);
            ArrayList<String[]> complaints = teacherDao.getComplaintsForStudent(student.getId());
            if (complaints.isEmpty()) {
                model.addRow(new String[]{"—", "✅  Clear Record — No complaints filed."});
            } else {
                for (String[] c : complaints) model.addRow(c);
            }
        };

        refreshBtn.addActionListener(e -> refresh.run());
        top.add(UITheme.label("Conduct notices filed by teachers:"));
        top.add(refreshBtn);

        refresh.run();
        panel.add(top,                         BorderLayout.NORTH);
        panel.add(UITheme.scrollPane(table),   BorderLayout.CENTER);
        return panel;
    }

    // ─── UTILITIES ────────────────────────────────────────────────────────────────
    private void showError(String msg)   { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }
    private void showInfo(String msg)    { JOptionPane.showMessageDialog(this, msg, "Info",    JOptionPane.INFORMATION_MESSAGE); }
}
