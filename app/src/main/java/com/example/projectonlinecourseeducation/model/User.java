// app/src/main/java/com/example/projectonlinecourseeducation/User.java
package com.example.projectonlinecourseeducation.model;

public class User {
    private String id;
    private String name;
    private String email;
    private String password; // demo: lưu plain text. THỰC TẾ hãy hash/salt!
    private boolean verified;
    private String resetCode;

    public User(String id, String name, String email, String password, boolean verified, String resetCode) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.verified = verified;
        this.resetCode = resetCode;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public boolean isVerified() { return verified; }
    public String getResetCode() { return resetCode; }

    public void setPassword(String password) { this.password = password; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setResetCode(String resetCode) { this.resetCode = resetCode; }
}
