package com.sharemyfood.main;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iamhabib.easy_preference.EasyPreference;
import com.sharemyfood.R;
import com.sharemyfood.base.BaseActivity;

import java.io.InputStream;

public class HelpActivity extends BaseActivity {

    /*@BindView(R.id.web)
    WebView webView;*/
   /* @BindView(R.id.progressBar)
    ProgressBar pbar;*/
    @BindView(R.id.tv_terms)
    TextView tvterms;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ButterKnife.bind(this);


        String result;
        try {

            Resources res = getResources();
            InputStream in_s = res.openRawResource(R.raw.terms);

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            result = new String(b);

        } catch (Exception e) {

            // e.printStackTrace();
            result = "Sorry, Now editing..";

        }

        tvterms.setText(result);
/*
        String Url = EasyPreference.with(this).getString("easyHelpLink", "");

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);     // JavaScript Event Enable
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(Url);*/

    }

    @OnClick(R.id.img_back)
    void onBack(){
        finish();
    }


    /******************* WebView Client Medel **********************/
    /*public class WebViewClient extends android.webkit.WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
            pbar.setVisibility(View.GONE);

        }

    }*/
}
