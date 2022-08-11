package com.sj.gfodapp.api.response;

import com.sj.gfodapp.model.Operator_Division;
import com.sj.gfodapp.model.UHF;

import java.util.List;

public class UhfResponse {
    private List<UHF> data;

    public UhfResponse() {
    }

    public UhfResponse(List<UHF> data) {
        this.data = data;
    }

    public List<UHF> getData() {
        return data;
    }

    public void setData(List<UHF> data) {
        this.data = data;
    }
}
