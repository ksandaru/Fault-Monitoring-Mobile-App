package com.sj.gfodapp.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sj.gfodapp.R;
import com.sj.gfodapp.api.response.ErrorResponse;
import com.sj.gfodapp.api.response.userprofile.UserProfileRes;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.Navigation;
import com.sj.gfodapp.utils.RetrofitClient;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ViewUserFragment extends Fragment {

    static String apiBaseUrl = "";
    private SharedPreferences sharedPre;
    private static Context context;
    private String userID;

    private TextView tv_fullName,tv_status,tv_voteEligible,tv_email,tv_nic,tv_district,tv_city, tv_regNumber,tv_gn_name,tv_gn_district;
    private ImageView imageUser;

    public ViewUserFragment() {
        // Required empty public constructor
        Navigation.currentScreen = "ViewUserFragment";
    }

    public ViewUserFragment(String userID) {
        Navigation.currentScreen = "ViewUserFragment";
        this.userID = userID;
        getUserAccount();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv_fullName = (TextView) getView().findViewById(R.id.tv_fullName);
        tv_email = (TextView) getView().findViewById(R.id.tv_email);
        tv_status = (TextView) getView().findViewById(R.id.tv_status);
        tv_nic = (TextView) getView().findViewById(R.id.tv_nic);
        tv_voteEligible = (TextView) getView().findViewById(R.id.tv_voteEligible);
        tv_district = (TextView) getView().findViewById(R.id.tv_district);
        tv_city = (TextView) getView().findViewById(R.id.tv_city);
        tv_regNumber = (TextView) getView().findViewById(R.id.tv_regNumber);
        tv_gn_name = (TextView) getView().findViewById(R.id.tv_gn_name);
        tv_gn_district = (TextView) getView().findViewById(R.id.tv_gn_district);

        imageUser= (ImageView) getView().findViewById(R.id.imageUser);
    }


    //TODO: Get from API
    public void getUserAccount(){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);

        Call<JsonObject> call = apiInterface.getUserProfileByProfileID(this.userID);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) { // Code 200
                    JsonObject jsonObject = response.body();
                    UserProfileRes userProfRes= new Gson().fromJson(jsonObject, UserProfileRes.class);

                    tv_fullName.setText("Full NAME: " + userProfRes.getFullName());
                    tv_status.setText("STATUS: " + userProfRes.getStatus());
                    tv_voteEligible.setText("VOTE ELIGIBLE: " + userProfRes.getVoteEligible());
                    tv_email.setText("EMAIL: " + userProfRes.getLogin().getEmail());
                    tv_nic.setText("NIC: " + userProfRes.getNic());
                    tv_district.setText("DISTRICT: " + userProfRes.getDistrict());
                    tv_city.setText("" + userProfRes.getCity());

                    tv_regNumber.setText("REG No: " + userProfRes.getDivision().getRegNumber());
                    tv_gn_name.setText("NAME: " + userProfRes.getDivision().getName());
                    tv_gn_district.setText("DISTRICT: " + userProfRes.getDivision().getDistrict());

                    Picasso.get().load(userProfRes.getLogin().getAvatar()).into(imageUser);

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
}