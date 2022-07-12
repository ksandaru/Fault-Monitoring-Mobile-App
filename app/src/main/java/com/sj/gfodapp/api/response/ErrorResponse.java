package com.sj.gfodapp.api.response;

import com.google.gson.annotations.SerializedName;

public class ErrorResponse {
    @SerializedName("error")
    public String error;

    public ErrorResponse() {
    }

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "Error{" +
                "error='" + error + '\'' +
                '}';
    }
}
