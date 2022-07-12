package com.sj.gfodapp.model;

public class Fault {
    private String id;
    private  String fault;
    private  String description;
    private  String monthOccured;
    private  String yearOccured;
    private  String imageUrl;
    private  String imageFileName;
    private String longitude;
    private String latitude;
    private Operator_Division division;


    public Fault() {
    }

    public Fault(String fault, String description, String monthOccured, String yearOccured, String longitude, String latitude) {
        this.fault= fault;
        this.description = description;
        this.monthOccured = monthOccured;
        this.yearOccured = yearOccured;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Fault(String id, String fault, String description, String monthOccured, String yearOccured, String imageUrl, String imageFileName, String longitude, String latitude, Operator_Division division) {
        this.id = id;
        this.fault = fault;
        this.description = description;
        this.monthOccured = monthOccured;
        this.yearOccured = yearOccured;
        this.imageUrl = imageUrl;
        this.imageFileName = imageFileName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.division = division;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFault() {
        return fault;
    }

    public void setFault(String fault) {
        this.fault = fault;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMonthOccured() {
        return monthOccured;
    }

    public void setMonthOccured(String monthOccured) {
        this.monthOccured = monthOccured;
    }

    public String getYearOccured() {
        return yearOccured;
    }

    public void setYearOccured(String yearOccured) {
        this.yearOccured = yearOccured;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public Operator_Division getDivision() {
        return division;
    }

    public void setDivision(Operator_Division division) {
        this.division = division;
    }
}

