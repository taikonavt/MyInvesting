package com.example.maxim.myinvesting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by maxim on 09.04.17.
 */

public class InfoDealActivity extends AppCompatActivity {

    private InfoDealAdapter mAdapter;
    private RecyclerView mRecyclerView;

    // todo change number into real when it become clear
    private static final int numOfItems = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_deal);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_info_deals);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        //mRecyclerView.setHasFixedSize(true);

        mAdapter = new InfoDealAdapter(numOfItems);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void onClick(View view) {

        Intent intent = new Intent(this, AddDealActivity.class);
        startActivity(intent);
    }
}
