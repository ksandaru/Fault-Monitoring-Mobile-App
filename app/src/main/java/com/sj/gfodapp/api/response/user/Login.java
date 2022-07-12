package com.sj.gfodapp.api.response.user;

public class Login {
    private String id;
    private String username;
    private String email;
    private String role;
    private String avatar;
    private String isActive;

    public Login() {
    }

    public Login(String id, String username, String email, String role, String avatar, String isActive) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.avatar = avatar;
        this.isActive = isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }
}
