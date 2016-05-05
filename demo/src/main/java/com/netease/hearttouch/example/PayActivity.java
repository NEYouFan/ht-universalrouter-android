package com.netease.hearttouch.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.netease.hearttouch.router.HTRouter;

@HTRouter(url = {"http://www.kaola.com/pay"}, entryAnim = R.anim.enter, exitAnim = R.anim.exit)
public class PayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        Button button = (Button) findViewById(R.id.id_close_pay_page);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
