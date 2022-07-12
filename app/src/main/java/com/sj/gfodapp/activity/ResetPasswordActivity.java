package com.sj.gfodapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sj.gfodapp.R;
import com.sj.gfodapp.api.response.ErrorResponse;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppProgressDialog;
import com.sj.gfodapp.utils.RetrofitClient;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText txtCurrentPassword, txtNewPassword, txtConfirmNewPassword;
    private Button btnUpdatePassoword;
    private static Dialog appProgressDialog;

    private String apiBaseUrl="";
    private String loginID="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        this.loginID = getIntent().getStringExtra("loginId");

        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        appProgressDialog = AppProgressDialog.createProgressDialog(ResetPasswordActivity.this);
        txtCurrentPassword = (TextInputEditText) findViewById(R.id.txtCurrentPassword);
        txtNewPassword = (TextInputEditText) findViewById(R.id.txtNewPassword);
        txtConfirmNewPassword = (TextInputEditText) findViewById(R.id.txtConfirmNewPassword);
        txtConfirmNewPassword = (TextInputEditText) findViewById(R.id.txtConfirmNewPassword);
        btnUpdatePassoword = (Button) findViewById(R.id.btnUpdatePassoword);

        btnUpdatePassoword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog(ResetPasswordActivity.this, ()->{
                    if(txtNewPassword.getText().toString().trim().equals(txtConfirmNewPassword.getText().toString().trim())){
                        updatePassword(txtCurrentPassword.getText().toString().trim(), txtConfirmNewPassword.getText().toString().trim());
                    }else{
                        showDialog(ResetPasswordActivity.this, "Oops...!","Confirm password Not match",()->{});
                    }
                },()->{

                });
            }
        });
    }

    public void updatePassword(String oldPswd, String newPswd){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);

        appProgressDialog.show();
        Call<JsonObject> call = apiInterface.updatePassword(this.loginID,oldPswd, newPswd);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) { // Code 200
                    appProgressDialog.dismiss();
                    txtCurrentPassword.setText("");
                    txtConfirmNewPassword.setText("");
                    txtNewPassword.setText("");

                    showDialog(ResetPasswordActivity.this, "Done...!","Successfuly updated..!",()->{
                        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                        finish();
                    });
                } else { //Code : 400
                    ErrorResponse errResponse = null;
                    try {
                        errResponse = new Gson().fromJson(response.errorBody().string(), ErrorResponse.class);
                        showDialog(ResetPasswordActivity.this, "Oops...!",errResponse.getError(),()->{});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.err.println(errResponse.getError());
                    appProgressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("_RES_"+ t.getMessage());
                appProgressDialog.dismiss();
            }
        });
    }


    public static  void showDialog(
            @NonNull final Context context,
            String title,
            String message,
            @Nullable Runnable confirmCallback
    ) {
        //TODO: Add TextInput Programatically...
        //  E.g. TextInputEditText myInput = new TextInputEditText(getContext());
        //  MaterialAlertDialogBuilder(context).addView(myInput)  <- Possible

        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Ok",
                        (dialog2, which) -> {
                            dialog2.dismiss();
                            if (confirmCallback != null) confirmCallback.run();
                        })
                .show();
    }

    public static  void showConfirmDialog(
            @NonNull final Context context,
            @Nullable Runnable confirmCallback,
            @Nullable Runnable cancelCallback
    ) {
        //TODO: Add TextInput Programatically...
        //  E.g. TextInputEditText myInput = new TextInputEditText(getContext());
        //  MaterialAlertDialogBuilder(context).addView(myInput)  <- Possible

        new MaterialAlertDialogBuilder(context)
                .setTitle("Are you sure ?")
                .setMessage("Are you sure to continue this task")
                .setCancelable(false)
                .setPositiveButton("Confirm",
                        (dialog2, which) -> {
                            dialog2.dismiss();
                            if (confirmCallback != null) confirmCallback.run();
                        })
                .setNegativeButton("Cancel",
                        (dialog2, which) -> {
                            dialog2.dismiss();
                            if (cancelCallback != null) cancelCallback.run();
                        })
                .show();
    }
}