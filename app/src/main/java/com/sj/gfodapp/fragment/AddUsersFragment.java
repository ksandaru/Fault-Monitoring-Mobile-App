package com.sj.gfodapp.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rustamg.filedialogs.FileDialog;
import com.rustamg.filedialogs.SaveFileDialog;
import com.sj.gfodapp.R;
import com.sj.gfodapp.adapter.ChooseDivisionRecyclerviewAdapter;
import com.sj.gfodapp.adapter.UsersRecyclerviewAdapter;
import com.sj.gfodapp.api.response.DivisionResponse;
import com.sj.gfodapp.api.response.ErrorResponse;
import com.sj.gfodapp.api.response.user.Profile;
import com.sj.gfodapp.api.response.user.UsersResponse;
import com.sj.gfodapp.api.response.userprofile.UserProfileRes;
import com.sj.gfodapp.model.Operator_Division;
import com.sj.gfodapp.model.Rfid_tag;
import com.sj.gfodapp.model.User;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppProgressDialog;
import com.sj.gfodapp.utils.Navigation;
import com.sj.gfodapp.utils.RetrofitClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AddUsersFragment extends Fragment {

    static String apiBaseUrl = "";
    private static Context context;
    private static Dialog appProgressDialog;

    private static Dialog appCustomDialog;
    private Button btnAddUser, btnRefreshList, btnExportToExcel;
    private static TextView labelNoOfVaccines, label_Heading;
    private TextInputEditText txtSearch;

    private static Dialog appCustomUpdateDialog;
    private static Dialog appDialogAddRfid_tag;

    private static RecyclerView userRecycler;
    private static UsersRecyclerviewAdapter usersRecyclerviewAdapter;
    private static ChooseDivisionRecyclerviewAdapter chooseDivisionRecyclerviewAdapter;
    private CharSequence search="";
    private static CharSequence searchDivision="";
    private static List<User> usersList = new ArrayList<>();
    private static List<Profile> usersProfileList = new ArrayList<>();
    private static List<Operator_Division> divisionsList = new ArrayList<>();
    private static String selectedDivisionId="";
    private static String selectedVoteEliType="all";
    private static boolean isSingleDivisionMode =false;

    static FragmentManager fragmentManager;

    private static String userType = "Operator";

    Integer READ_EXST = 10001;
    Integer WRITE_EXST =10002;

    public AddUsersFragment() {
        // Required empty public constructor
        Navigation.currentScreen = "AddUsersFragment";
    }

    public AddUsersFragment(String _userType) {
        switch (_userType){
            case "Operator":
                this.userType ="Operator";
                break;
        }
        Navigation.currentScreen = "AddUsersFragment";
    }

    public AddUsersFragment(Boolean _isSingleDivisionMode, String _divisionId) {
        this.userType ="Rfid_tag";
        selectedDivisionId = _divisionId;
        this.isSingleDivisionMode = _isSingleDivisionMode;
        Navigation.currentScreen = "AddUsersFragment";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        context = getContext();
        appProgressDialog = AppProgressDialog.createProgressDialog(context);

        fragmentManager = getActivity().getSupportFragmentManager();

        getDivisionsList();

        // TODO: Custom Dialog Start ::::::::::::::::::::::::::::::::::::::::::
        appCustomDialog = new Dialog(getContext());
        // >> TODO: Add Dialog Layout
        appCustomDialog.setContentView(R.layout.dialog_layout_add_user);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            appCustomDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.alert_background));
        }
        appCustomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        appCustomDialog.setCancelable(false);
        appCustomDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView labelFormHeading = appCustomDialog.findViewById(R.id.labelFormHeading);
        Chip chip_division = appCustomDialog.findViewById(R.id.chip_division);
        TextInputEditText txtUserNic = appCustomDialog.findViewById(R.id.txtUserNic);
        TextInputEditText txtUserEmail = appCustomDialog.findViewById(R.id.txtUserEmail);
        Button btn_dialog_btnCancel = appCustomDialog.findViewById(R.id.btn_dialog_btnCancel);
        Button btn_dialog_btnAdd = appCustomDialog.findViewById(R.id.btn_dialog_btnAdd);
        labelFormHeading.setText("Add New "+ userType);
        chip_division.setText("Choose Division");

        chip_division.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(divisionsList.size()>0)
                showDivisionsListDialog();
                else
                    showDialog(context, "Opps...!", "No divisions to choose", ()->{});
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
                if( selectedDivisionId.equals("") || txtUserNic.getText().toString().trim().equals("") || txtUserEmail.getText().toString().trim().equals("")){
                    showDialog(getActivity(),"Opps..!", "All fields are required.", ()->{});
                }else{
                    //TODO: Display Confirm Dialog
                    showConfirmDialog(getActivity(),() -> {
                        //TODO: confirmCall
                        String nic = txtUserNic.getText().toString().trim();
                        String email = txtUserEmail.getText().toString().trim();
                        txtUserNic.setText("");
                        txtUserEmail.setText("");
                        appCustomDialog.dismiss();
                        submitUserData(nic, email);
                    }, ()->{
                        //TODO: cancelCall
                    });
                }
            }
        });
        // TODO: Custom Dialog End ::::::::::::::::::::::::::::::::::::::::::
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_users, container, false);
    }

    FileDialog saveFileDialog = new SaveFileDialog();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnExportToExcel = (Button) getView().findViewById(R.id.btnExportToExcel);
        btnRefreshList = (Button) getView().findViewById(R.id.btnRefreshList);
        labelNoOfVaccines = (TextView) getView().findViewById(R.id.labelNoOfVaccines);
        label_Heading = (TextView) getView().findViewById(R.id.label_Heading);
        btnAddUser = (Button) getView().findViewById(R.id.btnAddUser);
        txtSearch = (TextInputEditText) getView().findViewById(R.id.txtSearch);
        userRecycler = (RecyclerView) getView().findViewById(R.id.userRecycler);

        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                usersRecyclerviewAdapter.getFilter().filter(charSequence);
                search = charSequence;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (userType){
                    case "Operator":
                        appCustomDialog.show();
                        break;
                    case "Rfid_tag":
                        showRfid_tagDialog(false, new Rfid_tag());
                        break;
                }
            }
        });

        btnRefreshList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUsersList(userType, "all");
                getDivisionsList();
            }
        });

        btnExportToExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXST);
                askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXST);

                // You can use either OpenFileDialog or SaveFileDialog depending on your needs
                saveFileDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Base_Theme_AppCompat);

                // Bundle args = new Bundle();
                // args.putString(FileDialog.EXTENSION, "xlsx"); // file extension is optional
                // saveFileDialog.setArguments(args);

                saveFileDialog.show(getActivity().getSupportFragmentManager(), SaveFileDialog.class.getName());
            }
        });

        getUsersList(userType, "all");
    }


    public static void exposrtToExcel(String path) {
        //  File sd = Environment.getExternalStorageDirectory();
        String csvFile = "Users.xls";

        System.out.println("========================== list size "+ usersList.size());

                //    File directory = new File(sd.getAbsolutePath());
        File directory = new File(path);

        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
          //  System.out.println("========================== create directory " + directory.getAbsolutePath());
        }
        try {
            //file path
            File file = new File(directory, csvFile);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale(Locale.GERMAN.getLanguage(), Locale.GERMAN.getCountry()));
            WritableWorkbook workbook;
            workbook = Workbook.createWorkbook(file, wbSettings);

            //Excel sheetA first sheetA
            WritableSheet sheetA = workbook.createSheet("Users", 0);

            // column and row titles
            sheetA.addCell(new Label(0, 0, "Full Name"));
            sheetA.addCell(new Label(1, 0, "STATUS"));
            sheetA.addCell(new Label(2, 0, "NIC No"));
//            sheetA.addCell(new Label(3, 0, "VOTE Eligible"));

            for(int i=0; i < usersProfileList.size(); i++){
                sheetA.addCell(new Label(0, i+1, usersProfileList.get(i).getFullName()));
                sheetA.addCell(new Label(1, i+1, usersProfileList.get(i).getStatus()));
                sheetA.addCell(new Label(3, i+1, usersProfileList.get(i).getNic()));
//                sheetA.addCell(new Label(4, i+1, usersProfileList.get(i).getVoteEligible()));
            }

            //Excel sheetB represents second sheet
            //WritableSheet sheetB = workbook.createSheet("sheet B", 1);

            // column and row titles
            // sheetB.addCell(new Label(0, 0, "sheet B 1"));
            // sheetB.addCell(new Label(1, 0, "sheet B 2"));
            // sheetB.addCell(new Label(0, 1, "sheet B 3"));
            // sheetB.addCell(new Label(1, 1, "sheet B 4"));

            // close workbook
            workbook.write();
            workbook.close();

            showDialog(context,"Done!", "Your data has been Exported.", ()-> {});

           // System.out.println("========================== Createed file path " + directory.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showDialog(context,"Export Failed!", e.getMessage(), ()-> {});
            System.out.println("========================== Error " + e.getMessage());
        }
    }

    public  static void userListItemOnClick(String userProfileId){
        //TODO: Goto View User Fragment
        //  FragmentManager fragmentManager =  AddUsersFragment.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new ViewUserFragment(userProfileId));
        //  fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public static void rfid_tagListItemOnClick(String userProfileId){
        showItemActionDialog(context, ()->{
            //TODO: DELETE
            showConfirmDialog(context, ()->{deleteItem(userProfileId);}, ()->{});
        }, ()->{
            //TODO: UPDATE
            getUserAccount(userProfileId);
        },()->{
            //TODO: VIEW
            //TODO: Goto View User Fragment
            //  FragmentManager fragmentManager =  AddUsersFragment.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, new ViewUserFragment(userProfileId));
            //  fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }


    private static void  getUsersList(String role, String tagStatus){

        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
//        Call<JsonObject> call = apiInterface.getUsersByRole(role, selectedVoteEliType, tagStatus);
        Call<JsonObject> call = apiInterface.getUsersByRole("Operator");
         System.out.println("===========================URL " + call.request().url());
        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                if (response.isSuccessful()) { //Response code : 200
                    JsonObject jsonObject = response.body();
                    try {
                        usersList.clear();
                        usersProfileList.clear();
                        UsersResponse usersResponse = new Gson().fromJson(jsonObject, UsersResponse.class);
                        List<Profile> dataList= usersResponse.getData();
                        for(Profile item:dataList){
                            usersProfileList.add(item);
                        }
                        String displayEmail="";
                        for(Profile item: dataList){
                            if(item.getLogin().getRole().equals("Operator"))
                                displayEmail =item.getLogin().getEmail();
                            else  displayEmail = "Vote Eligible: " + item.getVoteEligible();

                            usersList.add(new User(item.getId(),item.getFullName(),item.getStatus(),item.getNic(),displayEmail ,item.getLogin().getAvatar()));
                        }
                        setUserRecycler(usersList);
                        labelNoOfVaccines.setText(dataList.size() + " users");
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }else{ //Response code : 400
                    showDialog(context,"Opps...!", "No users found",()->{});
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


    public static void getUserAccount(String userProId){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);

        Call<JsonObject> call = apiInterface.getUserProfileByProfileID(userProId);
        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) { // Code 200
                    JsonObject jsonObject = response.body();
                    UserProfileRes user= new Gson().fromJson(jsonObject, UserProfileRes.class);

                    System.out.println("================== data ==================");
                    System.out.println(user.getFullName() + "  ST: " + user.getStatus());

                    showRfid_tagDialog(true, new Rfid_tag(userProId, user.getFullName(), user.getCity(),user.getStatus()));

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
                appProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                appProgressDialog.dismiss();
                System.out.println("_RES_"+ t.getMessage());
            }
        });
    }

    private void submitUserData(String nic, String email){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.addUser(userType, selectedDivisionId, nic, email);

        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                if (response.isSuccessful()) { //Response code : 200
                    showDialog(context,"Successfull!", "- User added. \n- Email has been sent.",()->{});

                    if(isSingleDivisionMode){
                        getUsersList(userType, selectedDivisionId);
                    }else{
                        getUsersList(userType, "all");
                        getDivisionsList();
                    }

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


    private static void submitRfid_tagData(boolean isUpdate, Rfid_tag rfidtag){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
        Call<JsonObject> call;
        if(isUpdate) call = apiInterface.updateRfid_tag(rfidtag.getProfileId(), rfidtag.getFullName(), rfidtag.getCity(), rfidtag.getStatus(), selectedDivisionId);
        else call = apiInterface.addRfid_tag(selectedDivisionId, rfidtag.getFullName(),rfidtag.getCity(),rfidtag.getStatus() );

        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) { //Response code : 200
                    String txtMode = isUpdate ? "updated": "saved";
                    showDialog(context,"Successfull!", "Rfid_tag "+txtMode+" successfully!",()->{});

                    if(isSingleDivisionMode){
                    //    getUsersList(userType, selectedDivisionId);
                        getUsersList(userType, "all");
                    }else{
                        getUsersList(userType, "all");
                        getDivisionsList();
                    }

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


    private static void deleteItem(String divId){

    }


    private static void showRfid_tagDialog(boolean isUpdateMode, Rfid_tag rfidtag){
        // TODO: Custom Dialog Start ::::::::::::::::::::::::::::::::::::::::::
        appDialogAddRfid_tag = new Dialog(context);

        // >> TODO: Add Dialog Layout
        appDialogAddRfid_tag.setContentView(R.layout.dialog_layout_add_person);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            appDialogAddRfid_tag.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.alert_background));
        }
        appDialogAddRfid_tag.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        appDialogAddRfid_tag.setCancelable(false);
        appDialogAddRfid_tag.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextInputEditText txt_fullName = appDialogAddRfid_tag.findViewById(R.id.txt_fullName);
        TextInputEditText txt_city = appDialogAddRfid_tag.findViewById(R.id.txt_city);
        TextInputEditText txt_status = appDialogAddRfid_tag.findViewById(R.id.txt_status);

        TextView labelFormHeading = appDialogAddRfid_tag.findViewById(R.id.labelFormHeading);
        Button btn_dialog_btnCancel = appDialogAddRfid_tag.findViewById(R.id.btn_dialog_btnCancel);
        Button btn_dialog_btnAdd = appDialogAddRfid_tag.findViewById(R.id.btn_dialog_btnAdd);
        btn_dialog_btnAdd.setText("ADD");
        labelFormHeading.setText("Add RFID Tag");
        if(isUpdateMode){
            btn_dialog_btnAdd.setText("UPDATE");
            labelFormHeading.setText("Update RFID tag");
            txt_fullName.setText(rfidtag.getFullName());
            txt_city.setText(rfidtag.getCity());
            txt_status.setText(rfidtag.getStatus());
        }
        btn_dialog_btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appDialogAddRfid_tag.dismiss();
            }
        });

        btn_dialog_btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = txt_fullName.getText().toString().trim();
                String city = txt_city.getText().toString().trim();
                String status = txt_status.getText().toString().trim();

                if(fullName.equals("")
                        || fullName.equals("")
                        || city.equals("")
                        || status.equals("")
                ){
                    showDialog(context,"Opps..!", "All fields are required.", ()->{});
                }else{
                    //TODO: Display Confirm Dialog
                    showConfirmDialog(context,() -> {
                        //TODO: confirmCall
                        appDialogAddRfid_tag.dismiss();
                        submitRfid_tagData(isUpdateMode, new Rfid_tag( rfidtag.getProfileId(),fullName,city,status));
                    }, ()->{
                        //TODO: cancelCall
                        appDialogAddRfid_tag.dismiss();
                    });
                }
            }
        });
        appDialogAddRfid_tag.show();
        // TODO: Custom Dialog End ::::::::::::::::::::::::::::::::::::::::::
    }


    private static void showDivisionsListDialog(){
        // TODO: Custom Dialog Start ::::::::::::::::::::::::::::::::::::::::::
        appCustomUpdateDialog = new Dialog(context);

        // >> TODO: Add Dialog Layout
        appCustomUpdateDialog.setContentView(R.layout.dialog_layout_divisions_list);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            appCustomUpdateDialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.alert_background));
        }
        appCustomUpdateDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        appCustomUpdateDialog.setCancelable(false);
        appCustomUpdateDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextInputEditText txtSearch = appCustomUpdateDialog.findViewById(R.id.txtSearch);
        RecyclerView divisionsListRecycler =  appCustomUpdateDialog.findViewById(R.id.divisionsListRecycler);
        ImageButton imgBtnClose =  appCustomUpdateDialog.findViewById(R.id.imgBtnClose);

        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        divisionsListRecycler.setLayoutManager(layoutManager);

        chooseDivisionRecyclerviewAdapter = new ChooseDivisionRecyclerviewAdapter(context, divisionsList);
        divisionsListRecycler.setAdapter(chooseDivisionRecyclerviewAdapter);

        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                chooseDivisionRecyclerviewAdapter.getFilter().filter(charSequence);
                searchDivision = charSequence;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appCustomUpdateDialog.dismiss();
            }
        });

        appCustomUpdateDialog.show();
        // TODO: Custom Dialog End ::::::::::::::::::::::::::::::::::::::::::
    }

    private static void  getDivisionsList(){
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.getDivisionList();

        appProgressDialog.show();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                if (response.isSuccessful()) { //Response code : 200
                    JsonObject jsonObject = response.body();
                    try {
                        divisionsList.clear();
                        DivisionResponse divisionRes = new Gson().fromJson(jsonObject, DivisionResponse.class);
                        List<Operator_Division> tempDataList= divisionRes.getData();
                        for(Operator_Division item: tempDataList){
                            divisionsList.add(new Operator_Division(item.getId(), item.getRegNumber(), item.getName(), item.getDistrict(), item.getLongitude(), item.getLatitude()));
                        }
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

    public static void setSelectedDivisionId(String id, String name){
        selectedDivisionId = id;
        appCustomUpdateDialog.dismiss();
        Chip chip_division = appCustomDialog.findViewById(R.id.chip_division);
        chip_division.setText(name);
    }



    private static void  setUserRecycler(List<User> uList){
        // vaccineRecycler = getView().findViewById(R.id.vaccineRecycler); TODO : Move to onViewCreated()
        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        userRecycler.setLayoutManager(layoutManager);
        usersRecyclerviewAdapter = new UsersRecyclerviewAdapter(context, usersList);
        userRecycler.setAdapter(usersRecyclerviewAdapter);
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(getActivity(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    getActivity(), permission)) {
                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{permission}, requestCode);
            }
        } else {
          //  Toast.makeText(this, permission + " is already granted.",Toast.LENGTH_SHORT).show();
        }
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
}