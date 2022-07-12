package com.sj.gfodapp.api.response.login;

public class User {

    private long id;
    private String username;
    private String email;
    private String role;
    private String avatar;

    public User() {
    }

    public User(long id, String username, String email, String role, String avatar) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.avatar = avatar;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
}
