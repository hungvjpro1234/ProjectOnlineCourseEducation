// app/src/main/java/com/example/projectonlinecourseeducation/core/model/User.java
package com.example.projectonlinecourseeducation.core.model;

public class User {

    public enum Role {
        STUDENT, TEACHER, ADMIN
    }

    private String id;
    private String name;
    private String username;   // NEW
    private String email;
    private String password;   // demo: plaintext. THỰC TẾ hãy hash + salt
    private boolean verified;  // xác minh email
    private String resetToken; // NEW: token đặt lại qua link

    public User(String id, String name, String username, String email,
                String password, boolean verified, String resetToken, Role role) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.verified = verified;
        this.resetToken = resetToken;
        this.role = role;
    }

    private Role role; // NEW

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public boolean isVerified() { return verified; }
    public String getResetToken() { return resetToken; }
    public Role getRole() { return role; }

    public void setPassword(String password) { this.password = password; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public void setRole(Role role) { this.role = role; }
}
