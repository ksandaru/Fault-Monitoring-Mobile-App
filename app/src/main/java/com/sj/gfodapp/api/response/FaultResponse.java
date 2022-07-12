package com.sj.gfodapp.api.response;

import com.sj.gfodapp.model.Fault;

import java.util.List;

public class FaultResponse {
    private List<Fault> data;

    public FaultResponse() {
    }

    public FaultResponse(List<Fault> data) {
        this.data = data;
    }

    public List<Fault> getData() {
        return data;
    }

    public void setData(List<Fault> data) {
        this.data = data;
    }
}
