package com.sj.gfodapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.rustamg.filedialogs.FileDialog;
import com.sj.gfodapp.R;
import com.sj.gfodapp.fragment.AddFaultFragment;
import com.sj.gfodapp.fragment.AddDivisionFragment;
import com.sj.gfodapp.fragment.AddUsersFragment;
import com.sj.gfodapp.fragment.OperatorHomeFragment;
import com.sj.gfodapp.fragment.MyProfileFragment;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppProgressDialog;
import com.sj.gfodapp.utils.AppSharedPreferences;
import com.sj.gfodapp.utils.Navigation;
import com.sj.gfodapp.databinding.ActivityGnpageContainerBinding;
import com.sj.gfodapp.utils.RetrofitClient;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class OperatorPageContainerActivity extends AppCompatActivity implements FileDialog.OnFileSelectedListener {

    ActivityGnpageContainerBinding binding;
    private SharedPreferences sharedPre;
    private String apiBaseUrl = "";
    private static Dialog appProgressDialog;
    private String myDivisionId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        sharedPre = getSharedPreferences("BordingFinPre", 0);

        //TODO:::::::::::::::::::::::IMPORTANT::::::::::::::::::::::
        /**
         * Make sure enable viewBingg at build.gradle file
         *     buildFeatures{
         *         viewBinding true
         *     }
         */
        binding = ActivityGnpageContainerBinding.inflate(getLayoutInflater());
        //Avoid Status-bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());

        //Start up view
        replaceFragment(new OperatorHomeFragment());

        appProgressDialog = AppProgressDialog.createProgressDialog(OperatorPageContainerActivity.this);
        getMyDivisionId();

        //Avoid changing of selected icon's color
        binding.bottomNavigationView.setItemIconTintList(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.home:
                    replaceFragment(new OperatorHomeFragment());
                    break;
                case R.id.users:
                    if(!myDivisionId.equals(""))
                        replaceFragment(new AddUsersFragment(true, myDivisionId));
                    break;
                case R.id.fault:
                    replaceFragment(new AddFaultFragment());
                    break;
                case R.id.profile:
                    replaceFragment(new MyProfileFragment());
                    break;
            }
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        //TODO: Avoid Go to previouse activity - > comment -> super.onBackPressed();
        //super.onBackPressed();
        switch (Navigation.currentScreen){
            case "AddDivisionFragment":
            case "AddUsersFragment":
            case "MyProfileFragment":
            case "AddFaultFragment":
                replaceFragment(new OperatorHomeFragment());
                break;
            case "ViewFaultFragment":
                replaceFragment(new AddFaultFragment());
                break;
            case "ViewUserFragment":
                replaceFragment(new AddUsersFragment("Rfid_tag"));
                break;
            case "ViewDivisionFragment":
                replaceFragment(new AddDivisionFragment());
                break;
            case "AdminHomeFragment":
                // super.onBackPressed();
                break;
        }
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

                        myDivisionId = responseObj.getString("divisionId");
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


    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }


    @Override
    public void onFileSelected(FileDialog dialog, File file) {
        //   System.out.println("=========== >> " + file.getName());
        AddUsersFragment.exposrtToExcel(file.getAbsolutePath());
    }

}