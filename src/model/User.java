package model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String username;
    private String password;
    private String role;
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String username, String password, String role,
                Timestamp createdAt) {
        this.userId = userId; this.username = username; this.password = password;
        this.role = role; this.createdAt = createdAt;
    }

    public int getUserId()         { return userId; }
    public String getUsername()    { return username; }
    public String getPassword()    { return password; }
    public String getRole()        { return role; }
    public Timestamp getCreatedAt(){ return createdAt; }

    public void setUserId(int v)          { this.userId = v; }
    public void setUsername(String v)     { this.username = v; }
    public void setPassword(String v)     { this.password = v; }
    public void setRole(String v)         { this.role = v; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }

    public boolean isAdmin() { return "admin".equalsIgnoreCase(role); }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", username='" + username + "', role='" + role + "'}";
    }
}