
package com.netease.hearttouch.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.netease.hearttouch.router.HTLogUtil;
import com.netease.hearttouch.router.HTRouterEntry;
import com.netease.hearttouch.router.HTRouterManager;

public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String url = intent.getStringExtra("customUrlKey");
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("HT", "重新加载=" + url);
                HTRouterEntry entity = HTRouterManager.findRouterEntryByUrl(url);
                //为了防止匹配不上后循环跳，这里需要有个判断
                if (entity != null) {
                    HTRouterManager.startActivity(WebActivity.this, url, null, false);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        String targetUrl = HTRouterManager.getHttpSchemaHostAndPath(url);
        HTLogUtil.d("打开链接=" + targetUrl);
        webView.loadUrl(targetUrl);
        setContentView(webView);
    }
}
