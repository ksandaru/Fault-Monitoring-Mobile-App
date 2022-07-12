package com.sj.gfodapp.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.ViewGroup;

import com.sj.gfodapp.R;

public class AppProgressDialog {

    public static Dialog createProgressDialog(Context context){
          Dialog appCustomDialog = new Dialog(context);
          // >> TODO: Add Dialog Layout
          appCustomDialog.setContentView(R.layout.progress_dialog);
          if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
              appCustomDialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.alert_background));
          }
          appCustomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
          appCustomDialog.setCancelable(false);
          appCustomDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
          return  appCustomDialog;
    }

}
