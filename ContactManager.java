import java.util.*;
import java.util.regex.*;

class Contact {
    private String name;
    private String phone;
    private String email;

    public Contact(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Name: " + name + " | Phone: " + phone + " | Email: " + email;
    }
}

public class ContactManager {
    private static ArrayList<Contact> contacts = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);

    private static boolean isValidPhone(String phone) {
        return Pattern.matches("\\d{10}", phone);
    }

    private static boolean isValidEmail(String email) {
        return Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email);
    }

    private static boolean isDuplicate(String phone, String email) {
        for (Contact c : contacts) {
            if (c.getPhone().equals(phone) || c.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    private static Contact findContact(String key) {
        for (Contact c : contacts) {
            if (c.getName().equalsIgnoreCase(key) || c.getPhone().equals(key) || c.getEmail().equalsIgnoreCase(key)) {
                return c;
            }
        }
        return null;
    }

    private static void addContact() {
        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Phone (10 digits): ");
        String phone = sc.nextLine();
        if (!isValidPhone(phone)) {
            System.out.println("Invalid phone format!");
            return;
        }

        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        if (!isValidEmail(email)) {
            System.out.println("Invalid email format!");
            return;
        }

        if (isDuplicate(phone, email)) {
            System.out.println("Contact with this phone/email already exists!");
            return;
        }

        contacts.add(new Contact(name, phone, email));
        System.out.println("Contact Added Successfully!");
    }

    private static void viewContacts() {
        if (contacts.isEmpty()) {
            System.out.println("No Contacts Found!");
            return;
        }
        System.out.println("\n--- Contact List ---");
        contacts.forEach(System.out::println);
    }

    private static void searchContact() {
        System.out.print("Enter name/phone/email to search: ");
        String key = sc.nextLine();
        Contact c = findContact(key);
        System.out.println(c == null ? "Contact not found!" : c);
    }

    private static void updateContact() {
        System.out.print("Enter name/phone/email to update: ");
        String key = sc.nextLine();
        Contact c = findContact(key);

        if (c == null) {
            System.out.println("Contact Not Found!");
            return;
        }

        System.out.print("Enter new name (leave blank to keep same): ");
        String name = sc.nextLine();
        if (!name.isBlank()) c.setName(name);

        System.out.print("Enter new phone (10 digits, blank to keep same): ");
        String phone = sc.nextLine();
        if (!phone.isBlank()) {
            if (!isValidPhone(phone)) {
                System.out.println("Invalid phone!");
                return;
            }
            c.setPhone(phone);
        }

        System.out.print("Enter new email (blank to keep same): ");
        String email = sc.nextLine();
        if (!email.isBlank()) {
            if (!isValidEmail(email)) {
                System.out.println("Invalid email!");
                return;
            }
            c.setEmail(email);
        }

        System.out.println("Contact Updated Successfully!");
    }

    private static void deleteContact() {
        System.out.print("Enter name/phone/email to delete: ");
        String key = sc.nextLine();
        Contact c = findContact(key);

        if (c == null) {
            System.out.println("Contact Not Found!");
            return;
        }

        contacts.remove(c);
        System.out.println("Contact Deleted Successfully!");
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n1. Add  2. View  3. Search  4. Update  5. Delete  6. Exit");
            System.out.print("Choose Option: ");
            int choice;

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Enter valid number!");
                continue;
            }

            switch (choice) {
                case 1 -> addContact();
                case 2 -> viewContacts();
                case 3 -> searchContact();
                case 4 -> updateContact();
                case 5 -> deleteContact();
                case 6 -> { System.out.println("Exiting..."); return; }
                default -> System.out.println("Invalid option!");
            }
        }
    }
}
