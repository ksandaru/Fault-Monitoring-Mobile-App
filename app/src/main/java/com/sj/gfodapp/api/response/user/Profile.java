package com.sj.gfodapp.api.response.user;

public class Profile {
    private String id;
    private String fullName;
    private String status;
    private String nic;
    private String district;
    private String voteEligible;
    private String city;
    private Login login;

    public Profile() {
    }

    public Profile(String id, String fullName, String status, String nic, String district, String voteEligible, String city, Login login) {
        this.id = id;
        this.fullName = fullName;
        this.status=status;
        this.nic = nic;
        this.district = district;
        this.voteEligible = voteEligible;
        this.city = city;
        this.login = login;
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

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getVoteEligible() {
        return voteEligible;
    }

    public void setVoteEligible(String voteEligible) {
        this.voteEligible = voteEligible;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }
}
