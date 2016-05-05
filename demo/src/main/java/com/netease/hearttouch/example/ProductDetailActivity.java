
package com.netease.hearttouch.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.hearttouch.router.HTRouter;
import com.netease.hearttouch.router.HTRouterEntry;
import com.netease.hearttouch.router.HTRouterManager;

import java.util.HashMap;

@HTRouter(url = {"http://www.kaola.com/activity/detail/{id}.shtml","http://m.kaola.com/activity/detail/{id}.shtml","http://m.kaola.com/product/{id}.html","http://www.kaola.com/product/{id}.html"},entryAnim = R.anim.enter,exitAnim = R.anim.exit)
public class ProductDetailActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tv;
    private Button btn;
    private Button btn1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);


        tv = (TextView) findViewById(R.id.tv);
        btn= (Button) findViewById(R.id.btn);
        btn.setOnClickListener(this);
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(this);
        WebView webView = (WebView) findViewById(R.id.webview);

        //获取URL参数
        Intent intent = getIntent();
        HashMap<String, String> urlParamsMap = (HashMap<String, String>)intent.getSerializableExtra(HTRouterManager.HT_URL_PARAMS_KEY);
        for(String key : urlParamsMap.keySet()){
            Log.d("url_params", key + ":" + urlParamsMap.get(key));
        }

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
                    HTRouterManager.startActivity(ProductDetailActivity.this, url, null, false);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        if (intent!=null){
            Log.i("HT", "产品详情=" + intent.getData());
            tv.setText("产品详情=" + intent.getData());
            HTRouterEntry entity = HTRouterManager.findRouterEntryByUrl(intent.getData().toString());
            webView.loadUrl(intent.getData().toString());
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn:
                Intent sourceIntent = new Intent();
                sourceIntent.putExtra("price",12345);
                sourceIntent.putExtra("id","m123456");
                sourceIntent.putExtra("name","Mac Pro 13寸");
                HTRouterManager.startActivityForResult(ProductDetailActivity.this,"http://www.kaola.com/pay",sourceIntent,false,1001);
                break;
            case R.id.btn1:
                Intent sourceIntent1 = new Intent();
                sourceIntent1.putExtra("price",12345);
                sourceIntent1.putExtra("id","m123456");
                sourceIntent1.putExtra("name","Mac Pro 13寸");
                HTRouterManager.startActivityForResult(ProductDetailActivity.this,
                        "http://www.kaola.com/mall/paygame.html",
                        sourceIntent1, false, 1001);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1001:
                Toast.makeText(ProductDetailActivity.this,"支付返回",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
