package com.sj.gfodapp.api.response.userprofile;

import com.sj.gfodapp.api.response.user.Login;
import com.sj.gfodapp.model.Operator_Division;

public class UserProfileRes {
    private String id;
    private String fullName;
    private String status;
    private String nic;
    private String district;
    private String city;
    private String voteEligible;
    private String createdAt;
    private String updatedAt;
    private String loginId;
    private Login login;
    private Operator_Division division;

    public UserProfileRes() {
    }

    public UserProfileRes(String id, String fullName,String status, String nic, String district, String city, String voteEligible, String createdAt, String updatedAt, String loginId, Login login, Operator_Division division) {
        this.id = id;
        this.fullName = fullName;
        this.status = status;
        this.nic = nic;
        this.district = district;
        this.city = city;
        this.voteEligible = voteEligible;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.loginId = loginId;
        this.login = login;
        this.division = division;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status= status;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }


    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


    public String getVoteEligible() {
        return voteEligible;
    }

    public void setVoteEligible(String voteEligible) {
        this.voteEligible = voteEligible;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }

    public Operator_Division getDivision() {
        return division;
    }

    public void setDivision(Operator_Division division) {
        this.division = division;
    }
}
