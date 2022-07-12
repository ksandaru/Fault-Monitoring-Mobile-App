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
import com.sj.gfodapp.R;

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


public class AdminHomeFragment extends Fragment {

    private SharedPreferences sharedPre;
    private String apiBaseUrl = "";
    static FragmentManager fragmentManager;

    private TextView tvOwnerName, tvOwnerEmail, txtDetail2, tvGNCount, tvPeoplesCount, tvDivisionsCount;
    private MaterialCardView cardReaderOperator, cardPeoples, cardDivisions, cardDisasters;
    private CircleImageView userImage;

    public AdminHomeFragment() {
        // Required empty public constructor
        Navigation.currentScreen = "AdminHomeFragment";
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
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardReaderOperator = (MaterialCardView) getView().findViewById(R.id.cardReaderOperator);
        cardPeoples = (MaterialCardView) getView().findViewById(R.id.cardPeoples);
        cardDivisions = (MaterialCardView) getView().findViewById(R.id.cardDivisions);
        cardDisasters = (MaterialCardView) getView().findViewById(R.id.cardDisasters);
        tvOwnerName = (TextView) getView().findViewById(R.id.tvOwnerName);
        tvOwnerEmail = (TextView) getView().findViewById(R.id.tvOwnerEmail);
        txtDetail2 = (TextView) getView().findViewById(R.id.txtDetail2);
        tvGNCount = (TextView) getView().findViewById(R.id.tvGNCount);
        tvPeoplesCount = (TextView) getView().findViewById(R.id.tvPeoplesCount);
        tvDivisionsCount = (TextView) getView().findViewById(R.id.tvDivisionsCount);
        userImage = (CircleImageView) getView().findViewById(R.id.imageDisaster);

        if (AppSharedPreferences.getData(sharedPre, "user-accessToken") != null && AppSharedPreferences.getData(sharedPre, "user-accessToken") != null) {
            if (!AppSharedPreferences.getData(sharedPre, "user-accessToken").equals("")) {
                tvOwnerName.setText(AppSharedPreferences.getData(sharedPre, "user-name"));
                tvOwnerEmail.setText(AppSharedPreferences.getData(sharedPre, "user-email"));
                txtDetail2.setText(AppSharedPreferences.getData(sharedPre, "user-role"));
                Picasso.get().load(AppSharedPreferences.getData(sharedPre, "user-avatar")).into(userImage);
            }
        }
        getCounts();

        cardReaderOperator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoFragment(new AddUsersFragment("Operator"));
            }
        });

        cardPeoples.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoFragment(new AddUsersFragment("Rfid_tag"));
            }
        });

        cardDivisions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoFragment(new AddDivisionFragment());
            }
        });

        cardDisasters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoFragment(new AddFaultFragment());
            }
        });

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

                        String peoples = responseObj.getString("RFIDtagCount");
                        String GNs = responseObj.getString("OperatorCount");
                        String Divisions = responseObj.getString("DivisionsCount");
                        tvGNCount.setText(GNs + " Operators added");
                        tvPeoplesCount.setText(peoples + " RFID tags added");
                        tvDivisionsCount.setText(Divisions + " divisions added");
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

    private void gotoFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        //  fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}