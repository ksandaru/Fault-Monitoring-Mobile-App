package com.sj.gfodapp.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sj.gfodapp.R;
import com.sj.gfodapp.api.response.FaultResponse;
import com.sj.gfodapp.model.Fault;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppProgressDialog;
import com.sj.gfodapp.utils.AppSharedPreferences;
import com.sj.gfodapp.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class FaultsMapFragment extends Fragment {

    static String apiBaseUrl = "";
    private SharedPreferences sharedPre;
    private static Context context;
    private static Activity activity;
    private static Dialog appProgressDialog;
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;

    private Chip chip_local_disaster, chip_all_disaster;
    private TextInputEditText txtSearch;
    private TextView recycleViewHeading;
    private CharSequence search = "";

    private static List<Fault> faultsList = new ArrayList<>();
    private String myDivisionID, selectedDivsion, selectedYear, currentUserRole;

    public FaultsMapFragment() {
        // Required empty public constructor
    }

    public FaultsMapFragment(String myDivisionID) {
        this.myDivisionID = myDivisionID;
        this.selectedDivsion = myDivisionID;

        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        selectedYear = String.valueOf(year);

        this.selectedDivsion = "all";
    }

    public FaultsMapFragment(Double lat, Double lon) {

        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        client = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            showLocation(true, lat, lon);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        sharedPre = getActivity().getSharedPreferences("BordingFinPre", 0);
        context = getContext();
        activity = getActivity();
        appProgressDialog = AppProgressDialog.createProgressDialog(context);

        if (AppSharedPreferences.getData(sharedPre, "user-id") != null) {
            this.currentUserRole = AppSharedPreferences.getData(sharedPre, "user-role");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_disasters_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chip_local_disaster = (Chip) getView().findViewById(R.id.chip_local_disaster);
        chip_all_disaster = (Chip) getView().findViewById(R.id.chip_all_disaster);
        txtSearch = (TextInputEditText) getView().findViewById(R.id.txtSearch);
        recycleViewHeading = (TextView) getView().findViewById(R.id.recycleViewHeading);

        if (currentUserRole.equals("Operator")) {
            chip_local_disaster.setVisibility(View.VISIBLE);
            chip_all_disaster.setVisibility(View.VISIBLE);
        } else {
            chip_local_disaster.setVisibility(View.GONE);
            chip_all_disaster.setVisibility(View.VISIBLE);
        }

        chip_local_disaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chip_local_disasterClick();
            }
        });

        chip_all_disaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chip_all_disasterClick();
            }
        });

        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                selectedYear = txtSearch.getText().toString().trim();
                if (selectedDivsion.equals("all")) {
                    recycleViewHeading.setText("Disasters in all divisions on " + selectedYear);
                    chip_all_disasterClick();
                } else {
                    recycleViewHeading.setText("Disasters in My divisions on " + selectedYear);
                    chip_local_disasterClick();
                }
            }
        });

        if (this.currentUserRole.equals("Operator")) {
            chip_local_disasterClick();
        } else {
            chip_all_disasterClick();
        }

    }

    private void chip_local_disasterClick() {
        selectedDivsion = myDivisionID;
        getFaultsList();
        recycleViewHeading.setText("Disasters in My divisions on " + selectedYear);
        chip_local_disaster.setChipBackgroundColorResource(R.color.colorAppPrimary);
        chip_local_disaster.setTextColor(Color.parseColor("#FFFFFF"));
        chip_all_disaster.setChipBackgroundColorResource(R.color.colorLightGray);
        chip_all_disaster.setTextColor(Color.parseColor("#000000"));
    }

    private void chip_all_disasterClick() {
        selectedDivsion = "all";
        getFaultsList();
        recycleViewHeading.setText("Disasters in all divisions on " + selectedYear);
        chip_all_disaster.setChipBackgroundColorResource(R.color.colorAppPrimary);
        chip_all_disaster.setTextColor(Color.parseColor("#FFFFFF"));
        chip_local_disaster.setChipBackgroundColorResource(R.color.colorLightGray);
        chip_local_disaster.setTextColor(Color.parseColor("#000000"));
    }

    private void getFaultsList() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.getFaults(selectedYear, selectedDivsion);

        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                if (response.isSuccessful()) { //Response code : 200
                    JsonObject jsonObject = response.body();
                    try {
                        faultsList.clear();
                        FaultResponse divisionRes = new Gson().fromJson(jsonObject, FaultResponse.class);
                        List<Fault> tempDataList = divisionRes.getData();
                        for (Fault item : tempDataList) {
                            System.out.println("=========== " + item.getFault() + "  " + item.getLatitude() + " " + item.getLongitude());

                           faultsList.add(new Fault(item.getId(), item.getFault(), item.getDescription(), item.getMonthOccured(), item.getYearOccured(), item.getImageUrl(), item.getImageFileName(), item.getLongitude(), item.getLatitude(), item.getDivision()));
                        }
                        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

                        client = LocationServices.getFusedLocationProviderClient(getActivity());
                        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            showLocation(false, 0.0, 0.0);
                        } else {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else { //Response code : 400
                    showDialog(context, "Opps...!", "No divisions found", () -> {
                    });
                }
                appProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                appProgressDialog.dismiss();
                showDialog(context, "Opps...!", " Could not connect \n Check Internet Connection", () -> {
                });
                System.out.println("_==================Error! Could not Access my API  ==================\n" + t.getMessage());
            }
        });
    }

    private void showLocation(Boolean isSingleLocation, Double lat, Double lon) {
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

                            if (isSingleLocation) {
                                // TODO: Show Single locations
                                LatLng latLng = new LatLng(lat, lon);
                                //Create maker options
                                MarkerOptions options = new MarkerOptions()
                                        .position(latLng)
                                        .title("Disaster Location");

                                //Zoom map
                                googleMap.animateCamera((CameraUpdateFactory.newLatLngZoom(latLng, 10)));
                                //Add Single marker on Map
                                googleMap.addMarker(options);
                            } else {
                                // TODO: Show Multiple locations
                                //  for(LatLng item: getMultipleLocations()){
                                //      googleMap.addMarker(new MarkerOptions().position(item).title("Title1"));
                                //       googleMap.animateCamera((CameraUpdateFactory.newLatLngZoom(item, 8)));
                                //  }

                                for (Fault item : faultsList) {
                                    googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(item.getLatitude()), Double.parseDouble(item.getLongitude()))).title(item.getFault()));
                                    googleMap.animateCamera((CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(item.getLatitude()), Double.parseDouble(item.getLongitude())), 8)));
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private List<LatLng> getMultipleLocations() {
        ArrayList<LatLng> locations = new ArrayList<>();
        for (Fault item : faultsList) {
            locations.add(new LatLng(Double.parseDouble(item.getLatitude()), Double.parseDouble(item.getLongitude())));
        }
        return locations;
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

    public static void showDialog(
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