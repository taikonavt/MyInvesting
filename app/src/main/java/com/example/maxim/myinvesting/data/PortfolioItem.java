package com.example.maxim.myinvesting.data;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;
import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 09.08.17.
 */

public class PortfolioItem {

    private int id = 0;
    private String ticker = ""; // Ввожу руками
    private String name = ""; // Получить из интернета по тикеру
    private int volume = 0; // Высчитываю из базы данных
    private int lotSize = 0;

    private int price = 0; // Получить из интернета и умножить на константу
    boolean priceIsReady = false;

    private TextView costTV = null;
    private TextView priceTV = null;
    private ProgressBar costPB;
    private ProgressBar pricePB;
    private boolean priceTVAndCostTVAreGot = false;

    private TextView nameTV;
    private boolean nameIsGot = false;

    public PortfolioItem(int lID, String lTicker, int lVolume) {

        id = lID;
        ticker = lTicker;
        volume = lVolume;
    }

    public void setName(String name) {
        this.name = name;
        nameIsGot = true;
    }

    void setLotSize(int lotSize) {
        this.lotSize = lotSize;
    }

    void setPrice(int price) {
        this.price = price;

        priceIsReady = true;

        SetPriceAndCostTask setPriceAndCostTask = new SetPriceAndCostTask();
        setPriceAndCostTask.execute();
    }

    public String getTicker() {
        return ticker;
    }

    public int getId() {
        return id;
    }

    public int getVolume() {
        return volume;
    }

    public void getName(TextView textView) {

        nameTV = textView;

        SetNameTask setNameTask = new SetNameTask();
        setNameTask.execute();
    }

    public long getCost() {
        return price * volume;
    }

    public void getPriceAndCost(TextView priceTV, TextView costTV,
                                ProgressBar pricePB, ProgressBar costPB) {

        this.priceTV = priceTV;
        this.costTV = costTV;
        this.pricePB = pricePB;
        this.costPB = costPB;

        priceTVAndCostTVAreGot = true;
    }

    class SetPriceAndCostTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                if (!priceTVAndCostTVAreGot) {
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            priceTV.setText(String.valueOf((float) price/MULTIPLIER_FOR_MONEY));
            costTV.setText(String.valueOf((float) volume*price/MULTIPLIER_FOR_MONEY));

            pricePB.setVisibility(View.GONE);
            costPB.setVisibility(View.GONE);

            priceTV.setVisibility(View.VISIBLE);
            costTV.setVisibility(View.VISIBLE);
        }
    }


    class SetNameTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {

            try {
                if (!nameIsGot) {
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            nameTV.setText(name);
        }
    }
}
