package com.sj.gfodapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.WindowManager;

import com.rustamg.filedialogs.FileDialog;
import com.sj.gfodapp.R;

import com.sj.gfodapp.databinding.ActivityAdminPageContainerBinding;
import com.sj.gfodapp.fragment.AddFaultFragment;
import com.sj.gfodapp.fragment.AddDivisionFragment;
import com.sj.gfodapp.fragment.AddUsersFragment;
import com.sj.gfodapp.fragment.AdminHomeFragment;
import com.sj.gfodapp.fragment.MyProfileFragment;
import com.sj.gfodapp.utils.Navigation;

import java.io.File;

public class AdminPageContainerActivity extends AppCompatActivity implements FileDialog.OnFileSelectedListener {

    ActivityAdminPageContainerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO:::::::::::::::::::::::IMPORTANT::::::::::::::::::::::
        /**
         * Make sure enable viewBingg at build.gradle file
         *     buildFeatures{
         *         viewBinding true
         *     }
         */
        binding = ActivityAdminPageContainerBinding.inflate(getLayoutInflater());
        //Avoid Status-bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());

        //Start up view
        replaceFragment(new AdminHomeFragment());

        //Avoid changing of selected icon's color
        binding.bottomNavigationView.setItemIconTintList(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    replaceFragment(new AdminHomeFragment());
                    break;
                case R.id.users:
                    replaceFragment(new AddUsersFragment("Operator"));
                    break;
                case R.id.divisions:
                    replaceFragment(new AddDivisionFragment());
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
        switch (Navigation.currentScreen) {
            case "AddDivisionFragment":
            case "AddUsersFragment":
            case "MyProfileFragment":
            case "AddFaultFragment":
                replaceFragment(new AdminHomeFragment());
                break;
            case "ViewUserFragment":
                replaceFragment(new AddUsersFragment("Rfid_tag"));
                break;
            case "ViewFaultFragment":
                replaceFragment(new AddFaultFragment());
                break;
            case "ViewDivisionFragment":
                replaceFragment(new AddDivisionFragment());
                break;
            case "AdminHomeFragment":
                // super.onBackPressed();
                break;
        }
    }

    private void replaceFragment(Fragment fragment) {
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