package databases;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

/**
 * TeacherDao — handles all Teacher-specific JDBC operations.
 * Includes attendance marking, complaint filing, emergency logging, and file scheduling.
 * All queries use PreparedStatement for injection safety.
 */
public class TeacherDao {

    // ─── 1. WRITE SCHEDULE FILE ──────────────────────────────────────────────────
    /**
     * Saves a trip schedule as a .txt file using Java File I/O.
     * Demonstrates Java's BufferedWriter for efficient file writing.
     */
    public void writeScheduleFile(String tripName, String scheduleData) {
        String fileName = tripName.replaceAll("\\s+", "_") + "_Schedule.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("==============================\n");
            writer.write("   TRIP SCHEDULE: " + tripName.toUpperCase() + "\n");
            writer.write("==============================\n\n");
            writer.write(scheduleData);
            System.out.println("Schedule saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("File Error: " + e.getMessage());
        }
    }

    // ─── 2. MARK ATTENDANCE ──────────────────────────────────────────────────────
    /**
     * Updates attendance status for a student on a specific trip.
     *
     * @param tripId    trip identifier
     * @param studentId student to mark
     * @param status    Present | Absent | Excused
     */
    public void markAttendance(int tripId, String studentId, String status) {
        String sql = "UPDATE Bookings SET attendance_status = ? WHERE trip_id = ? AND student_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, tripId);
            ps.setString(3, studentId);
            int rows = ps.executeUpdate();
            if (rows == 0) System.out.println("No matching booking found.");
        } catch (SQLException e) {
            System.err.println("Attendance error: " + e.getMessage());
        }
    }

    // ─── 3. FILE COMPLAINT ───────────────────────────────────────────────────────
    /**
     * Files a conduct complaint against a student by a teacher.
     */
    public void fileComplaint(String teacherId, String studentId, String issue) {
        String sql = "INSERT INTO Complaints (student_id, teacher_id, issue) VALUES (?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, teacherId);
            ps.setString(3, issue);
            ps.executeUpdate();
            System.out.println("Complaint filed against student: " + studentId);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1452) {
                System.out.println("Error: Student ID '" + studentId + "' not found.");
            } else {
                System.err.println("Complaint error: " + e.getMessage());
            }
        }
    }

    // ─── 4. GET COMPLAINTS FOR STUDENT ───────────────────────────────────────────
    /**
     * Returns all conduct complaints filed against a student.
     * Columns: [Date, Issue]
     */
    public ArrayList<String[]> getComplaintsForStudent(String studentId) {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT issue, date_filed FROM Complaints WHERE student_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("date_filed");
                String dateStr = (ts != null) ? ts.toString() : "Unknown Date";
                list.add(new String[]{ dateStr, rs.getString("issue") });
            }
        } catch (SQLException e) {
            System.err.println("Complaint fetch error: " + e.getMessage());
        }
        return list;
    }

    // ─── 5. LOG EMERGENCY ────────────────────────────────────────────────────────
    /**
     * Logs an emergency incident for a trip to the EmergencyLogs table.
     *
     * @param tripId      the trip where the incident occurred
     * @param reportedBy  teacher_id of the reporting teacher
     * @param description full description of the emergency
     * @param contact     emergency contact number
     * @return true if log was saved successfully
     */
    public boolean logEmergency(int tripId, String reportedBy, String description, String contact) {
        String sql = "INSERT INTO EmergencyLogs (trip_id, reported_by, description, contact_number)"
                   + " VALUES (?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            ps.setString(2, reportedBy);
            ps.setString(3, description);
            ps.setString(4, contact);
            ps.executeUpdate();
            System.out.println("Emergency logged for trip ID: " + tripId);
            return true;
        } catch (SQLException e) {
            System.err.println("Emergency log error: " + e.getMessage());
            return false;
        }
    }

    // ─── 6. GET EMERGENCY LOGS FOR TRIP ─────────────────────────────────────────
    /**
     * Returns all emergency logs for a given trip.
     * Columns: [EmergencyID, ReportedBy, Description, Contact, LoggedAt]
     */
    public ArrayList<String[]> getEmergencyLogs(int tripId) {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT emergency_id, reported_by, description, contact_number, logged_at"
                   + " FROM EmergencyLogs WHERE trip_id = ? ORDER BY logged_at DESC";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("emergency_id")),
                    rs.getString("reported_by"),
                    rs.getString("description"),
                    rs.getString("contact_number"),
                    rs.getString("logged_at")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching emergency logs: " + e.getMessage());
        }
        return list;
    }

    // ─── 7. GET ALL EMERGENCY LOGS (Admin view) ───────────────────────────────────
    /**
     * Returns all emergency logs across all trips for admin oversight.
     * Columns: [EmergencyID, TripDest, ReportedBy, Description, Contact, LoggedAt]
     */
    public ArrayList<String[]> getAllEmergencyLogs() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT e.emergency_id, t.destination, e.reported_by, e.description,"
                   + " e.contact_number, e.logged_at"
                   + " FROM EmergencyLogs e JOIN Trips t ON e.trip_id = t.trip_id"
                   + " ORDER BY e.logged_at DESC";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("emergency_id")),
                    rs.getString("destination"),
                    rs.getString("reported_by"),
                    rs.getString("description"),
                    rs.getString("contact_number"),
                    rs.getString("logged_at")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all emergency logs: " + e.getMessage());
        }
        return list;
    }

    // ─── 8. GET ALL USERS (Admin User Management) ────────────────────────────────
    /**
     * Returns all teachers and students combined for admin user management.
     * Columns: [ID, Name, Email, Role, Contact/Class]
     */
    public ArrayList<String[]> getAllUsers() {
        ArrayList<String[]> list = new ArrayList<>();

        // Teachers
        String tSql = "SELECT teacher_id, name, email, contact_no FROM Teachers";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(tSql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("teacher_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    "Teacher",
                    rs.getString("contact_no")
                });
            }
        } catch (SQLException e) { System.err.println("Error fetching teachers: " + e.getMessage()); }

        // Students
        // FIX: `class` is a MySQL keyword — must be backtick-quoted in explicit SELECT
        String sSql = "SELECT student_id, name, email, `class` FROM Students";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sSql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    "Student",
                    rs.getString("class")
                });
            }
        } catch (SQLException e) { System.err.println("Error fetching students: " + e.getMessage()); }

        return list;
    }
}