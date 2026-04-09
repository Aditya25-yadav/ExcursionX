package models;



public class Teacher extends User {

    private String contactNo;



    public Teacher(String id, String name, String email, String password, String contactNo) {

        super(id, name, email, password);

        this.contactNo = contactNo;

    }



    // Encapsulation: Add Getter so other classes can access this private data

    public String getContactNo() {

        return contactNo;

    }



    @Override

    public void displayDashboard() {

        System.out.println("\n--- TEACHER PORTAL ---");

        // Using the variable here clears the "unused field" warning

        System.out.println("Logged in as: " + getName() + " | Contact: " + contactNo);

        System.out.println("1. Create Activity Schedule\n2. Track Student Attendance\n3. Emergency Panel\n4. Logout");

    }

}