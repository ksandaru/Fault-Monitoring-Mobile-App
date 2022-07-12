package com.sj.gfodapp.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sj.gfodapp.R;
import com.sj.gfodapp.api.response.ErrorResponse;
import com.sj.gfodapp.api.response.single_fault.SingleFaultResponse;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.Navigation;
import com.sj.gfodapp.utils.RetrofitClient;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewFaultFragment extends Fragment {

    static String apiBaseUrl = "";
    private static Context context;
    private static Activity activity;

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;

    ImageView imageDisaster;
    TextView tv_disaster, tv_desc, tv_monthAndYear, tv_DivNameAndRegNo,tv_GN_Nametv, tvGN_Email, tv_GN_Contact;
    CircleImageView imageGN;

    String faultID;

    public ViewFaultFragment() {
        // Required empty public constructor
    }

    public ViewFaultFragment(String faultID) {
        Navigation.currentScreen = "ViewDisasterFragment";
        this.faultID = faultID;
        getFault();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        context = getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_disaster, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageDisaster = (ImageView) getView().findViewById(R.id.imageDisaster);
        tv_disaster = (TextView) getView().findViewById(R.id.tv_disaster);
        tv_desc = (TextView) getView().findViewById(R.id.tv_desc);
        tv_monthAndYear = (TextView) getView().findViewById(R.id.tv_monthAndYear);
        tv_DivNameAndRegNo = (TextView) getView().findViewById(R.id.tv_DivNameAndRegNo);
        tv_GN_Nametv = (TextView) getView().findViewById(R.id.tv_GN_Name);
        tvGN_Email = (TextView) getView().findViewById(R.id.tvGN_Email);

        imageGN = (CircleImageView) getView().findViewById(R.id.imageGN);
    }

    public void getFault() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);

        Call<JsonObject> call = apiInterface.getFaultByID(this.faultID);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) { // Code 200
                    JsonObject jsonObject = response.body();
                    SingleFaultResponse fault = new Gson().fromJson(jsonObject, SingleFaultResponse.class);

                    tv_disaster.setText(fault.getFault().getFault());
                    tv_desc.setText(fault.getFault().getDescription());
                    tv_monthAndYear.setText(" Month/Year: " + fault.getFault().getMonthOccured() + "/" + fault.getFault().getYearOccured());
                    tv_DivNameAndRegNo.setText("REG No: " + fault.getFault().getDivision().getRegNumber() + " - " + fault.getFault().getDivision().getName());

                    tv_GN_Nametv.setText(fault.getReaderOperator().getFullName());
                    tvGN_Email.setText(fault.getReaderOperator().getLogin().getEmail());

                    Picasso.get().load(fault.getFault().getImageUrl()).into(imageDisaster);
                    Picasso.get().load(fault.getReaderOperator().getLogin().getAvatar()).into(imageGN);

                   // supportMapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.google_map);
                    supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

                    client = LocationServices.getFusedLocationProviderClient(getActivity());
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        showtLocation(fault.getFault().getFault(), Double.parseDouble(fault.getFault().getLatitude()), Double.parseDouble(fault.getFault().getLongitude()));
                    } else {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                    }

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
                System.out.println("_RES_" + t.getMessage());
            }
        });
    }

    private void showtLocation(String DV_name, Double latitude, Double longitude) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling ActivityCompat#requestPermissions
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //Sync map
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            //initialize lat lng
                            //  LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            LatLng latLng = new LatLng(latitude, longitude);

                            //Create maker options
                            MarkerOptions options = new MarkerOptions().position(latLng)
                                    .title(DV_name);

                            //Zoom map
                            googleMap.animateCamera((CameraUpdateFactory.newLatLngZoom(latLng, 10)));
                            //Add Single marker on Map
                            googleMap.addMarker(options);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //  getCurrentLocation();
            }
        }
    }
}