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
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.sj.gfodapp.R;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppProgressDialog;
import com.sj.gfodapp.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;

public class ForgetPassowrdActivity extends AppCompatActivity {

    private Button btnReqReset;
    private TextInputEditText txtLoginEmail;
    private static Dialog appProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_passowrd);

        appProgressDialog = AppProgressDialog.createProgressDialog(ForgetPassowrdActivity.this);
        txtLoginEmail = (TextInputEditText) findViewById(R.id.txtLoginEmail);
        btnReqReset = (Button) findViewById(R.id.btnReqReset);

        btnReqReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetPassword();
            }
        });
    }

    public void forgetPassword(){
        String email_text =  txtLoginEmail.getText().toString().trim();

        if(email_text.equals(""))
        {
            Toast.makeText(this,"Please enter your email",Toast.LENGTH_LONG).show();
        }
        else{
            String apiBaseUrl = getResources().getString(R.string.apiBaseUrl);

            ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
            Call<JsonObject> call = apiInterface.forgetPassword(email_text);
            appProgressDialog.show();
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                    if (response.isSuccessful()) { //Response code : 200
                        txtLoginEmail.setText("");
                        showDialog(ForgetPassowrdActivity.this, "Done!","You will receive an email for reset password",()->{
                            startActivity(new Intent(ForgetPassowrdActivity.this, LoginActivity.class));
                            finish();
                        });
                    }else{ //Response code : 400
                        System.out.println("_==================Error 400==================");
                    }
                    appProgressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    appProgressDialog.dismiss();
                    System.out.println("_==================Error! Could not Access my API  ==================\n"+ t.getMessage());
                }
            });
        }
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
}