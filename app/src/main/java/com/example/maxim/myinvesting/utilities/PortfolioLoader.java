package com.example.maxim.myinvesting.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.maxim.myinvesting.MainActivity;
import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.data.PortfolioItem;

import static com.example.maxim.myinvesting.data.Const.TAG;

import java.util.ArrayList;

/**
 * Created by maxim on 09.08.17.
 */

public class PortfolioLoader extends AsyncTaskLoader<ArrayList<PortfolioItem>> {

    private Context mContext;

    public PortfolioLoader(Context context) {
        super(context);

        mContext = context;
    }

    @Override
    public ArrayList<PortfolioItem> loadInBackground() {

        Uri uri = Contract.PortfolioEntry.CONTENT_URI.buildUpon()
                .appendPath(
                        Contract.DealsEntry.COLUMN_TICKER)
                .build();

        // SELECT ticker
        String [] projection = {
                Contract.DealsEntry.COLUMN_TICKER
        };

        // WHERE portfolio = '5838194'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = "
                + ((MainActivity) mContext).getNameOfPortfolio();

        // ORDER BY ticker ASC
        String orderBy = Contract.DealsEntry.COLUMN_TICKER + " ASC";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                orderBy);

        ArrayList<PortfolioItem> arrayList;

        if (cursor.moveToFirst()) {

            int numOfRows = cursor.getCount();

            arrayList = new ArrayList<>(numOfRows);

            int tickerIndex = cursor.getColumnIndex(Contract.DealsEntry.COLUMN_TICKER);

            int i = 0;

            do {

                String ticker = cursor.getString(tickerIndex);

                int volume = getVolume(ticker);

                int price = getPrice(ticker);
// TODO: 14.08.17 Проверить работоспособность
                PortfolioItem portfolioItem = new PortfolioItem(i, ticker, volume, price);
                arrayList.add(portfolioItem);

                i++;

            } while (cursor.moveToNext());
        } else throw
                new NullPointerException("PortfolioLoader.loadInBackground(): cursor have 0 rows");

        cursor.close();

        return arrayList;
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

    private int getPrice(String lTicker) {

        return NetworkUtils.getCurrentPrice(lTicker);
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
    public void onCanceled(ArrayList<PortfolioItem> data) {
        super.onCanceled(data);
        onReleaseResources();
    }

    protected void onReleaseResources() {

    }
}