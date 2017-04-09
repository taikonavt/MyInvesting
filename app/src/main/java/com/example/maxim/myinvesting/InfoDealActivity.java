package com.example.maxim.myinvesting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by maxim on 09.04.17.
 */

public class InfoDealActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info_deal);
    }

    public void onClick(View view) {

        Intent intent = new Intent(this, AddDealActivity.class);
        startActivity(intent);
    }
}
