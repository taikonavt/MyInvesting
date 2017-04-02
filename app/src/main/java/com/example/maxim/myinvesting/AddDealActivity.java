package com.example.maxim.myinvesting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by maxim on 27.03.17.
 */

public class AddDealActivity extends AppCompatActivity {

    String ticker;
    String type;
    String date;
    String price;
    String volume;
    String fee;

    EditText eTTicker;
    EditText eTType;
    EditText eTDate;
    EditText eTYear;
    EditText eTMonth;
    EditText eTDay;
    EditText eTPrice;
    EditText eTVolume;
    EditText eTFee;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_deal);

        eTTicker = (EditText) findViewById(R.id.eTTicker);
        eTType = (EditText) findViewById(R.id.eTType);
        eTYear = (EditText) findViewById(R.id.eTYear);
        eTMonth = (EditText) findViewById(R.id.eTMonth);
        eTDay = (EditText) findViewById(R.id.eTDay);
        eTPrice = (EditText) findViewById(R.id.eTPrice);
        eTVolume = (EditText) findViewById(R.id.eTVolume);
        eTFee = (EditText) findViewById(R.id.eTFee);
    }

    private void onClick(View view) {


    }





}
