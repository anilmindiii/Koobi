package com.mualab.org.user.activity.authentication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.dialogs.Progress;

public class WebViewActivity extends AppCompatActivity {
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.term_amp_policies));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        webView =  findViewById(R.id.webview);
        Progress.show(WebViewActivity.this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Progress.show(WebViewActivity.this);
               // loadingView.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                Progress.hide(WebViewActivity.this);
                super.onPageCommitVisible(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                Progress.hide(WebViewActivity.this);
                super.onPageFinished(view, url);
                //loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Progress.hide(WebViewActivity.this);
                super.onReceivedError(view, request, error);
            }
        });

        webView.loadUrl("http://koobi.co.uk/");

    }
}
