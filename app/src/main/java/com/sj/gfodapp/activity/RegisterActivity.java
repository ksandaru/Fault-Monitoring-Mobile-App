package com.sj.gfodapp.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import com.sj.gfodapp.R;
import com.sj.gfodapp.api.response.ErrorResponse;
import com.sj.gfodapp.utils.ApiInterface;
import com.sj.gfodapp.utils.AppProgressDialog;
import com.sj.gfodapp.utils.FileUtils;
import com.sj.gfodapp.utils.RetrofitClient;

public class RegisterActivity extends AppCompatActivity {

    private String apiBaseUrl = "";
    private Dialog appProgressDialog;

    CharSequence[] options = {"Camera", "Gallery", "Cancel"};
    public ActivityResultLauncher<Intent> activityResultLaunch;
    public String selectedImage = "";
    public String fileDialogSelectedOption = "";

    private TextInputEditText txtNic, txtEmail,
            txt_fullName,
            txt_district,
            txt_city,
            txt_password,
            txt_confirmPassword;
    private TextView tvSelectImage;
    private Button btnRegister;
    private CircleImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Avoid Status-bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        apiBaseUrl = getResources().getString(R.string.apiBaseUrl);
        appProgressDialog = AppProgressDialog.createProgressDialog(RegisterActivity.this);

        btnRegister = (Button) findViewById(R.id.btnRegister);

        txtNic = (TextInputEditText) findViewById(R.id.txtNic);
        txtEmail = (TextInputEditText) findViewById(R.id.txtEmail);
        txt_fullName = (TextInputEditText) findViewById(R.id.txt_fullName);
        txt_district = (TextInputEditText) findViewById(R.id.txt_district);
        txt_city = (TextInputEditText) findViewById(R.id.txt_city);
        txt_password = (TextInputEditText) findViewById(R.id.txt_password);
        txt_confirmPassword = (TextInputEditText) findViewById(R.id.txt_confirmPassword);
        userImage = (CircleImageView) findViewById(R.id.imageDisaster);
        tvSelectImage = (TextView) findViewById(R.id.tvSelectImage);

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectDialog();
            }
        });

        tvSelectImage.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectDialog();
            }
        }));

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String NIC = txtNic.getText().toString().trim();
                String email = txtEmail.getText().toString().trim();
                String fullName = txt_fullName.getText().toString().trim();
                String district = txt_district.getText().toString().trim();
                String city = txt_city.getText().toString().trim();
                String password = txt_password.getText().toString().trim();
                String confirmPassword = txt_confirmPassword.getText().toString().trim();
                if (!NIC.equals("") &&
                        !email.equals("") &&
                        !fullName.equals("") &&
                        !district.equals("") &&
                        !city.equals("") &&
                        !password.equals("") &&
                        !confirmPassword.equals("")) {
                    if (!password.equals(confirmPassword)) {
                        showDialog(RegisterActivity.this, "Opps..!", "Password doesn't match", () -> {
                        });
                    } else {
                        register(selectedImage, NIC, email, fullName, district, city,confirmPassword);
                    }
                } else {
                    showDialog(RegisterActivity.this, "Opps..!", "All fields are required", () -> {
                    });
                }
            }
        });

        requirePermission();

        activityResultLaunch = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == -1) {
                            switch (fileDialogSelectedOption) {
                                case "Camera":
                                    //TODO : do stuff when image captured by camera
                                    Bitmap image = (Bitmap) result.getData().getExtras().get("data");
                                    selectedImage = FileUtils.getPath(RegisterActivity.this, getImageUri(RegisterActivity.this, image));
                                    userImage.setImageBitmap(image);
                                    break;
                                case "Gallery":
                                    // TODO : do stuff when image Selected from gallery
                                    Uri imageUri = result.getData().getData();
                                    selectedImage = FileUtils.getPath(RegisterActivity.this, imageUri);
                                    userImage.setImageURI(imageUri);
                                    //Picasso.get().load(image).into(imageView); // <-- Show image in ImageView
                                    break;
                            }
                        } else if (result.getResultCode() == 0) { // Not selected
                            // TODO : do stuff when image not Selected
                        }
                    }
                });
    }

    private void openSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
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
                }
            }
        });

        builder.show();
    }

    private void register(String imagePath,  String NIC, String email, String fullName,  String district, String city, String password) {
        appProgressDialog.show();

        MultipartBody.Part filePart = null;
        if(!imagePath.equals("")){
            File file = new File(Uri.parse(imagePath).getPath());
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            filePart = MultipartBody.Part.createFormData("avatar", file.getName(), requestBody);
            // System.out.println("getTotalSpace " + file.getTotalSpace() + "\n" + file.getName());
            System.out.println("=========== Image selected ========");
        }else {
            //Default image fill set to this profile
            System.out.println("=========== Image Not selected ========");
        }

        RequestBody _nic = RequestBody.create(MediaType.parse("multipart/form-data"), NIC);
        RequestBody _email = RequestBody.create(MediaType.parse("multipart/form-data"), email);
        RequestBody _password = RequestBody.create(MediaType.parse("multipart/form-data"), password);
        RequestBody _fullName = RequestBody.create(MediaType.parse("multipart/form-data"), fullName);
        RequestBody _district = RequestBody.create(MediaType.parse("multipart/form-data"), district);
//        RequestBody _city = RequestBody.create(MediaType.parse("multipart/form-data"), city);


        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance(apiBaseUrl).create(ApiInterface.class);

        Call<ResponseBody> call = apiInterface.userRegister(filePart, _nic, _email, _password,_fullName, _district);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                if (response.isSuccessful()) { //Response code : 200
                    showDialog(RegisterActivity.this, "Congratulations..!", "Successfully Registered!", () -> {
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    });

                } else { //Response code : 400 response.code()
                    try {
                        ErrorResponse errResponse = null;
                        errResponse = new Gson().fromJson(response.errorBody().string(), ErrorResponse.class);
                        System.out.println("=========================ERROR========================= \n" + response.errorBody().string());
                        showDialog(RegisterActivity.this, "Opps..!", errResponse.getError(), () -> {
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                appProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                appProgressDialog.dismiss();
                showDialog(RegisterActivity.this, "Opps..!", "Could not connect", () -> {
                });
                System.out.println("_==================Error! Could not Access my API  ==================");
            }
        });
    }


    public Uri getImageUri(Context context, Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "myImage", "");
        return Uri.parse(path);
    }

    public void requirePermission() {
        ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    public static void showDialog(
            @NonNull final Context context,
            String heading,
            String message,
            @Nullable Runnable confirmCallback
    ) {
        //TODO: Add TextInput Programatically...
        //  E.g. TextInputEditText myInput = new TextInputEditText(getContext());
        //  MaterialAlertDialogBuilder(context).addView(myInput)  <- Possible
        new MaterialAlertDialogBuilder(context)
                .setTitle(heading)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok",
                        (dialog2, which) -> {
                            dialog2.dismiss();
                            if (confirmCallback != null) confirmCallback.run();
                        })
                .show();
    }
}