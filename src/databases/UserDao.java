package databases;

import models.*;
import java.sql.*;
import java.util.ArrayList;

public class UserDao {

    // --- LOGIN LOGIC ---
    public User loginUser(String email, String password, int role) {
        String tableName = (role == 1) ? "Admins" : (role == 2) ? "Teachers" : "Students";
        String idCol = (role == 1) ? "admin_id" : (role == 2) ? "teacher_id" : "student_id";
        
        String sql = "SELECT * FROM " + tableName + " WHERE email = ? AND password = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password); // Note: Use hashed comparison in production
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                if (role == 1) return new Admin(rs.getString(idCol), rs.getString("name"), email, password);
                if (role == 2) return new Teacher(rs.getString(idCol), rs.getString("name"), email, password, rs.getString("contact_no"));
                if (role == 3) return new Student(rs.getString(idCol), rs.getString("name"), email, password, rs.getString("class"), rs.getString("emergency_contact"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- REGISTRATION LOGIC ---
    public boolean registerAdmin(Admin admin) {
        String sql = "INSERT INTO Admins (admin_id, name, email, password) VALUES (?, ?, ?, ?)";
        return executeUpdate(sql, admin.getId(), admin.getName(), admin.getEmail(), admin.getPassword());
    }

    public boolean registerTeacher(Teacher teacher) {
        String sql = "INSERT INTO Teachers (teacher_id, name, email, password, contact_no) VALUES (?, ?, ?, ?, ?)";
        return executeUpdate(sql, teacher.getId(), teacher.getName(), teacher.getEmail(), teacher.getPassword(), teacher.getContactNo());
    }

    public boolean registerStudent(Student student) {
        String sql = "INSERT INTO Students (student_id, name, email, password, class, emergency_contact) VALUES (?, ?, ?, ?, ?, ?)";
        return executeUpdate(sql, student.getId(), student.getName(), student.getEmail(), student.getPassword(), student.getStudentClass(), student.getEmergencyContact());
    }

    // --- STUDENT REPORT LOGIC (NEW) ---
    public void checkStudentReport(String studentId) {
        String bookingSql = "SELECT t.destination, b.payment_status, b.attendance_status " +
                            "FROM Bookings b JOIN Trips t ON b.trip_id = t.trip_id " +
                            "WHERE b.student_id = ?";
        
        String complaintSql = "SELECT issue, date_filed FROM Complaints WHERE student_id = ?";

        try (Connection conn = DBconnection.getConnection()) {
            // 1. Fetch Trip Stats
            PreparedStatement ps1 = conn.prepareStatement(bookingSql);
            ps1.setString(1, studentId);
            ResultSet rs1 = ps1.executeQuery();

            System.out.println("\n--- YOUR EXCURSION STATUS ---");
            boolean foundBooking = false;
            while (rs1.next()) {
                foundBooking = true;
                System.out.println("Destination: " + rs1.getString("destination") + 
                                   " | Payment: [" + rs1.getString("payment_status") + "]" +
                                   " | Attendance: [" + rs1.getString("attendance_status") + "]");
            }
            if (!foundBooking) System.out.println("No trips booked yet.");

            // 2. Fetch Complaints
            PreparedStatement ps2 = conn.prepareStatement(complaintSql);
            ps2.setString(1, studentId);
            ResultSet rs2 = ps2.executeQuery();

            System.out.println("\n--- CONDUCT NOTICES ---");
            boolean foundComplaints = false;
            while (rs2.next()) {
                foundComplaints = true;
                System.out.println("Date: " + rs2.getTimestamp("date_filed") + " | Incident: " + rs2.getString("issue"));
            }
            if (!foundComplaints) System.out.println("Clear Record - No complaints filed.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper to reduce code repetition
    private boolean executeUpdate(String sql, String... params) {
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setString(i + 1, params[i]);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
            return false;
        }
    }
}