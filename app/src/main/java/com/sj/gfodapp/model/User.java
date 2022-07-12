package com.sj.gfodapp.model;

public class User {
    private String userProfileId;
    private String fullName;
    private String status;
    private String nic;
    private String email;
    private String avatartUrl;

    public User() {
    }

    public User(String userProfileId, String fullName, String status, String nic, String email, String avatartUrl) {
        this.userProfileId = userProfileId;
        this.fullName = fullName;
        this.status = status;
        this.nic = nic;
        this.email = email;
        this.avatartUrl = avatartUrl;
    }

    public String getUserProfileId() {
        return userProfileId;
    }

    public void setUserProfileId(String userProfileId) {
        this.userProfileId = userProfileId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatartUrl() {
        return avatartUrl;
    }

    public void setAvatartUrl(String avatartUrl) {
        this.avatartUrl = avatartUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "userProfileId='" + userProfileId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", nic='" + nic + '\'' +
                ", email='" + email + '\'' +
                ", avatartUrl='" + avatartUrl + '\'' +
                '}';
    }
}
