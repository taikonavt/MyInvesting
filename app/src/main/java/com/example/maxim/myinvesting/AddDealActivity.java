package com.example.maxim.myinvesting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by maxim on 27.03.17.
 */

public class AddDealActivity extends AppCompatActivity {

    String TAG = "MyLog";

    String ticker;
    String type;
    int year;
    int month;
    int day;
    int price;
    int volume;
    int fee;

    EditText eTTicker;
    EditText eTType;
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

    public void onClick(View view) {

        try {
            ticker = eTTicker.getText().toString();
            if (ticker.length() == 0)
                throw new UnsupportedOperationException("Ticker не задан");

            type = eTType.getText().toString();
            if (type.length() == 0)
                throw new UnsupportedOperationException("Type не задан");
            if ( ! (type.equals("S") || type.equals("B") || type.equals("D")))
                throw new UnsupportedOperationException("Type не определен");

            year = Integer.valueOf(eTYear.getText().toString());
            month = Integer.valueOf(eTMonth.getText().toString());
            day = Integer.valueOf(eTDay.getText().toString());

            String strPrice = eTPrice.getText().toString();
            Float floatPrice = Float.valueOf(strPrice);
            //цену акции умножаю на 100000 чтобы уйти от запятой, т.е. 1 руб = 100000 ед.
            price = (int) (floatPrice * 100000);
            if (price != floatPrice)
                throw new UnsupportedOperationException("Разряд числа не поддерживается");

            volume = Integer.valueOf(eTVolume.getText().toString());

            String strFee = eTPrice.getText().toString();
            Float floatFee = Float.valueOf(strFee);
            //коммисию умножаю на 100000 чтобы уйти от запятой, т.е. 1 руб = 100000 ед.
            fee = (int) (floatFee * 100000);
            if (price != floatFee)
                throw new UnsupportedOperationException("Разряд числа не поддерживается");
            fee = Integer.valueOf(eTFee.getText().toString());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Дата указана не верно", Toast.LENGTH_LONG).show();
            Log.d(TAG, e.toString());
            e.printStackTrace();

            return;
        }
        catch (Exception e) {

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(TAG, e.toString());
            e.printStackTrace();

            return;
        }

    }
}
