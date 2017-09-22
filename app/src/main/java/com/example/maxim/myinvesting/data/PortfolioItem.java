package com.example.maxim.myinvesting.data;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
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

    private TextView costTV;
    private TextView priceTV;
    private boolean priceTVAndCostTVAreGot = false;

    public PortfolioItem(int lID, String lTicker, int lVolume) {

        id = lID;
        ticker = lTicker;
        volume = lVolume;
    }

//    public PortfolioItem (int lID, String lTicker, int lVolume, int lPrice) {
//
//        id = lID;
//        ticker = lTicker;
//        volume = lVolume;
//
//        price = lPrice;
//
//        priceIsReady = true;
//    }

    public void setName(String name) {
        this.name = name;
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

    public long getCost() {
        return price * volume;
    }

    public void getPriceAndCost(TextView priceTV, TextView costTV) {

        this.priceTV = priceTV;
        this.costTV = costTV;

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
        }
    }
}
