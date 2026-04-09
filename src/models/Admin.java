package models;

public class Admin extends User {
    public Admin(String id, String name, String email, String password) {
        super(id, name, email, password);
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n--- ADMIN DASHBOARD ---");
        System.out.println("1. Manage Trips\n2. View Financial Reports\n3. Manage Users\n4. Logout");
    }
}