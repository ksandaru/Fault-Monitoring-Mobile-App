package com.sj.gfodapp.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.sj.gfodapp.R;


import com.sj.gfodapp.utils.Navigation;



public class WebViewFragment extends Fragment {
    WebView webView;
    SharedPreferences sharedPre;


    public WebViewFragment() {
        // Required empty public constructor
        Navigation.currentScreen = "WebViewFragment";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.activity_map, container, false);
        webView = (WebView) view.findViewById(R.id.mywebView);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.google.lk/");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

}