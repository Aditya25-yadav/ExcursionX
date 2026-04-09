package models;

/**
 * Represents a booking record linking a Student to a Trip.
 * Demonstrates OOP Encapsulation via private fields + public getters.
 */
public class Booking {

    private int    bookingId;
    private String studentId;
    private int    tripId;
    private String bookingDate;       // stored as String for display convenience
    private String paymentStatus;     // Pending | Paid | Refunded
    private String attendanceStatus;  // Present | Absent | Excused | Not Marked

    // ─── Constructors ────────────────────────────────────────────────────────────

    /** Full constructor used when reading from DB */
    public Booking(int bookingId, String studentId, int tripId,
                   String bookingDate, String paymentStatus, String attendanceStatus) {
        this.bookingId        = bookingId;
        this.studentId        = studentId;
        this.tripId           = tripId;
        this.bookingDate      = bookingDate;
        this.paymentStatus    = paymentStatus;
        this.attendanceStatus = attendanceStatus;
    }

    /** Light constructor used when creating a new booking (ID assigned by DB) */
    public Booking(String studentId, int tripId) {
        this.studentId        = studentId;
        this.tripId           = tripId;
        this.paymentStatus    = "Pending";
        this.attendanceStatus = "Not Marked";
    }

    // ─── Getters ─────────────────────────────────────────────────────────────────

    public int    getBookingId()        { return bookingId; }
    public String getStudentId()        { return studentId; }
    public int    getTripId()           { return tripId; }
    public String getBookingDate()      { return bookingDate; }
    public String getPaymentStatus()    { return paymentStatus; }
    public String getAttendanceStatus() { return attendanceStatus; }

    // ─── Setters ─────────────────────────────────────────────────────────────────

    public void setPaymentStatus(String paymentStatus)       { this.paymentStatus    = paymentStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    @Override
    public String toString() {
        return "Booking[id=" + bookingId + ", student=" + studentId
                + ", trip=" + tripId + ", payment=" + paymentStatus
                + ", attendance=" + attendanceStatus + "]";
    }
}
