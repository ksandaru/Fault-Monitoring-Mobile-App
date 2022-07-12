package com.sj.gfodapp.api.response.user;

import java.util.List;

public class UsersResponse {
    private List<com.sj.gfodapp.api.response.user.Profile> data;

    public UsersResponse() {
    }

    public UsersResponse(List<com.sj.gfodapp.api.response.user.Profile> data) {
        this.data = data;
    }

    public List<com.sj.gfodapp.api.response.user.Profile> getData() {
        return data;
    }

    public void setData(List<com.sj.gfodapp.api.response.user.Profile> data) {
        this.data = data;
    }
}
