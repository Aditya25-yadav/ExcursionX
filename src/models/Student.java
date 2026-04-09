package models;

public class Student extends User {
    private String studentClass;
    private String emergencyContact;

    public Student(String id, String name, String email, String password, String studentClass, String emergencyContact) {
        super(id, name, email, password);
        this.studentClass = studentClass;
        this.emergencyContact = emergencyContact;
    }

    // ADD THIS GETTER METHOD BELOW
    public String getStudentClass() {
        return studentClass;
    }

    // You should also add one for emergency contact to clear that warning too
    public String getEmergencyContact() {
        return emergencyContact;
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n--- STUDENT ACCESS PORTAL ---");
        // Using the variable here also clears the warning!
        System.out.println("Welcome student from class: " + studentClass); 
        System.out.println("1. Browse Trips\n2. Book a Seat\n3. My Payments\n4. Logout");
    }
}