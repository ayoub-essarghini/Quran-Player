package com.items.mp3player.ui;

import static com.items.mp3player.Constants.*;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

;
import androidx.appcompat.app.AppCompatActivity;


import com.items.mp3player.R;

public class OnlinePlayer extends AppCompatActivity {


    private WebView webView;

    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_player);

        webView = findViewById(R.id.web_view);
        backBtn = findViewById(R.id.back_btn);

        backBtn.setOnClickListener(v->{
            onBackPressed();
        });

        // Enable JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false); // Disable default zoom controls



        // Handle URL redirects within the WebView (prevent opening in browser)
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        // Set a WebChromeClient for better support with JavaScript dialogs (optional)
        webView.setWebChromeClient(new WebChromeClient());

        // Load a URL
        webView.loadUrl(WEB_URL);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);



    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.clearCache(true);
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
        }
        super.onDestroy();


    }
}
