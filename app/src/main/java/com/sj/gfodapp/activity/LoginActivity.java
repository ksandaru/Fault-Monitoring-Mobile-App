package com.sj.gfodapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.sj.gfodapp.R;
import com.sj.gfodapp.api.response.login.AuthResponse;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppProgressDialog;
import com.sj.gfodapp.utils.AppSharedPreferences;
import com.sj.gfodapp.utils.RetrofitClient;

public class LoginActivity extends AppCompatActivity {


    private Button loginBtn, btnFogerPassword, btnGotoRegister;
    private TextInputEditText txtUsername, txtPassword;
    private Dialog appProgressDialog;

    private Context context;
    private SharedPreferences sharedPre;
    private String apiBaseUrl="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Avoid Status-bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        context = this;
        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        sharedPre=getSharedPreferences("BordingFinPre",0);
        appProgressDialog = AppProgressDialog.createProgressDialog(LoginActivity.this);

        /**--------------Hooks------------------------------------------*/
        loginBtn = findViewById(R.id.btnUserLogin);
        btnFogerPassword = findViewById(R.id.btnFogerPassword);
        btnGotoRegister = findViewById(R.id.btnGotoRegister);

        txtUsername = (TextInputEditText) findViewById(R.id.txtUsername);
        txtPassword = (TextInputEditText) findViewById(R.id.txtPassword);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username_text =  txtUsername.getText().toString().trim();
                String password_text =  txtPassword.getText().toString().trim();
                if(username_text.equals("") || password_text.equals(""))
                {
                    showDialog(context, "Make sure credentials are not Empty", ()->{});
                }
                else{
                    loginRequest(username_text, password_text);
                }
            }
        });

        btnFogerPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgetPassowrdActivity.class));
            }
        });

        btnGotoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        if(AppSharedPreferences.getData(sharedPre, "user-id")!=null)
            if(!AppSharedPreferences.getData(sharedPre, "user-id").equals("")){
                //Redirect to Home
                switch (AppSharedPreferences.getData(sharedPre, "user-role")){
                    case "Operator":
                        startActivity(new Intent(LoginActivity.this, OperatorPageContainerActivity.class));
                        finish();// Cant came back here after visiting Home page
                        break;
                    case "admin":
                        startActivity(new Intent(LoginActivity.this, AdminPageContainerActivity.class));
                        finish();// Cant came back here after visiting Home page
                        break;
                }
            }
    }

    private void loginRequest(String username, String password){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.userLogin(username,password);
        // System.out.println(":::::::::::: Request URL:" + call.request().url());
        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) { //Response code : 200
                    AuthResponse authResponse=null;
                    JsonObject jsonObject = response.body();
                    try {
                        authResponse = new Gson().fromJson(jsonObject, AuthResponse.class);
                        txtUsername.setText("");
                        txtPassword.setText("");
                        postLogin(authResponse);
                    }catch (Exception e){
                        System.out.println("_================== Exception==================");
                        e.printStackTrace();
                    }
                    appProgressDialog.dismiss();
                }else{ //Response code : 400
                    switch (response.code()){
                        case 400: //invalid credentials
                        case 401: //invalid credentials
                            showDialog(context, "Invalid credentials", ()->{
                                txtUsername.setText("");
                                txtPassword.setText("");
                            });
                            break;
                        case 403: // Didn't complete registration
                            ResponseBody responseBody2 = response.errorBody();
                            String jsonData2 = null;
                            try {
                                jsonData2 = responseBody2.string();
                                JSONObject responseObj = new JSONObject(jsonData2);
                                JSONObject dataObj = responseObj.getJSONObject("data");

                                String message = responseObj.getString("message");
                                String loginId = dataObj.getString("loginId");
                                String nic = dataObj.getString("nic");
                                String email = dataObj.getString("email");

                                showDialog(context, message, ()->{
                                    Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                                    intent.putExtra("loginId",loginId);
                                    startActivity(intent);
                                });

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                            break;

                    }
                    appProgressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                appProgressDialog.dismiss();
                showDialog(context, "Could not connect", ()->{});
                System.out.println("_==================Error! Could not Access my API  =================="+t.getMessage());
            }
        });
    }

    private void postLogin(AuthResponse authResponse){

        try {
            //Save User'Data
            AppSharedPreferences.saveData(sharedPre,"user-id", String.valueOf(authResponse.getUser().getId()));
            AppSharedPreferences.saveData(sharedPre,"user-email", authResponse.getUser().getEmail());
            AppSharedPreferences.saveData(sharedPre,"user-name", authResponse.getUser().getUsername());
            AppSharedPreferences.saveData(sharedPre,"user-role", authResponse.getUser().getRole());
            AppSharedPreferences.saveData(sharedPre,"user-avatar", authResponse.getUser().getAvatar());
            AppSharedPreferences.saveData(sharedPre,"user-accessToken", authResponse.getAccessToken());
            AppSharedPreferences.saveData(sharedPre,"user-tokenType", authResponse.getTokenType());

            //Redirect to Home
            switch (authResponse.getUser().getRole()){
                case "Operator":
                     startActivity(new Intent(LoginActivity.this, OperatorPageContainerActivity.class));
                      finish();// Cant came back here after visiting Home page
                    break;
                case "admin":
                    startActivity(new Intent(LoginActivity.this, AdminPageContainerActivity.class));
                    finish();finish();// Cant came back here after visiting Home page
                    break;
            }
        }catch (Exception e){
            System.out.println("_================== Exception appSharedPreferences ==================");
            e.printStackTrace();
        }

    }


    public static  void showDialog(
            @NonNull final Context context,
            String message,
            @Nullable Runnable confirmCallback
    ) {
        //TODO: Add TextInput Programatically...
        //  E.g. TextInputEditText myInput = new TextInputEditText(getContext());
        //  MaterialAlertDialogBuilder(context).addView(myInput)  <- Possible

        new MaterialAlertDialogBuilder(context)
                .setTitle("Opps!")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok",
                        (dialog2, which) -> {
                            dialog2.dismiss();
                            if (confirmCallback != null) confirmCallback.run();
                        })
                .show();
    }

}