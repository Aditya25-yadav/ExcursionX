package databases;

import models.Booking;
import models.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * BookingDao — handles all booking & payment JDBC operations.
 * All queries use PreparedStatement to prevent SQL injection.
 * Thread-safe booking uses Java's synchronized keyword + DB transactions.
 */
public class BookingDao {

    // ─── 1. THREAD-SAFE SEAT BOOKING ─────────────────────────────────────────────
    /**
     * Books a seat for a student on a trip.
     * Uses synchronized + DB transaction to prevent race conditions (overbooking).
     *
     * @return true if booked successfully, false if duplicate or full
     */
    public synchronized boolean bookTrip(String studentId, int tripId) {
        String checkDuplSql  = "SELECT COUNT(*) FROM Bookings WHERE student_id = ? AND trip_id = ?";
        String checkCapSql   = "SELECT capacity  FROM Trips   WHERE trip_id     = ?";
        String updateCapSql  = "UPDATE Trips     SET capacity = capacity - 1 WHERE trip_id = ? AND capacity > 0";
        String insertBkgSql  = "INSERT INTO Bookings (student_id, trip_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DBconnection.getConnection();
            conn.setAutoCommit(false);

            // STEP A — check for duplicate booking
            try (PreparedStatement ps = conn.prepareStatement(checkDuplSql)) {
                ps.setString(1, studentId);
                ps.setInt(2, tripId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Duplicate booking rejected for student: " + studentId);
                    conn.rollback();
                    return false;
                }
            }

            // STEP B — verify capacity
            int currentCap = 0;
            try (PreparedStatement ps = conn.prepareStatement(checkCapSql)) {
                ps.setInt(1, tripId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) currentCap = rs.getInt("capacity");
            }
            if (currentCap <= 0) {
                System.out.println("Trip full — booking rejected.");
                conn.rollback();
                return false;
            }

            // STEP C — decrement capacity
            try (PreparedStatement ps = conn.prepareStatement(updateCapSql)) {
                ps.setInt(1, tripId);
                int rows = ps.executeUpdate();
                if (rows == 0) { conn.rollback(); return false; }
            }

            // STEP D — insert booking record
            try (PreparedStatement ps = conn.prepareStatement(insertBkgSql)) {
                ps.setString(1, studentId);
                ps.setInt(2, tripId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ─── 2. GET BOOKING ID ────────────────────────────────────────────────────────
    /** Fetches the booking_id for a given student + trip combination */
    public int getBookingId(String studentId, int tripId) {
        String sql = "SELECT booking_id FROM Bookings WHERE student_id = ? AND trip_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, tripId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("booking_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    // ─── 3. SIMULATE PAYMENT ─────────────────────────────────────────────────────
    /**
     * Simulates payment — inserts into Payments and updates Bookings.payment_status.
     *
     * @param studentId    the paying student
     * @param tripId       the trip being paid for
     * @param amount       amount in INR
     * @param method       UPI | Card | Cash | Net Banking
     * @return true if payment recorded successfully
     */
    public synchronized boolean makePayment(String studentId, int tripId,
                                             double amount, String method) {
        int bookingId = getBookingId(studentId, tripId);
        if (bookingId == -1) return false; // no booking found

        String insertSql = "INSERT INTO Payments (booking_id, student_id, trip_id, amount, payment_method, payment_status)"
                         + " VALUES (?, ?, ?, ?, ?, 'Paid')";
        String updateSql = "UPDATE Bookings SET payment_status = 'Paid' WHERE booking_id = ?";

        Connection conn = null;
        try {
            conn = DBconnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, bookingId);
                ps.setString(2, studentId);
                ps.setInt(3, tripId);
                ps.setDouble(4, amount);
                ps.setString(5, method);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ─── 4. GET MY BOOKINGS (rich) ────────────────────────────────────────────────
    /**
     * Returns full booking history for a student as a list of String arrays.
     * Columns: [Destination, StartDate, EndDate, PaymentStatus, AttendanceStatus]
     */
    public ArrayList<String[]> getMyBookings(String studentId) {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT t.destination, t.start_date, t.end_date, "
                   + "b.payment_status, b.attendance_status "
                   + "FROM Bookings b JOIN Trips t ON b.trip_id = t.trip_id "
                   + "WHERE b.student_id = ? ORDER BY b.booking_date DESC";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("destination"),
                    rs.getString("start_date")  != null ? rs.getString("start_date")  : "TBD",
                    rs.getString("end_date")    != null ? rs.getString("end_date")    : "TBD",
                    rs.getString("payment_status"),
                    rs.getString("attendance_status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ─── 5. CHECK IF ALREADY PAID ─────────────────────────────────────────────────
    public boolean isAlreadyPaid(String studentId, int tripId) {
        String sql = "SELECT payment_status FROM Bookings WHERE student_id = ? AND trip_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, tripId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return "Paid".equalsIgnoreCase(rs.getString("payment_status"));
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ─── 6. GET ALL PAYMENTS (Admin Revenue) ──────────────────────────────────────
    /**
     * Returns all payments for admin analytics.
     * Columns: [PaymentID, Student, Destination, Amount, Method, Status, Date]
     */
    public ArrayList<String[]> getAllPayments() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT p.payment_id, s.name, t.destination, p.amount, "
                   + "p.payment_method, p.payment_status, p.paid_at "
                   + "FROM Payments p "
                   + "JOIN Students s ON p.student_id = s.student_id "
                   + "JOIN Trips t    ON p.trip_id    = t.trip_id "
                   + "ORDER BY p.paid_at DESC";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("payment_id")),
                    rs.getString("name"),
                    rs.getString("destination"),
                    "₹" + rs.getDouble("amount"),
                    rs.getString("payment_method"),
                    rs.getString("payment_status"),
                    rs.getString("paid_at")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ─── 7. GET ANALYTICS STATS ───────────────────────────────────────────────────
    /**
     * Returns a HashMap of key analytics metrics for the Admin dashboard.
     * Keys: totalTrips, totalStudents, totalRevenue, pendingPayments
     */
    public HashMap<String, String> getAnalytics() {
        HashMap<String, String> stats = new HashMap<>();
        // FIX: Use try-with-resources to ensure the connection is always closed
        try (Connection conn = DBconnection.getConnection()) {
            if (conn == null) { stats.put("error", "DB Offline"); return stats; }

            // Total Trips
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Trips")) {
                ResultSet rs = ps.executeQuery();
                stats.put("totalTrips", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
            // Total Students
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Students")) {
                ResultSet rs = ps.executeQuery();
                stats.put("totalStudents", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
            // Total Bookings
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Bookings")) {
                ResultSet rs = ps.executeQuery();
                stats.put("totalBookings", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
            // Total Revenue (paid payments)
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(amount),0) FROM Payments WHERE payment_status='Paid'")) {
                ResultSet rs = ps.executeQuery();
                stats.put("totalRevenue", rs.next() ? String.format("₹%.0f", rs.getDouble(1)) : "₹0");
            }
            // Pending Payments count
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Bookings WHERE payment_status='Pending'")) {
                ResultSet rs = ps.executeQuery();
                stats.put("pendingPayments", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
            // Total Teachers
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Teachers")) {
                ResultSet rs = ps.executeQuery();
                stats.put("totalTeachers", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }
}
