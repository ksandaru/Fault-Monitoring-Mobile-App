package com.sj.gfodapp.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sj.gfodapp.R;
import com.sj.gfodapp.adapter.UHFRecyclerviewAdapter;
import com.sj.gfodapp.adapter.UsersRecyclerviewAdapter;
import com.sj.gfodapp.api.response.DivisionResponse;
import com.sj.gfodapp.api.response.ErrorResponse;
import com.sj.gfodapp.api.response.UhfResponse;
import com.sj.gfodapp.api.response.user.Profile;
import com.sj.gfodapp.model.Operator_Division;
import com.sj.gfodapp.model.UHF;
import com.sj.gfodapp.model.User;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppProgressDialog;
import com.sj.gfodapp.utils.AppSharedPreferences;
import com.sj.gfodapp.utils.RetrofitClient;
import com.sj.gfodapp.utils.StaticStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;


public class UHFFragment extends Fragment {

    static String apiBaseUrl = "";
    static SharedPreferences sharedPre;
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
    private static UHFRecyclerviewAdapter divisionRecyclerviewAdapter;
    private CharSequence search="";
    private static List<UHF> dataList = new ArrayList<UHF>();

    static FragmentManager fragmentManager;
    public static ActivityResultLauncher<Intent> activityResultLaunch;

    private static boolean isDVDialogUpdateMode= false;

    public UHFFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        context = getContext();
        activity =getActivity();
        appProgressDialog = AppProgressDialog.createProgressDialog(context);
        sharedPre= getActivity().getSharedPreferences("BordingFinPre",0);

        fragmentManager = getActivity().getSupportFragmentManager();

        // TODO: Custom Dialog Start ::::::::::::::::::::::::::::::::::::::::::
        appCustomDialog = new Dialog(getContext());
        // >> TODO: Add Dialog Layout
        appCustomDialog.setContentView(R.layout.dialog_layout_add_uhf);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            appCustomDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.alert_background));
        }
        appCustomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        appCustomDialog.setCancelable(false);
        appCustomDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView labelFormHeading = appCustomDialog.findViewById(R.id.labelFormHeading);

        TextInputEditText txtUhfID = appCustomDialog.findViewById(R.id.txtUhfID);
        TextInputEditText txtLocationID = appCustomDialog.findViewById(R.id.txtLocationID);

        Button btn_dialog_btnCancel = appCustomDialog.findViewById(R.id.btn_dialog_btnCancel);
        Button btn_dialog_btnAdd = appCustomDialog.findViewById(R.id.btn_dialog_btnAdd);
        labelFormHeading.setText("Add New UHF Tag");
        btn_dialog_btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appCustomDialog.dismiss();
            }
        });

        btn_dialog_btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtUhfID.getText().toString().trim().equals("")
                        || txtLocationID.getText().toString().trim().equals("")
                ){
                    showDialog(getActivity(),"Opps..!", "All fields are required.", ()->{});
                }else{
                    //TODO: Display Confirm Dialog
                    showConfirmDialog(getActivity(),() -> {
                        //TODO: confirmCall
                        String UhfID = txtUhfID.getText().toString().trim();
                        String locationID = txtLocationID.getText().toString().trim();

                        txtUhfID.setText("");
                        txtLocationID.setText("");
                        appCustomDialog.dismiss();
                        submitUHFData(UhfID, locationID);
                    }, ()->{
                        //TODO: cancelCall
                    });
                }
            }
        });
        // TODO: Custom Dialog End ::::::::::::::::::::::::::::::::::::::::::
    }

    private void submitUHFData(String UhfID, String locationID){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.addUHFTag( String.valueOf(StaticStorage.myDivisionID) , UhfID, "LOOSE", locationID);

        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                if (response.isSuccessful()) { //Response code : 200
                    showDialog(context,"Successful!", "UHF added.",()->{});
                    getDataList();
                }else{ //Response code : 400
                    System.out.println("===================================");

                    ErrorResponse errResponse = null;
                    try {
                        System.out.println(response.errorBody().string());

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_u_h_f, container, false);
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

    private static void  getDataList(){

        String loginId ="0";
        if( AppSharedPreferences.getData(sharedPre,"user-id") != null){
            loginId =AppSharedPreferences.getData(sharedPre,"user-id");
        }

        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.getUHFTagsByUserId(loginId);

        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                if (response.isSuccessful()) { //Response code : 200
                    JsonObject jsonObject = response.body();
                    try {
                        dataList.clear();
                        UhfResponse divisionRes = new Gson().fromJson(jsonObject, UhfResponse.class);
                        List<UHF> tempDataList= divisionRes.getData();
                        for(UHF item: tempDataList){
                            dataList.add(new UHF(item.getId(), item.getUhfId(), item.getStatus(), item.getLocationId(), item.getDivisionId()));
                        }
                        setUserRecycler();
                        labelNoOfVaccines.setText(dataList.size() + " Tags added");
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }else{ //Response code : 400
                   // showDialog(context,"Opps...!", "No UHFs found",()->{});
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


    private static void  setUserRecycler(){
        // vaccineRecycler = getView().findViewById(R.id.vaccineRecycler); TODO : Move to onViewCreated()
        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        userRecycler.setLayoutManager(layoutManager);
        divisionRecyclerviewAdapter = new UHFRecyclerviewAdapter(context, dataList);
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