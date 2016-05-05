package com.netease.hearttouch.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.netease.hearttouch.router.HTRouter;
import com.netease.hearttouch.router.HTRouterManager;

@HTRouter(url = {"kaola://www.kaola.com/","kaola://m.kaola.com"},entryAnim = R.anim.enter,exitAnim = R.anim.exit)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button button = new Button(this);
        button.setText("跳转到考拉商品类别H5页面");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTRouterManager.startActivity(MainActivity.this, "http://www.kaola.com/activity/detail/6932.shtml?navindex=7", null, true);
            }
        });
        setContentView(button);
    }

}
