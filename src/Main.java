import java.util.Scanner;

class Main{
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to the student management system!");
        System.out.println("Please enter your name:");
        String name = sc.nextLine();
        System.out.println("Please enter your email:");
        String email = sc.nextLine();
        System.out.println("PLease enter your password:");
        String password = sc.nextLine();
        User User = new User(name,email,password);
        System.out.println("Welcome " + User.getName() + "!");
    }
}