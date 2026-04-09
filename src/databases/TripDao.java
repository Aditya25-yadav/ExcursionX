package databases;

import models.Trip;
import java.sql.*;
import java.util.ArrayList;

/**
 * TripDao — all Trip-related JDBC operations.
 * Uses fresh connections (DBconnection.getConnection()) in every method.
 * All queries use PreparedStatement; dates handled safely with Types.DATE.
 */
public class TripDao {

    // ─── 1. CREATE TRIP ──────────────────────────────────────────────────────────
    public boolean createTrip(Trip trip) {
        // Try with new schema (start_date, end_date, teacher_id)
        String sql = "INSERT INTO Trips (destination, start_date, end_date, capacity, total_budget, teacher_id)"
                   + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conn == null) { System.err.println("No DB connection."); return false; }

            ps.setString(1, trip.getDestination());
            setDateOrNull(ps, 2, trip.getStartDate());
            setDateOrNull(ps, 3, trip.getEndDate());
            ps.setInt(4, trip.getCapacity());
            ps.setDouble(5, trip.getTotalBudget());
            setStringOrNull(ps, 6, trip.getTeacherId());

            return ps.executeUpdate() > 0;

        } catch (SQLSyntaxErrorException e) {
            // Columns start_date/end_date/teacher_id don't exist — fall back to minimal insert
            System.err.println("[TripDao] New columns missing, using legacy insert: " + e.getMessage());
            return createTripLegacy(trip);

        } catch (SQLException e) {
            System.err.println("[TripDao] createTrip error: " + e.getMessage());
            return false;
        }
    }

    /** Fallback for databases that don't yet have the new columns */
    private boolean createTripLegacy(Trip trip) {
        String sql = "INSERT INTO Trips (destination, capacity, total_budget) VALUES (?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trip.getDestination());
            ps.setInt(2, trip.getCapacity());
            ps.setDouble(3, trip.getTotalBudget());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TripDao] createTripLegacy error: " + e.getMessage());
            return false;
        }
    }

    // ─── 2. GET ALL TRIPS ────────────────────────────────────────────────────────
    public ArrayList<Trip> getAllTrips() {
        ArrayList<Trip> list = new ArrayList<>();

        // Try with new columns first
        String sql = "SELECT trip_id, destination, start_date, end_date, capacity, total_budget, teacher_id FROM Trips";
        try (Connection conn = DBconnection.getConnection()) {
            if (conn == null) return list;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Trip(
                        rs.getInt("trip_id"),
                        rs.getString("destination"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getInt("capacity"),
                        rs.getDouble("total_budget"),
                        rs.getString("teacher_id")
                    ));
                }
                return list;
            }
        } catch (SQLSyntaxErrorException e) {
            // Fall back: old schema without date/teacher columns
            System.err.println("[TripDao] Using legacy schema for getAllTrips.");
            return getAllTripsLegacy();
        } catch (SQLException e) {
            System.err.println("[TripDao] getAllTrips error: " + e.getMessage());
            return list;
        }
    }

    private ArrayList<Trip> getAllTripsLegacy() {
        ArrayList<Trip> list = new ArrayList<>();
        String sql = "SELECT trip_id, destination, capacity, total_budget FROM Trips";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Trip(
                    rs.getInt("trip_id"),
                    rs.getString("destination"),
                    rs.getInt("capacity"),
                    rs.getDouble("total_budget")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[TripDao] getAllTripsLegacy error: " + e.getMessage());
        }
        return list;
    }

    // ─── 3. GET TRIPS FOR TEACHER ────────────────────────────────────────────────
    public ArrayList<Trip> getTripsForTeacher(String teacherId) {
        ArrayList<Trip> list = new ArrayList<>();
        String sql = "SELECT trip_id, destination, start_date, end_date, capacity, total_budget, teacher_id"
                   + " FROM Trips WHERE teacher_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return list;
            ps.setString(1, teacherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Trip(
                    rs.getInt("trip_id"),
                    rs.getString("destination"),
                    rs.getString("start_date"),
                    rs.getString("end_date"),
                    rs.getInt("capacity"),
                    rs.getDouble("total_budget"),
                    rs.getString("teacher_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[TripDao] getTripsForTeacher error: " + e.getMessage());
        }
        return list;
    }

    // ─── 4. UPDATE TRIP ──────────────────────────────────────────────────────────
    public boolean updateTrip(int tripId, String destination, String startDate,
                               String endDate, int capacity, double budget) {
        // Try with new schema columns first
        String sql = "UPDATE Trips SET destination=?, start_date=?, end_date=?, capacity=?, total_budget=?"
                   + " WHERE trip_id=?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, destination);
            setDateOrNull(ps, 2, startDate);
            setDateOrNull(ps, 3, endDate);
            ps.setInt(4, capacity);
            ps.setDouble(5, budget);
            ps.setInt(6, tripId);
            return ps.executeUpdate() > 0;

        } catch (SQLSyntaxErrorException e) {
            // Fall back to legacy update
            System.err.println("[TripDao] Using legacy update.");
            return updateTripLegacy(tripId, destination, capacity, budget);
        } catch (SQLException e) {
            System.err.println("[TripDao] updateTrip error: " + e.getMessage());
            return false;
        }
    }

    private boolean updateTripLegacy(int tripId, String destination, int capacity, double budget) {
        String sql = "UPDATE Trips SET destination=?, capacity=?, total_budget=? WHERE trip_id=?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, destination);
            ps.setInt(2, capacity);
            ps.setDouble(3, budget);
            ps.setInt(4, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TripDao] updateTripLegacy error: " + e.getMessage());
            return false;
        }
    }

    // ─── 5. DELETE TRIP ──────────────────────────────────────────────────────────
    public boolean deleteTrip(int tripId) {
        // First manually remove child records to handle any FK constraint
        deleteBookingsForTrip(tripId);

        String sql = "DELETE FROM Trips WHERE trip_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, tripId);
            int rows = ps.executeUpdate();
            if (rows == 0) System.err.println("[TripDao] deleteTrip: no trip with ID " + tripId);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[TripDao] deleteTrip error: " + e.getMessage());
            return false;
        }
    }

    /** Removes all Bookings for a trip before deletion (prevents FK constraint failures) */
    private void deleteBookingsForTrip(int tripId) {
        String sql = "DELETE FROM Bookings WHERE trip_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return;
            ps.setInt(1, tripId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TripDao] deleteBookingsForTrip: " + e.getMessage());
        }
    }

    // ─── 6. UPDATE PAYMENT STATUS (Admin override) ───────────────────────────────
    public void updatePaymentStatus(String studentId, int tripId, String status) {
        String sql = "UPDATE Bookings SET payment_status = ? WHERE student_id = ? AND trip_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return;
            ps.setString(1, status);
            ps.setString(2, studentId);
            ps.setInt(3, tripId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TripDao] updatePaymentStatus error: " + e.getMessage());
        }
    }

    // ─── 7. GET MANIFEST LIST ────────────────────────────────────────────────────
    public ArrayList<String[]> getManifestList(int tripId) {
        ArrayList<String[]> manifest = new ArrayList<>();
        String sql = "SELECT s.name, s.email, s.class, b.payment_status, b.attendance_status"
                   + " FROM Bookings b JOIN Students s ON b.student_id = s.student_id"
                   + " WHERE b.trip_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return manifest;
            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                manifest.add(new String[]{
                    rs.getString("name"), rs.getString("email"), rs.getString("class"),
                    rs.getString("payment_status"), rs.getString("attendance_status")
                });
            }
        } catch (SQLException e) {
            System.err.println("[TripDao] getManifestList error: " + e.getMessage());
        }
        return manifest;
    }

    // ─── 8. GET MY BOOKINGS (Student) ────────────────────────────────────────────
    public ArrayList<String[]> getMyBookings(String studentId) {
        ArrayList<String[]> bookings = new ArrayList<>();
        String sql = "SELECT t.destination, b.payment_status, b.attendance_status"
                   + " FROM Bookings b JOIN Trips t ON b.trip_id = t.trip_id"
                   + " WHERE b.student_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return bookings;
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookings.add(new String[]{
                    rs.getString("destination"),
                    rs.getString("payment_status"),
                    rs.getString("attendance_status")
                });
            }
        } catch (SQLException e) {
            System.err.println("[TripDao] getMyBookings error: " + e.getMessage());
        }
        return bookings;
    }

    // ─── 9. ADD EXPENSE ──────────────────────────────────────────────────────────
    public boolean addExpense(int tripId, String category, double amount, String description) {
        String sql = "INSERT INTO Expenses (trip_id, category, amount, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, tripId);
            ps.setString(2, category);
            ps.setDouble(3, amount);
            ps.setString(4, description);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TripDao] addExpense error: " + e.getMessage());
            return false;
        }
    }

    // ─── 10. GET EXPENSES ─────────────────────────────────────────────────────────
    public ArrayList<String[]> getExpenses(int tripId) {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT category, amount, description, added_at FROM Expenses WHERE trip_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return list;
            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("category"), "₹" + rs.getDouble("amount"),
                    rs.getString("description"), rs.getString("added_at")
                });
            }
        } catch (SQLException e) {
            System.err.println("[TripDao] getExpenses error: " + e.getMessage());
        }
        return list;
    }

    // ─── HELPER METHODS ───────────────────────────────────────────────────────────

    /** Sets a DATE parameter, or NULL if the value is empty/null */
    private void setDateOrNull(PreparedStatement ps, int idx, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(idx, Types.DATE);
        } else {
            ps.setString(idx, value.trim());
        }
    }

    /** Sets a VARCHAR parameter, or NULL if the value is empty/null */
    private void setStringOrNull(PreparedStatement ps, int idx, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(idx, Types.VARCHAR);
        } else {
            ps.setString(idx, value.trim());
        }
    }

    /** Console manifest print — preserved for compatibility */
    public void displayManifest(int tripId) {
        System.out.println("\n--- TRIP MANIFEST (Trip ID: " + tripId + ") ---");
        for (String[] row : getManifestList(tripId)) {
            System.out.printf("Name: %s | Email: %s | Class: %s%n", row[0], row[1], row[2]);
        }
    }
}