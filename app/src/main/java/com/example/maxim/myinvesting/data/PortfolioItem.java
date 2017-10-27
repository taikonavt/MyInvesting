package com.example.maxim.myinvesting.data;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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

        if (priceIsReady) {

            setPriceAndCostMethod();
        }
        else {

            SetPriceAndCostTask setPriceAndCostTask = new SetPriceAndCostTask();
            setPriceAndCostTask.execute();
        }
    }

    private void setPriceAndCostMethod() {

        priceTV.setText(String.valueOf((float) price / MULTIPLIER_FOR_MONEY));
        costTV.setText(String.valueOf((float) volume * price / MULTIPLIER_FOR_MONEY));

        pricePB.setVisibility(View.GONE);
        costPB.setVisibility(View.GONE);

        priceTV.setVisibility(View.VISIBLE);
        costTV.setVisibility(View.VISIBLE);
    }

    private class SetPriceAndCostTask extends AsyncTask<Void, Void, Void> {

        final int TIME_TO_SLEEP = 200;
        final int TWELVE_SECOND = 60;

        @Override
        protected Void doInBackground(Void... params) {

            try {

                int i = 0;

                while (!priceTVAndCostTVAreGot) {

                    Thread.sleep(TIME_TO_SLEEP);
                    i++;

                    if (i == TWELVE_SECOND) {
                        return null;
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (priceTV != null && costTV != null &&
                    pricePB != null && costPB != null) {

                setPriceAndCostMethod();
            }
        }
    }


    private class SetNameTask extends AsyncTask<Void, Void, Void> {

        private final int TEN_SECOND = 50;
        final int TIME_TO_SLEEP = 200;
        private boolean noNetwork = false;

        @Override
        protected Void doInBackground(Void... params) {

            int i = 0;

            try {
                while (!nameIsGot) {

Log.d(TAG, SetNameTask.class.getSimpleName() + " " + i);

                    Thread.sleep(TIME_TO_SLEEP);
                    i++;

                    if (i == TEN_SECOND) {

                        noNetwork = true;
                        return null;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!noNetwork) {
                nameTV.setText(name);
            }
        }
    }
}
