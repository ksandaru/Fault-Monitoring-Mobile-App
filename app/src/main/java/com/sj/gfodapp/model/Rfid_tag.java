package com.sj.gfodapp.model;

public class Rfid_tag {
    private  String profileId;
    private String fullName;
    private String city;
    private  String status;


    public Rfid_tag() {
    }

    public Rfid_tag(String profileId, String fullName,  String city, String status) {
        this.profileId = profileId;
        this.fullName = fullName;
        this.city = city;
        this.status = status;



    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    //-------
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;}
    //--------

}
