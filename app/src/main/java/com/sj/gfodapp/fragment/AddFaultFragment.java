package com.sj.gfodapp.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
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
import com.sj.gfodapp.adapter.FaultsRecyclerviewAdapter;
import com.sj.gfodapp.api.response.FaultResponse;
import com.sj.gfodapp.api.response.ErrorResponse;
import com.sj.gfodapp.model.Fault;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppProgressDialog;
import com.sj.gfodapp.utils.AppSharedPreferences;
import com.sj.gfodapp.utils.FileUtils;
import com.sj.gfodapp.utils.Navigation;
import com.sj.gfodapp.utils.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


public class AddFaultFragment extends Fragment {

    static String apiBaseUrl = "";
    SharedPreferences sharedPre;
    private static Context context;
    Activity activity1;

    private static Dialog appProgressDialog, appCustomDialog;
    private Button btnAdd, btnGotoMapDisaster;
    private static TextView recycleViewHeading;
    private TextInputEditText txtSearch;
    private Chip chip_local_disaster, chip_all_disaster;

    private static RecyclerView userRecycler;
    private static FaultsRecyclerviewAdapter disastersRecyclerviewAdapter;
    private CharSequence search="";
    static FragmentManager fragmentManager;

    private static List<Fault> faultsList = new ArrayList<>();
    private String myDivisionID, selectedDivsion, selectedYear, currentUserRole;

    CharSequence[] options = {"Camera", "Gallery", "Cancel"};
    public ActivityResultLauncher<Intent> activityResultLaunch;
    public static ActivityResultLauncher<Intent> activityResultLaunchLocation;
    public String selectedImage = "";
    public String fileDialogSelectedOption = "";

    public AddFaultFragment() {
        // Required empty public constructor
        Navigation.currentScreen = "AddFaultFragment";
        this.myDivisionID = "all";
        this.selectedDivsion = "all";
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        selectedYear = String.valueOf(year);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        sharedPre= getActivity().getSharedPreferences("BordingFinPre",0);
        context = getContext();
        activity1 = getActivity();
        appProgressDialog = AppProgressDialog.createProgressDialog(context);

        fragmentManager = getActivity().getSupportFragmentManager();

        if(AppSharedPreferences.getData(sharedPre, "user-id")!=null){
            this.currentUserRole =   AppSharedPreferences.getData(sharedPre, "user-role");
            if(this.currentUserRole.equals("Operator")){
                getMyDivisionId();
            }
        }

        // TODO: Custom Dialog Start ::::::::::::::::::::::::::::::::::::::::::
        appCustomDialog = new Dialog(getContext());
        // >> TODO: Add Dialog Layout
        appCustomDialog.setContentView(R.layout.dialog_layout_add_disaster);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            appCustomDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.alert_background));
        }
        appCustomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        appCustomDialog.setCancelable(false);
        appCustomDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView labelFormHeading = appCustomDialog.findViewById(R.id.labelFormHeading);
        Chip chip_pick_image = appCustomDialog.findViewById(R.id.chip_pick_image);
        Chip chip_location = appCustomDialog.findViewById(R.id.chip_location);
        TextInputEditText txt_disaster= appCustomDialog.findViewById(R.id.txt_disaster);
        TextInputEditText txt_description= appCustomDialog.findViewById(R.id.txt_description);
        TextInputEditText txt_month= appCustomDialog.findViewById(R.id.txt_month);
        TextInputEditText txt_year= appCustomDialog.findViewById(R.id.txt_year);
        TextInputEditText txt_longitude = appCustomDialog.findViewById(R.id.txt_longitude);
        TextInputEditText txt_latitude = appCustomDialog.findViewById(R.id.txt_latitude);

        Button btn_dialog_btnCancel = appCustomDialog.findViewById(R.id.btn_dialog_btnCancel);
        Button btn_dialog_btnAdd = appCustomDialog.findViewById(R.id.btn_dialog_btnAdd);
        //labelFormHeading.setText("Add New "+ userType);
        chip_location.setText("Pick location");
        txt_longitude.setText("");
        txt_latitude.setText("");

        chip_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    activityResultLaunchLocation.launch(builder.build(activity1));
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        chip_pick_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requirePermission();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Select Image");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (options[which].equals("Camera")) {
                            Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            activityResultLaunch.launch(takePic);
                            fileDialogSelectedOption = "Camera";
                        } else if (options[which].equals("Gallery")) {
                            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            activityResultLaunch.launch(gallery);
                            fileDialogSelectedOption = "Gallery";
                        } else {
                            dialog.dismiss();
                            chip_pick_image.setText("Pick an Image");
                        }
                    }
                });
                builder.show();
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
                String fault = txt_disaster.getText().toString().trim();
                String description = txt_description.getText().toString().trim();
                String month = txt_month.getText().toString().trim();
                String year = txt_year.getText().toString().trim();
                String longitude = txt_longitude.getText().toString().trim();
                String latitude = txt_latitude.getText().toString().trim();

                if( selectedImage.equals("") || fault.equals("") || description.equals("") || month.equals("") || year.equals("") || longitude.equals("")){
                    showDialog(getActivity(),"Opps..!", "All fields are required.", ()->{});
                }else{
                    //TODO: Display Confirm Dialog
                    showConfirmDialog(getActivity(),() -> {
                        //TODO: confirmCall
                        txt_disaster.setText("");
                        txt_description.setText("");
                        txt_month.setText("");
                        txt_year.setText("");
                        appCustomDialog.dismiss();
                       addFault(new Fault(fault, description, month, year, longitude, latitude));
                    }, ()->{
                        //TODO: cancelCall
                        txt_disaster.setText("");
                        txt_description.setText("");
                        txt_month.setText("");
                        txt_year.setText("");
                    });
                }
            }
        });
        // TODO: Custom Dialog End ::::::::::::::::::::::::::::::::::::::::::

        activityResultLaunchLocation = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Chip chip_location;
                        TextInputEditText txt_longitude, txt_latitude;
                        chip_location = appCustomDialog.findViewById(R.id.chip_location);
                        txt_longitude = appCustomDialog.findViewById(R.id.txt_longitude);
                        txt_latitude = appCustomDialog.findViewById(R.id.txt_latitude);
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
                            if(txt_longitude.getText().equals("")){
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
        return inflater.inflate(R.layout.fragment_add_disaster, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recycleViewHeading = (TextView) getView().findViewById(R.id.recycleViewHeading);
        btnAdd = (Button) getView().findViewById(R.id.btnAdd);
        btnGotoMapDisaster = (Button) getView().findViewById(R.id.btnGotoMapDisaster);
        txtSearch = (TextInputEditText) getView().findViewById(R.id.txtSearch);
        chip_local_disaster= (Chip) getView().findViewById(R.id.chip_local_disaster);
        chip_all_disaster= (Chip) getView().findViewById(R.id.chip_all_disaster);
        userRecycler = (RecyclerView) getView().findViewById(R.id.userRecycler);

        if(currentUserRole.equals("Operator")){
            chip_local_disaster.setVisibility(View.VISIBLE);
            chip_all_disaster.setVisibility(View.VISIBLE);
        }else{
            chip_local_disaster.setVisibility(View.GONE);
            chip_all_disaster.setVisibility(View.VISIBLE);
        }

        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
               // disastersRecyclerviewAdapter.getFilter().filter(charSequence);
               // search = charSequence;
                /*
                selectedYear = charSequence.toString();
                if(selectedDivsion.equals("all")){
                    recycleViewHeading.setText("Disasters in all divisions on " + selectedYear);
                }else{
                    recycleViewHeading.setText("Disasters in My divisions on " + selectedYear);
                }
                getDisastersList();
                */
            }

            @Override
            public void afterTextChanged(Editable s) {
                selectedYear = txtSearch.getText().toString().trim();
                if(selectedDivsion.equals("all")){
                    recycleViewHeading.setText("Disasters in all divisions on " + selectedYear);
                }else{
                    recycleViewHeading.setText("Disasters in My divisions on " + selectedYear);
                }
                getFaultsList();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appCustomDialog.show();
            }
        });

        btnGotoMapDisaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Goto View User Fragment
                //  FragmentManager fragmentManager =  AddUsersFragment.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, new FaultsMapFragment(myDivisionID));
                //  fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        chip_all_disaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chip_all_disasterClick();
            }
        });

        chip_local_disaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chip_local_disasterClick();
            }
        });

        if(this.currentUserRole.equals("Operator")){
        }else{
           chip_all_disasterClick();
        }

        activityResultLaunch = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Chip chip_pick_image = appCustomDialog.findViewById(R.id.chip_pick_image);
                        if (result.getResultCode() == -1) {
                            switch (fileDialogSelectedOption) {
                                case "Camera":
                                    //TODO : do stuff when image captured by camera
                                    Bitmap image = (Bitmap) result.getData().getExtras().get("data");
                                    selectedImage = FileUtils.getPath(context, getImageUri(context, image));
                                    chip_pick_image.setText("Image Added");
                                   // userImage.setImageBitmap(image);  // <-- Show image in ImageView
                                    break;
                                case "Gallery":
                                    // TODO : do stuff when image Selected from gallery
                                    Uri imageUri = result.getData().getData();
                                    selectedImage = FileUtils.getPath(context, imageUri);
                                    chip_pick_image.setText("Image Added");
                                  //  userImage.setImageURI(imageUri);// <-- Show image in ImageView
                                    break;
                            }
                        } else if (result.getResultCode() == 0) { // Not selected
                            // TODO : do stuff when image not Selected
                            chip_pick_image.setText("Pick an Image");
                           // try {
                             //   userImage.setImageResource(R.drawable.ic_baseline_camera_alt_24);
                           // }catch (Exception e){e.printStackTrace();}
                        }
                    }
                });
    }

    public void requirePermission() {
        ActivityCompat.requestPermissions(activity1, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    public Uri getImageUri(Context context, Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "myImage", "");
        return Uri.parse(path);
    }

    private void chip_local_disasterClick(){
        selectedDivsion= myDivisionID;
        recycleViewHeading.setText("Disasters in My divisions on " + selectedYear);
        getFaultsList();
        chip_local_disaster.setChipBackgroundColorResource(R.color.colorAppPrimary);
        chip_local_disaster.setTextColor(Color.parseColor("#FFFFFF"));
        chip_all_disaster.setChipBackgroundColorResource(R.color.colorLightGray);
        chip_all_disaster.setTextColor(Color.parseColor("#000000"));
    }

    private void chip_all_disasterClick(){
        selectedDivsion = "all";
        recycleViewHeading.setText("Disasters in all divisions on " + selectedYear);
        getFaultsList();
        chip_all_disaster.setChipBackgroundColorResource(R.color.colorAppPrimary);
        chip_all_disaster.setTextColor(Color.parseColor("#FFFFFF"));
        chip_local_disaster.setChipBackgroundColorResource(R.color.colorLightGray);
        chip_local_disaster.setTextColor(Color.parseColor("#000000"));
    }

    public static void listItemOnClick(String faultId){
        //TODO: Goto View ViewDisasterFragmentViewDisasterFragment
        //  FragmentManager fragmentManager =  AddUsersFragment.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new ViewFaultFragment(faultId));
        //  fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void  getFaultsList(){
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
                        List<Fault> tempDataList= divisionRes.getData();
                        for(Fault item: tempDataList){

                            faultsList.add(new Fault(item.getId(),
                                                              item.getFault(),
                                                              item.getDescription(),
                                                              item.getMonthOccured(),
                                                              item.getYearOccured(),
                                                              item.getImageUrl(),
                                                              item.getImageFileName(),
                                                              item.getLongitude(),
                                                              item.getLatitude(),
                                                              item.getDivision()));
                                                }
                        setUserRecycler(tempDataList);
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

    private void addFault(Fault faultObj) {
        appProgressDialog.show();

        File file = new File(Uri.parse(selectedImage).getPath());
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", file.getName(), requestBody);

      //  System.out.println("getTotalSpace " + file.getTotalSpace() + "\n" + file.getName());

        RequestBody fault = RequestBody.create(MediaType.parse("multipart/form-data"), faultObj.getFault());
        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), faultObj.getDescription());
        RequestBody monthOccured = RequestBody.create(MediaType.parse("multipart/form-data"), faultObj.getMonthOccured());
        RequestBody yearOccured = RequestBody.create(MediaType.parse("multipart/form-data"), faultObj.getYearOccured());
        RequestBody divisionId = RequestBody.create(MediaType.parse("multipart/form-data"), myDivisionID);
        RequestBody longitude = RequestBody.create(MediaType.parse("multipart/form-data"), faultObj.getLongitude());
        RequestBody latitude = RequestBody.create(MediaType.parse("multipart/form-data"), faultObj.getLatitude());

        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);

        Call<ResponseBody> call = apiInterface.addFault(filePart, fault, description, monthOccured, yearOccured, divisionId, longitude, latitude);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) { //Response code : 200
                    showDialog(context, "Done..!", "Fault added", () -> {
                    });
                    getFaultsList();
                } else { //Response code : 400 response.code()
                    try {
                        ErrorResponse errResponse = null;
                        errResponse = new Gson().fromJson(response.errorBody().string(), ErrorResponse.class);
                        System.out.println("=========================ERROR========================= \n" + response.errorBody().string());
                        showDialog(context, "Opps..!", errResponse.getError(), () -> {
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        showDialog(context, "Opps..!", "Only image files are allowed", () -> {
                        });
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                appProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                appProgressDialog.dismiss();
                showDialog(context, "Opps..!", "Time out \nCould not connect", () -> {
                });
                System.out.println("_==================Error! Could not Access my API within 10seconds ==================\n" + t.getMessage());
            }
        });
    }

    private static void  setUserRecycler(List<Fault> uList){
        // vaccineRecycler = getView().findViewById(R.id.vaccineRecycler); TODO : Move to onViewCreated()
        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        userRecycler.setLayoutManager(layoutManager);
        disastersRecyclerviewAdapter = new FaultsRecyclerviewAdapter(context, faultsList);
        userRecycler.setAdapter(disastersRecyclerviewAdapter);
    }

    private void getMyDivisionId() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);

        String loginId ="0";
        if( AppSharedPreferences.getData(sharedPre,"user-id") != null){
            loginId =AppSharedPreferences.getData(sharedPre,"user-id");
        }

        Call<ResponseBody> call = apiInterface.getMyDivisionId(loginId);
        appProgressDialog.show();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                if (response.isSuccessful()) { //Response code : 200
                    ResponseBody responseBody = response.body();
                    String jsonData = null;
                    try {
                        jsonData = responseBody.string();
                        JSONObject responseObj = new JSONObject(jsonData);

                        myDivisionID = responseObj.getString("divisionId");
                        selectedDivsion = String.valueOf(myDivisionID);

                        if(currentUserRole.equals("Operator")){
                            recycleViewHeading.setText("Disasters in My divisions on " + selectedYear);
                            chip_local_disasterClick();
                        }else{
                            recycleViewHeading.setText("Disasters in all divisions on " + selectedYear);
                            chip_all_disasterClick();
                        }

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    appProgressDialog.dismiss();
                } else { //Response code : 400
                    appProgressDialog.dismiss();
                    // showDialog(context,"Opps...!", "Could not get data \n Do Refresh",()->{});
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                appProgressDialog.dismiss();
                // showDialog(context,"Opps...!", " Could not connect \n Check Internet Connection",()->{});
                System.out.println("_==================Error! Could not Access my API  ==================\n" + t.getMessage());
            }
        });
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