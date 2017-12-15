package com.example.maxim.myinvesting.data;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

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

    private long price = 0; // Получить из интернета и умножить на константу
    boolean priceIsReady = false;

    private WeakReference<TextView> costTV;
    private WeakReference<TextView> priceTV;
    private WeakReference<ProgressBar> costPB;
    private WeakReference<ProgressBar> pricePB;
    private boolean priceTVAndCostTVAreGot = false;

    private WeakReference<TextView> nameTV;
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

    void setPrice(long price) {
        this.price = price;

        priceIsReady = true;
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

        nameTV = new WeakReference<TextView>(textView);

        SetNameTask setNameTask = new SetNameTask();
        setNameTask.execute();
    }

    long getCost() {
        return price * volume;
    }

    public void getPriceAndCost(TextView priceTV, TextView costTV,
                                ProgressBar pricePB, ProgressBar costPB) {

        this.priceTV = new WeakReference<TextView>(priceTV);
        this.costTV = new WeakReference<>(costTV);
        this.pricePB = new WeakReference<ProgressBar>(pricePB);
        this.costPB = new WeakReference<ProgressBar>(costPB);

        priceTVAndCostTVAreGot = true;

        if (priceIsReady) {

            try {
                setPriceAndCostMethod();
            }
            catch (NullPointerException e) {

                e.printStackTrace();
            }
        }
        else {

            SetPriceAndCostTask setPriceAndCostTask = new SetPriceAndCostTask();
            setPriceAndCostTask.execute();
        }
    }

    private void setPriceAndCostMethod() throws NullPointerException {

        priceTV.get().setText(String.valueOf((float) price / MULTIPLIER_FOR_MONEY));
        costTV.get().setText(String.valueOf((float) volume * price / MULTIPLIER_FOR_MONEY));

        pricePB.get().setVisibility(View.GONE);
        costPB.get().setVisibility(View.GONE);

        priceTV.get().setVisibility(View.VISIBLE);
        costTV.get().setVisibility(View.VISIBLE);
    }

    private class SetPriceAndCostTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                setPriceAndCostMethod();
            }
            catch (NullPointerException e) {

                e.printStackTrace();
            }
        }
    }


    private class SetNameTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                nameTV.get().setText(name);
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
