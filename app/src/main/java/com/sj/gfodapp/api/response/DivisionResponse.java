package com.sj.gfodapp.api.response;

import com.sj.gfodapp.model.Operator_Division;

import java.util.List;

public class DivisionResponse {
    private List<Operator_Division> data;

    public DivisionResponse() {
    }

    public DivisionResponse(List<Operator_Division> data) {
        this.data = data;
    }

    public List<Operator_Division> getData() {
        return data;
    }

    public void setData(List<Operator_Division> data) {
        this.data = data;
    }
}
