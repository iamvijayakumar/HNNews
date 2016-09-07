package com.hnnews.news;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class DetailActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;
    private String url = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            url = bundle.getString("url","");
        }


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int progress) {
                // update progressbar

            }
        });
        webView.setWebViewClient(new WebClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

    public class WebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // return super.shouldOverrideUrlLoading(view, url);
            progressBar.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            view.loadUrl(url);
            return false;
        }

    }
}
