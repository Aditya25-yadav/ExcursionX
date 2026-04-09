package models;

import java.util.ArrayList;

/**
 * Represents a trip/excursion with all scheduling and budget details.
 * Extended from original to include start_date, end_date, and teacherId.
 */
public class Trip {

    private int    tripId;
    private String destination;
    private String startDate;   // yyyy-MM-dd format
    private String endDate;     // yyyy-MM-dd format
    private int    capacity;
    private double totalBudget;
    private String teacherId;   // FK to Teachers
    private ArrayList<String> activities;

    // ─── Full Constructor (from DB) ──────────────────────────────────────────────
    public Trip(int tripId, String destination, String startDate, String endDate,
                int capacity, double totalBudget, String teacherId) {
        this.tripId      = tripId;
        this.destination = destination;
        this.startDate   = startDate  != null ? startDate  : "";
        this.endDate     = endDate    != null ? endDate    : "";
        this.capacity    = capacity;
        this.totalBudget = totalBudget;
        this.teacherId   = teacherId  != null ? teacherId  : "";
        this.activities  = new ArrayList<>();
    }

    /** Legacy constructor — kept for backward compatibility */
    public Trip(int tripId, String destination, int capacity, double totalBudget) {
        this(tripId, destination, "", "", capacity, totalBudget, "");
    }

    // ─── Business Logic ──────────────────────────────────────────────────────────

    /** Adds an activity to the in-memory itinerary list */
    public void addActivity(String activityName) {
        activities.add(activityName);
    }

    /** Returns a concise display string used in tables/reports */
    public String toDisplayString() {
        return String.format("ID:%d | %s | %s → %s | Cap:%d | ₹%.0f",
                tripId, destination, startDate, endDate, capacity, totalBudget);
    }

    // ─── Getters ─────────────────────────────────────────────────────────────────
    public int               getTripId()      { return tripId; }
    public String            getDestination() { return destination; }
    public String            getStartDate()   { return startDate; }
    public String            getEndDate()     { return endDate; }
    public int               getCapacity()    { return capacity; }
    public double            getTotalBudget() { return totalBudget; }
    public String            getTeacherId()   { return teacherId; }
    public ArrayList<String> getActivities()  { return activities; }

    // ─── Setters ─────────────────────────────────────────────────────────────────
    public void setCapacity(int capacity)    { this.capacity = capacity; }
    public void setEndDate(String endDate)   { this.endDate  = endDate; }

    /** Print trip details — useful for debugging */
    public void displayTripDetails() {
        System.out.println("Trip ID: " + tripId + " | Destination: " + destination);
        System.out.println("Dates : " + startDate + " → " + endDate);
        System.out.println("Capacity: " + capacity + " | Budget: ₹" + totalBudget);
        System.out.println("Activities: " + (activities.isEmpty() ? "None planned yet." : activities));
    }
}