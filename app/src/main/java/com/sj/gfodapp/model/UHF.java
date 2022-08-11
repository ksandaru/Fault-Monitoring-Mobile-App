package com.sj.gfodapp.model;

public class UHF {
    private Integer id;
    private  String uhfId;
    private String status;
    private String locationId;
    private  String divisionId;

    public UHF() {
    }

    public UHF(Integer id, String uhfId, String status, String locationId, String divisionId) {
        this.id = id;
        this.uhfId = uhfId;
        this.status = status;
        this.locationId = locationId;
        this.divisionId = divisionId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUhfId() {
        return uhfId;
    }

    public void setUhfId(String uhfId) {
        this.uhfId = uhfId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(String divisionId) {
        this.divisionId = divisionId;
    }

    @Override
    public String toString() {
        return "UHF{" +
                "id=" + id +
                ", uhfId='" + uhfId + '\'' +
                ", status='" + status + '\'' +
                ", locationId='" + locationId + '\'' +
                ", divisionId='" + divisionId + '\'' +
                '}';
    }
}
