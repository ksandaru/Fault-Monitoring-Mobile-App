package com.sj.gfodapp.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sj.gfodapp.R;
import com.sj.gfodapp.adapter.DivisionRecyclerviewAdapter;
import com.sj.gfodapp.api.response.DivisionResponse;
import com.sj.gfodapp.api.response.ErrorResponse;
import com.sj.gfodapp.model.Operator_Division;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppProgressDialog;
import com.sj.gfodapp.utils.Navigation;
import com.sj.gfodapp.utils.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class AddDivisionFragment extends Fragment {

    static String apiBaseUrl = "";
    private static Context context;
    private static Activity activity;
    private static Dialog appProgressDialog;
    private static Dialog appCustomUpdateDialog;

    private Button btnAdd;
    private Dialog appCustomDialog;
    private Button btnRefreshList;
    private static TextView labelNoOfVaccines;
    private TextInputEditText txtSearch;

    private static RecyclerView userRecycler;
    private static DivisionRecyclerviewAdapter divisionRecyclerviewAdapter;
    private CharSequence search="";
    private static List<Operator_Division> dataList = new ArrayList<>();

    static FragmentManager fragmentManager;
    public static ActivityResultLauncher<Intent> activityResultLaunch;

    String latitude = "";
    String longitude = "";
    private static boolean isDVDialogUpdateMode= false;

    public AddDivisionFragment() {
        // Required empty public constructor
        Navigation.currentScreen = "AddDivisionFragment";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        context = getContext();
        activity =getActivity();
        appProgressDialog = AppProgressDialog.createProgressDialog(context);

        fragmentManager = getActivity().getSupportFragmentManager();

        // TODO: Custom Dialog Start ::::::::::::::::::::::::::::::::::::::::::
        appCustomDialog = new Dialog(getContext());
        // >> TODO: Add Dialog Layout
        appCustomDialog.setContentView(R.layout.dialog_layout_add_division);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            appCustomDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.alert_background));
        }
        appCustomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        appCustomDialog.setCancelable(false);
        appCustomDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView labelFormHeading = appCustomDialog.findViewById(R.id.labelFormHeading);

        TextInputEditText txtRegNumber = appCustomDialog.findViewById(R.id.txtRegNumber);
        TextInputEditText txtName = appCustomDialog.findViewById(R.id.txtName);
        TextInputEditText txtDistrict = appCustomDialog.findViewById(R.id.txtDistrict);
        Chip chip_location = appCustomDialog.findViewById(R.id.chip_location);
        TextInputEditText txt_longitude = appCustomDialog.findViewById(R.id.txt_longitude);
        TextInputEditText txt_latitude = appCustomDialog.findViewById(R.id.txt_latitude);

        Button btn_dialog_btnCancel = appCustomDialog.findViewById(R.id.btn_dialog_btnCancel);
        Button btn_dialog_btnAdd = appCustomDialog.findViewById(R.id.btn_dialog_btnAdd);
        labelFormHeading.setText("Add New Division");
        chip_location.setText("Pick location");

        chip_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    activityResultLaunch.launch(builder.build(activity));
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_dialog_btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appCustomDialog.dismiss();
            }
        });

        btn_dialog_btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtRegNumber.getText().toString().trim().equals("")
                        || txtName.getText().toString().trim().equals("")
                        || txtDistrict.getText().toString().trim().equals("")
                        || txt_latitude.getText().toString().trim().equals("")
                ){
                    showDialog(getActivity(),"Opps..!", "All fields are required.", ()->{});
                }else{
                    //TODO: Display Confirm Dialog
                    showConfirmDialog(getActivity(),() -> {
                        //TODO: confirmCall
                        String regNumber = txtRegNumber.getText().toString().trim();
                        String name = txtName.getText().toString().trim();
                        String district = txtDistrict.getText().toString().trim();
                        String longitude = txt_longitude.getText().toString().trim();
                        String latitude = txt_latitude.getText().toString().trim();

                        txtRegNumber.setText("");
                        txtName.setText("");
                        txtDistrict.setText("");
                        txt_longitude.setText("");
                        txt_latitude.setText("");
                        appCustomDialog.dismiss();
                        submitDivsionData(regNumber, name, district, longitude, latitude);
                    }, ()->{
                        //TODO: cancelCall
                    });
                }
            }
        });
        // TODO: Custom Dialog End ::::::::::::::::::::::::::::::::::::::::::

        activityResultLaunch = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Chip chip_location;
                        TextInputEditText txt_longitude, txt_latitude;
                        if(isDVDialogUpdateMode){
                            chip_location = appCustomUpdateDialog.findViewById(R.id.chip_location);
                            txt_longitude = appCustomUpdateDialog.findViewById(R.id.txt_longitude);
                            txt_latitude = appCustomUpdateDialog.findViewById(R.id.txt_latitude);
                        }else{
                            chip_location = appCustomDialog.findViewById(R.id.chip_location);
                            txt_longitude = appCustomDialog.findViewById(R.id.txt_longitude);
                            txt_latitude = appCustomDialog.findViewById(R.id.txt_latitude);
                        }

                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            Place place = PlacePicker.getPlace(data, context);
                            StringBuilder stringBuilder = new StringBuilder();
                            String latitude = String.valueOf(place.getLatLng().latitude);
                            String longitude = String.valueOf(place.getLatLng().longitude);
                            System.out.println("=============== LON LAT : " + latitude + "  " + longitude);
                            chip_location.setText("Change location");
                            txt_longitude.setText(longitude);
                            txt_latitude.setText(latitude);
                        }else{
                            if(txt_longitude.getText().toString().equals("")){
                                chip_location.setText("Pick location");
                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_division, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnRefreshList = (Button) getView().findViewById(R.id.btnRefreshList);
        labelNoOfVaccines = (TextView) getView().findViewById(R.id.labelNoOfVaccines);
        btnAdd = (Button) getView().findViewById(R.id.btnAdd);
        txtSearch = (TextInputEditText) getView().findViewById(R.id.txtSearch);
        userRecycler = (RecyclerView) getView().findViewById(R.id.userRecycler);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDVDialogUpdateMode = false;
                appCustomDialog.show();
            }
        });

        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                divisionRecyclerviewAdapter.getFilter().filter(charSequence);
                search = charSequence;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnRefreshList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataList();
            }
        });

        getDataList();
    }


    public static void divisionsListItemOnClick(Operator_Division operator_division){
        showItemActionDialog(context, ()->{
            //TODO: DELETE
            showConfirmDialog(context, ()->{deleteItem(operator_division.getId());}, ()->{});
        }, ()->{
            //TODO: UPDATE
            showUpdateDialog(operator_division);
        }, ()->{
            //TODO: View
            //TODO: Goto View User Fragment
            //  FragmentManager fragmentManager =  AddUsersFragment.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, new ViewDivisionFragment(operator_division.getId()));
            //  fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }


    private static void  getDataList(){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.getDivisionList();

        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                if (response.isSuccessful()) { //Response code : 200
                    JsonObject jsonObject = response.body();
                    try {
                         dataList.clear();
                        DivisionResponse divisionRes = new Gson().fromJson(jsonObject, DivisionResponse.class);
                        List<Operator_Division> tempDataList= divisionRes.getData();
                        for(Operator_Division item: tempDataList){
                            dataList.add(new Operator_Division(item.getId(), item.getRegNumber(), item.getName(), item.getDistrict(), item.getLongitude(), item.getLatitude()));
                        }
                        setUserRecycler(dataList);
                        labelNoOfVaccines.setText(dataList.size() + " divisions added");
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }else{ //Response code : 400
                    showDialog(context,"Opps...!", "No divisions found",()->{});
                }
                appProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                appProgressDialog.dismiss();
                showDialog(context,"Opps...!", " Could not connect \n Check Internet Connection",()->{});
                System.out.println("_==================Error! Could not Access my API  ==================\n" + t.getMessage());
            }
        });
    }


    private void submitDivsionData(String regNumber, String name,String district,String longitude,String latitude){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.addDivision(regNumber, name, district, longitude, latitude);

        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                if (response.isSuccessful()) { //Response code : 200
                    showDialog(context,"Successful!", "Division added.",()->{});
                    getDataList();
                }else{ //Response code : 400

                    ErrorResponse errResponse = null;
                    try {
                        errResponse = new Gson().fromJson(response.errorBody().string(), ErrorResponse.class);
                        showDialog(context,"Opps...!", errResponse.getError(),()->{});
                    } catch (IOException e) {
                        e.printStackTrace();
                        showDialog(context,"Opps...!", "Cannot save, Try again",()->{});
                    }

                }
                appProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                appProgressDialog.dismiss();
                showDialog(context,"Opps...!", "Could not connect \n Check Internet Connection",()->{});
                System.out.println("_==================Error! Could not Access my API  ==================");
            }
        });
    }


    private static void updateDivision(Operator_Division operator_division){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.updateDivision(operator_division.getId(), operator_division.getRegNumber(), operator_division.getName(), operator_division.getDistrict(), operator_division.getLongitude(), operator_division.getLatitude());

        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                if (response.isSuccessful()) { //Response code : 200
                    showDialog(context,"Successful!", "Division updated.",()->{});
                    getDataList();
                }else{ //Response code : 400

                    ErrorResponse errResponse = null;
                    try {
                        errResponse = new Gson().fromJson(response.errorBody().string(), ErrorResponse.class);
                        showDialog(context,"Opps...!", errResponse.getError(),()->{});
                    } catch (IOException e) {
                        e.printStackTrace();
                        showDialog(context,"Opps...!", "Cannot save, Try again",()->{});
                    }

                }
                appProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                appProgressDialog.dismiss();
                showDialog(context,"Opps...!", "Could not connect \n Check Internet Connection",()->{});
                System.out.println("_==================Error! Could not Access my API  ==================");
            }
        });
    }


    public static void deleteItem(String id){
        showConfirmDialog(context, ()->{
            //TODO: confirm
            ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
            Call<JsonObject> call = apiInterface.deleteDivision(id);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                    if (response.isSuccessful()) { //Response code : 200
                        showDialog(context,"Successfull!", "Vaccine deleted.",()->{
                        });
                        getDataList();
                    }else{ //Response code : 400
                        ResponseBody responseBody1 = response.errorBody();
                        String jsonData = null;
                        try {
                            jsonData = responseBody1.string();
                            JSONObject responseObj = new JSONObject(jsonData);
                            String message = responseObj.getString("message");
                            showDialog(context,"Opps...!", message,()->{});
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            showDialog(context,"Opps...!", "Cannot save, Try again",()->{});
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    showDialog(context,"Opps...!", "Could not connect \n Check Internet Connection",()->{});
                    System.out.println("_==================Error! Could not Access my API  ==================");
                }
            });
        },()->{});

    }


    private static void showUpdateDialog(Operator_Division operator_division){
        isDVDialogUpdateMode =true;
        // TODO: Custom Dialog Start ::::::::::::::::::::::::::::::::::::::::::
        appCustomUpdateDialog = new Dialog(context);

        // >> TODO: Add Dialog Layout
        appCustomUpdateDialog.setContentView(R.layout.dialog_layout_add_division);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            appCustomUpdateDialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.alert_background));
        }
        appCustomUpdateDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        appCustomUpdateDialog.setCancelable(false);
        appCustomUpdateDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView labelFormHeading = appCustomUpdateDialog.findViewById(R.id.labelFormHeading);
        TextInputEditText txtRegNumber = appCustomUpdateDialog.findViewById(R.id.txtRegNumber);
        TextInputEditText txtName = appCustomUpdateDialog.findViewById(R.id.txtName);
        TextInputEditText txtDistrict = appCustomUpdateDialog.findViewById(R.id.txtDistrict);
        Button btn_dialog_btnCancel = appCustomUpdateDialog.findViewById(R.id.btn_dialog_btnCancel);
        Button btn_dialog_btnAdd = appCustomUpdateDialog.findViewById(R.id.btn_dialog_btnAdd);
        Chip chip_location = appCustomUpdateDialog.findViewById(R.id.chip_location);
        TextView txt_longitude = appCustomUpdateDialog.findViewById(R.id.txt_longitude);
        TextView txt_latitude = appCustomUpdateDialog.findViewById(R.id.txt_latitude);

        labelFormHeading.setText("Update Division");
        btn_dialog_btnAdd.setText("Update");
        chip_location.setText("Change location");
        txtRegNumber.setText(operator_division.getRegNumber());
        txtName.setText(operator_division.getName());
        txtDistrict.setText(operator_division.getDistrict());
        txt_longitude.setText(operator_division.getLongitude());
        txt_latitude.setText(operator_division.getLatitude());

        chip_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    activityResultLaunch.launch(builder.build(activity));
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_dialog_btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appCustomUpdateDialog.dismiss();
            }
        });

        btn_dialog_btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String regNumber = txtRegNumber.getText().toString().trim();
                String name = txtName.getText().toString().trim();
                String district = txtDistrict.getText().toString().trim();
                String longitude = txt_longitude.getText().toString().trim();
                String latitude = txt_latitude.getText().toString().trim();
                if(regNumber.equals("")
                        || name.equals("")
                        || district.equals("")
                        || longitude.equals("")
                ){
                    showDialog(context,"Opps..!", "All fields are required.", ()->{});
                }else{
                    //TODO: Display Confirm Dialog
                    showConfirmDialog(context,() -> {
                        //TODO: confirmCall
                        appCustomUpdateDialog.dismiss();
                        updateDivision(new Operator_Division(operator_division.getId(), regNumber, name, district, longitude, latitude));
                    }, ()->{
                        //TODO: cancelCall
                    });
                }
            }
        });
        appCustomUpdateDialog.show();
        // TODO: Custom Dialog End ::::::::::::::::::::::::::::::::::::::::::
    }


    private static void  setUserRecycler(List<Operator_Division> vaccineList){
        // vaccineRecycler = getView().findViewById(R.id.vaccineRecycler); TODO : Move to onViewCreated()
        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        userRecycler.setLayoutManager(layoutManager);
        divisionRecyclerviewAdapter = new DivisionRecyclerviewAdapter(context, dataList);
        userRecycler.setAdapter(divisionRecyclerviewAdapter);
    }

    public static  void showItemActionDialog(
            @NonNull final Context context,
            @Nullable Runnable confirmCallback,
            @Nullable Runnable cancelCallback,
            @Nullable Runnable neutralCallback
    ) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("ACTIONS")
                .setMessage("Choose an Action")
                .setCancelable(true)
                .setPositiveButton("DELETE",
                        (dialog2, which) -> {
                            dialog2.dismiss();
                            if (confirmCallback != null) confirmCallback.run();
                        })
                .setNegativeButton("MODIFY",
                        (dialog2, which) -> {
                            dialog2.dismiss();
                            if (cancelCallback != null) cancelCallback.run();
                        })
                .setNeutralButton("VIEW",
                        (dialog2, which) -> {
                            dialog2.dismiss();
                            if (neutralCallback != null) neutralCallback.run();
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