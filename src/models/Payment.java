package models;

/**
 * Represents a payment transaction linked to a Booking.
 * Demonstrates OOP Encapsulation and clean domain modelling.
 */
public class Payment {

    private int    paymentId;
    private int    bookingId;
    private String studentId;
    private int    tripId;
    private double amount;
    private String paymentMethod;   // UPI | Card | Cash | Net Banking
    private String paymentStatus;   // Pending | Paid | Failed
    private String paidAt;          // timestamp string

    // ─── Constructor (full, from DB) ────────────────────────────────────────────
    public Payment(int paymentId, int bookingId, String studentId, int tripId,
                   double amount, String paymentMethod, String paymentStatus, String paidAt) {
        this.paymentId     = paymentId;
        this.bookingId     = bookingId;
        this.studentId     = studentId;
        this.tripId        = tripId;
        this.amount        = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paidAt        = paidAt;
    }

    /** Constructor for new payment (ID assigned by DB) */
    public Payment(int bookingId, String studentId, int tripId,
                   double amount, String paymentMethod) {
        this.bookingId     = bookingId;
        this.studentId     = studentId;
        this.tripId        = tripId;
        this.amount        = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = "Pending";
    }

    // ─── Getters ─────────────────────────────────────────────────────────────────

    public int    getPaymentId()     { return paymentId; }
    public int    getBookingId()     { return bookingId; }
    public String getStudentId()     { return studentId; }
    public int    getTripId()        { return tripId; }
    public double getAmount()        { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPaidAt()        { return paidAt; }

    // ─── Setters ─────────────────────────────────────────────────────────────────

    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    @Override
    public String toString() {
        return "Payment[id=" + paymentId + ", booking=" + bookingId
                + ", amount=₹" + amount + ", method=" + paymentMethod
                + ", status=" + paymentStatus + "]";
    }
}
