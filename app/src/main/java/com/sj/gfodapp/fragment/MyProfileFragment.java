package com.sj.gfodapp.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sj.gfodapp.R;
import com.sj.gfodapp.activity.LoginActivity;
import com.sj.gfodapp.api.response.ErrorResponse;
import com.sj.gfodapp.api.response.userprofile.UserProfileRes;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppProgressDialog;
import com.sj.gfodapp.utils.AppSharedPreferences;
import com.sj.gfodapp.utils.Navigation;
import com.sj.gfodapp.utils.RetrofitClient;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProfileFragment extends Fragment {

    String apiBaseUrl = "";
    SharedPreferences sharedPre;

    private CircleImageView userImage;
    private TextView tvOwnerName, tvOwnerEmail,tvRole;
    private TextInputEditText txtCurrentPassword, txtNewPassword, txtConfirmNewPassword;
    private Button btnLogout, btnGotoSettings,btnGoBack, btnUpdatePassoword;
    private MaterialCardView cardViewProfile, cardViewSettings;
    private static Dialog appProgressDialog;
    private static Context context;

    public MyProfileFragment() {
        // Required empty public constructor
        Navigation.currentScreen = "MyProfileFragment";
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        sharedPre= getActivity().getSharedPreferences("BordingFinPre",0);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appProgressDialog = AppProgressDialog.createProgressDialog(getContext());
        userImage = (CircleImageView) getView().findViewById(R.id.imageDisaster);
        tvOwnerName = (TextView) getView().findViewById(R.id.tvOwnerName);
        tvOwnerEmail = (TextView) getView().findViewById(R.id.tvOwnerEmail);
        tvRole = (TextView) getView().findViewById(R.id.tvRole);
        btnGotoSettings = (Button) getView().findViewById(R.id.btnGotoSettings);
        btnGoBack = (Button) getView().findViewById(R.id.btnGoBack);
        btnUpdatePassoword = (Button) getView().findViewById(R.id.btnUpdatePassoword);
        cardViewProfile = (MaterialCardView) getView().findViewById(R.id.cardViewProfile);
        cardViewSettings = (MaterialCardView) getView().findViewById(R.id.cardViewSettings);
        txtCurrentPassword = (TextInputEditText) getView().findViewById(R.id.txtCurrentPassword);
        txtNewPassword = (TextInputEditText) getView().findViewById(R.id.txtNewPassword);
        txtConfirmNewPassword = (TextInputEditText) getView().findViewById(R.id.txtConfirmNewPassword);
        btnLogout = (Button) getView().findViewById(R.id.btnLogout);
        getUserAccount();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppSharedPreferences.getData(sharedPre,"user-accessToken") != null){
                    // Remove user's data
                    AppSharedPreferences.removeData(sharedPre,"user-id");
                    AppSharedPreferences.removeData(sharedPre,"user-email");
                    AppSharedPreferences.removeData(sharedPre,"user-name");
                    AppSharedPreferences.removeData(sharedPre,"user-role");
                    AppSharedPreferences.removeData(sharedPre,"user-avatar");
                    AppSharedPreferences.removeData(sharedPre,"user-accessToken");
                    AppSharedPreferences.removeData(sharedPre,"user-tokenType");
                }
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });
        btnGotoSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewProfile.setVisibility(View.GONE);
                btnGotoSettings.setVisibility(View.GONE);
                cardViewSettings.setVisibility(View.VISIBLE);
            }
        });

        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewProfile.setVisibility(View.VISIBLE);
                btnGotoSettings.setVisibility(View.VISIBLE);
                cardViewSettings.setVisibility(View.GONE);
            }
        });

        btnUpdatePassoword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog(context, ()->{
                    if(txtNewPassword.getText().toString().trim().equals(txtConfirmNewPassword.getText().toString().trim())){
                        updatePassword(txtCurrentPassword.getText().toString().trim(), txtConfirmNewPassword.getText().toString().trim());
                    }else{
                        showDialog(context, "Oops...!","Confirm password Not match",()->{});
                    }
                },()->{

                });
            }
        });
    }


    //TODO: Get from API
    public void getUserAccount(){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);

        String loginId ="0";
        if( AppSharedPreferences.getData(sharedPre,"user-id") != null){
            loginId =AppSharedPreferences.getData(sharedPre,"user-id");
        }
        Call<JsonObject> call = apiInterface.getUserProfile(loginId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) { // Code 200
                    JsonObject jsonObject = response.body();
                    UserProfileRes userProfRes= new Gson().fromJson(jsonObject, UserProfileRes.class);
                    tvOwnerName.setText(userProfRes.getFullName());
                    tvOwnerEmail.setText(userProfRes.getLogin().getEmail());
                    tvRole.setText(userProfRes.getLogin().getRole().toUpperCase());
                    Picasso.get().load(userProfRes.getLogin().getAvatar()).into(userImage);

                } else { //Code : 400
                    System.out.println("================== ERROR while getting user account data ==================");
                    ErrorResponse errResponse = null;
                    try {
                        errResponse = new Gson().fromJson(response.errorBody().string(), ErrorResponse.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.err.println(errResponse.getError());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("_RES_"+ t.getMessage());
                //   Toast.makeText(getActivity(), "Could Load data..reload page!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updatePassword(String oldPswd, String newPswd){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);

        String loginId ="0";
        if( AppSharedPreferences.getData(sharedPre,"user-id") != null){
            loginId =AppSharedPreferences.getData(sharedPre,"user-id");
        }
        appProgressDialog.show();
        Call<JsonObject> call = apiInterface.updatePassword(loginId,oldPswd, newPswd);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) { // Code 200
                    appProgressDialog.dismiss();
                    showDialog(context, "Oops...!","Successfuly updated..!",()->{});
                    txtCurrentPassword.setText("");
                    txtConfirmNewPassword.setText("");
                    txtNewPassword.setText("");
                    cardViewProfile.setVisibility(View.VISIBLE);
                    btnGotoSettings.setVisibility(View.VISIBLE);
                    cardViewSettings.setVisibility(View.GONE);
                } else { //Code : 400
                    ErrorResponse errResponse = null;
                    try {
                        errResponse = new Gson().fromJson(response.errorBody().string(), ErrorResponse.class);
                        showDialog(context, "Oops...!",errResponse.getError(),()->{});
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