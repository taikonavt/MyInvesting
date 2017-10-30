package com.example.maxim.myinvesting.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.Toast;

import com.example.maxim.myinvesting.MainActivity;
import com.example.maxim.myinvesting.R;
import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.data.PortfolioData;
import com.example.maxim.myinvesting.data.PortfolioItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.maxim.myinvesting.data.Const.MILLIS_IN_DAY;
import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;
import static com.example.maxim.myinvesting.data.Const.TAG;
import static java.lang.Math.pow;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

/**
 * Created by maxim on 09.08.17.
 */

public class PortfolioLoader extends AsyncTaskLoader<PortfolioData> {

    private Context mContext;

    public PortfolioLoader(Context context) {
        super(context);

        mContext = context;
    }

    @Override
    public PortfolioData loadInBackground() {

        PortfolioData portfolioData = new PortfolioData(
                        mContext, Calendar.getInstance().getTimeInMillis());

        setPortfolioList(portfolioData);

        return portfolioData;
    }

    @Override
    protected void onStartLoading() {

        super.onStartLoading();
        forceLoad();
    }

    @Override
    protected void onStopLoading() {

        cancelLoad();
    }

    @Override
    public void onCanceled(PortfolioData data) {

        super.onCanceled(data);
        onReleaseResources();
    }

    @Override
    protected void onReset() {

        super.onReset();

        onStopLoading();
    }

    private void onReleaseResources() {
    }

    // Устанавливает в portfolioData массив с акциями портфеля и стоимость всего портфеля
    private void setPortfolioList(PortfolioData data) {

        Uri uri = Contract.PortfolioEntry.CONTENT_URI.buildUpon()
                .appendPath(
                        Contract.DealsEntry.COLUMN_TICKER)
                .build();

        // SELECT ticker
        String [] projection = {
                Contract.DealsEntry.COLUMN_TICKER
        };

        // WHERE portfolio = '5838194'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '"
                + ((MainActivity) mContext).getNameOfPortfolio() + "'";

        // ORDER BY ticker ASC
        String orderBy = Contract.DealsEntry.COLUMN_TICKER + " ASC";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                orderBy);

        ArrayList<PortfolioItem> arrayList = new ArrayList<>();

        int i = 0; // id для строк массива

        if (cursor.moveToFirst()) {

            int tickerIndex = cursor.getColumnIndex(Contract.DealsEntry.COLUMN_TICKER);

            do {

                String ticker = cursor.getString(tickerIndex);

                int volume = getVolume(ticker);

                PortfolioItem portfolioItem = new PortfolioItem(i, ticker, volume);
                arrayList.add(portfolioItem);

                i++;

            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "Portfolio have 0 items");
        }

        cursor.close();

        data.setPortfolioItems(arrayList);
    }

    // Получение количества акций Ticker в портфеле
    private int getVolume(String lTicker) {

        int volumeBuy = 0;
        int volumeSell = 0;
        int volumeResult;

            Uri uri = Contract.PortfolioEntry.CONTENT_URI.buildUpon()
                    .appendPath(
                            Contract.DealsEntry.COLUMN_TICKER) // GROUP BY ticker
                    .build();

            // SELECT ticker
            String[] projection = {
                    Contract.DealsEntry.COLUMN_TICKER,
                    "sum (" + Contract.DealsEntry.COLUMN_VOLUME
                            + ") AS '" + Contract.DealsEntry.COLUMN_VOLUME + "'"
            };

            // WHERE portfolio = '5838194' for cursorBuy
            String selectionBuy = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                    ((MainActivity) mContext).getNameOfPortfolio() + "' AND " +
                    Contract.DealsEntry.COLUMN_TICKER + " = '" + lTicker + "' AND " +
                    Contract.DealsEntry.COLUMN_TYPE + " = 'Buy'";

            Cursor cursorBuy = getContext().getContentResolver().query(
                    uri,
                    projection,
                    selectionBuy,
                    null,
                    null);

            if (cursorBuy.moveToFirst()) {

                int volumeBuyIndex = cursorBuy.getColumnIndex(Contract.DealsEntry.COLUMN_VOLUME);
                volumeBuy = cursorBuy.getInt(volumeBuyIndex);
            }

            cursorBuy.close();

            // WHERE portfolio = '5838194' for cursorSell
            String selectionSell = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                    ((MainActivity) mContext).getNameOfPortfolio() + "' AND " +
                    Contract.DealsEntry.COLUMN_TICKER + " = '" + lTicker + "' AND " +
                    Contract.DealsEntry.COLUMN_TYPE + " = 'Sell'";

            Cursor cursorSell = getContext().getContentResolver().query(
                    uri,
                    projection,
                    selectionSell,
                    null,
                    null);

            if (cursorSell.moveToFirst()) {

                int volumeSellIndex = cursorSell.getColumnIndex(Contract.DealsEntry.COLUMN_VOLUME);
                volumeSell = cursorSell.getInt(volumeSellIndex);
            }

            cursorSell.close();

            volumeResult = volumeBuy - volumeSell;

        return volumeResult;
    }
}