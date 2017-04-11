package com.example.maxim.myinvesting;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    final static int MULTIPLIER_FOR_MONEY = 10000;
    public static final String TAG = "MyLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClick(View view) {

        Intent intent = new Intent(this, InfoDealActivity.class);
        startActivity(intent);
    }
}
