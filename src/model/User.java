package model;

import java.sql.Timestamp;

/**
 * Represents a staff user account.
 * Maps to the `users` table in the database.
 */
public class User {

    private int       userId;
    private String    username;
    private String    password;   // stored as SHA-256 hash
    private String    role;       // "admin" or "cashier"
    private Timestamp createdAt;

    // Constructors
    public User() {}

    public User(int userId, String username,
                String password, String role, Timestamp createdAt) {
        this.userId    = userId;
        this.username  = username;
        this.password  = password;
        this.role      = role;
        this.createdAt = createdAt;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Convenience
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{userId=" + userId
             + ", username='" + username + "'"
             + ", role='" + role + "'}";
    }
}