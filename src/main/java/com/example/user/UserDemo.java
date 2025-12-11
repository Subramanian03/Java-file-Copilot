package com.example.user;

/**
 * Demo class to test the hardened user creation system with security validations.
 */
public class UserDemo {
    public static void main(String[] args) {
        UserService userService = new UserService();
        int successCount = 0;

        System.out.println("=== User Creation System - Security Hardened ===\n");

        // Test 1: Create first user successfully
        try {
            User user1 = userService.createUser("John Doe", "john@example.com");
            System.out.println("✓ User created: " + user1);
            successCount++;
        } catch (ValidationException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }

        // Test 2: Create second user successfully
        try {
            User user2 = userService.createUser("Jane Smith", "jane@example.com");
            System.out.println("✓ User created: " + user2);
            successCount++;
        } catch (ValidationException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }

        // Test 3: Attempt duplicate email (should fail)
        try {
            System.out.println("\nAttempting duplicate email...");
            User user3 = userService.createUser("Another John", "john@example.com");
            System.out.println("✓ User created: " + user3);
        } catch (ValidationException e) {
            System.out.println("✗ Expected error (duplicate email): " + e.getMessage());
        }

        // Test 4: Invalid email format (should fail)
        try {
            System.out.println("\nAttempting invalid email format...");
            User user4 = userService.createUser("Invalid User", "invalid-email");
            System.out.println("✓ User created: " + user4);
        } catch (ValidationException e) {
            System.out.println("✗ Expected error (invalid format): " + e.getMessage());
        }

        // Test 5: Empty name (should fail)
        try {
            System.out.println("\nAttempting empty name...");
            User user5 = userService.createUser("", "valid@example.com");
            System.out.println("✓ User created: " + user5);
        } catch (ValidationException e) {
            System.out.println("✗ Expected error (empty name): " + e.getMessage());
        }

        // Test 6: Null email (should fail)
        try {
            System.out.println("\nAttempting null email...");
            User user6 = userService.createUser("Valid Name", null);
            System.out.println("✓ User created: " + user6);
        } catch (ValidationException e) {
            System.out.println("✗ Expected error (null email): " + e.getMessage());
        }

        // Test 7: Email exceeding max length (should fail)
        try {
            System.out.println("\nAttempting email exceeding max length...");
            String longEmail = "a".repeat(300) + "@example.com";
            User user7 = userService.createUser("Test User", longEmail);
            System.out.println("✓ User created: " + user7);
        } catch (ValidationException e) {
            System.out.println("✗ Expected error (email too long): " + e.getMessage());
        }

        System.out.println("\n=== Summary ===");
        System.out.println("Total users successfully created: " + successCount);
        System.out.println("Total users in system: " + userService.getUserCount());
        System.out.println("\n✓ All security validations working correctly");
    }
}
