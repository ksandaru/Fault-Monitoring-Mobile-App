package com.sj.gfodapp.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sj.gfodapp.R;

import com.sj.gfodapp.api.response.ErrorResponse;
import com.sj.gfodapp.api.response.userprofile.UserProfileRes;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppSharedPreferences;
import com.sj.gfodapp.utils.Navigation;
import com.sj.gfodapp.utils.RetrofitClient;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorHomeFragment extends Fragment {

    private SharedPreferences sharedPre;
    private String apiBaseUrl = "";
    static FragmentManager fragmentManager;

    private TextView tvOwnerName, tvOwnerEmail, txtDetail2,tvDivisionName, tvRegNo, tv_district, tvPersonCount;
    private MaterialCardView cardLocalDisasters, cardTags;
    private CircleImageView userImage;
    private String myDivisionID;

    public OperatorHomeFragment() {
        // Required empty public constructor
        Navigation.currentScreen = "OperatorHomeFragment";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        sharedPre = getActivity().getSharedPreferences("BordingFinPre", 0);
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_g_n_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardTags = (MaterialCardView) getView().findViewById(R.id.cardTags);
        cardLocalDisasters = (MaterialCardView) getView().findViewById(R.id.cardLocalDisasters);

        tvOwnerName = (TextView) getView().findViewById(R.id.tvOwnerName);
        tvOwnerEmail = (TextView) getView().findViewById(R.id.tvOwnerEmail);
        txtDetail2 = (TextView) getView().findViewById(R.id.txtDetail2);

        tvDivisionName = (TextView) getView().findViewById(R.id.tvDivisionName);
        tvRegNo = (TextView) getView().findViewById(R.id.tvRegNo);
        tv_district = (TextView) getView().findViewById(R.id.tv_district);
        tvPersonCount = (TextView) getView().findViewById(R.id.tvPersonCount);

        userImage = (CircleImageView) getView().findViewById(R.id.imageDisaster);

        getCounts();

        cardTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoFragment(new UHFFragment());
            }
        });

        cardLocalDisasters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoFragment(new AddFaultFragment());
            }
        });

        getUserAccount();
    }

    private void getCounts() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
        Call<ResponseBody> call = apiInterface.getCounts();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                if (response.isSuccessful()) { //Response code : 200
                    ResponseBody responseBody = response.body();
                    String jsonData = null;
                    try {
                        jsonData = responseBody.string();
                        JSONObject responseObj = new JSONObject(jsonData);

                        String peoples = responseObj.getString("RFITagsCount");
                        String GNs = responseObj.getString("OperatorCount");
                        String Divisions = responseObj.getString("DivisionsCount");

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else { //Response code : 400
                    // showDialog(context,"Opps...!", "Could not get data \n Do Refresh",()->{});
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // showDialog(context,"Opps...!", " Could not connect \n Check Internet Connection",()->{});
                System.out.println("_==================Error! Could not Access my API  ==================\n" + t.getMessage());
            }
        });
    }


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

                    tvDivisionName.setText(userProfRes.getDivision().getName());
                    tvRegNo.setText(userProfRes.getDivision().getRegNumber());
                    tv_district.setText(userProfRes.getDivision().getDistrict());
                    myDivisionID = userProfRes.getDivision().getId();
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

    private void gotoFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        //  fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}