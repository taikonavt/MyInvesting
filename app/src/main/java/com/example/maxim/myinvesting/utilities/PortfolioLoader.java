package com.example.maxim.myinvesting.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.Toast;

import com.example.maxim.myinvesting.MainActivity;
import com.example.maxim.myinvesting.R;
import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.data.PortfolioItem;

import static com.example.maxim.myinvesting.data.Const.TAG;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

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

        ArrayList<PortfolioItem> arrayList = null;

        if (cursor.moveToFirst()) {

            int numOfRows = cursor.getCount();

            arrayList = new ArrayList<>(numOfRows);

            int tickerIndex = cursor.getColumnIndex(Contract.DealsEntry.COLUMN_TICKER);

            int i = 0;

            do {

                String ticker = cursor.getString(tickerIndex);

                int volume = getVolume(ticker);

                int price = getPrice(ticker);

                getInputs(((MainActivity) mContext).getNameOfPortfolio(),
                        Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).
                                getTimeInMillis());

                getBuys(((MainActivity) mContext).getNameOfPortfolio(),
                        Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).
                                getTimeInMillis());

                getSells(((MainActivity) mContext).getNameOfPortfolio(),
                        Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).
                                getTimeInMillis());

                PortfolioItem portfolioItem = new PortfolioItem(i, ticker, volume, price);
                arrayList.add(portfolioItem);

                i++;

            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "Portfolio have 0 items");
//            Toast.makeText(mContext, "Portfolio have 0 items", Toast.LENGTH_LONG).show();
        }

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

    // получение суммы всех вводов
    private void getInputs(String lPortfolio, long lDate) {

        int amountInput = 0;

        String [] strings = mContext.getResources().getStringArray(R.array.spinType_input_array);

        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_INPUT)
                .appendPath(Contract.PATH_SUM)
                .build();

        // SELECT portfolio, sum(amount) AS 'amount'
        String[] projection = {
                Contract.InputEntry.COLUMN_PORTFOLIO,
                "sum (" + Contract.InputEntry.COLUMN_AMOUNT + ") AS '" +
                        Contract.InputEntry.COLUMN_AMOUNT + "'"
        };

        // WHERE portfolio = '5838199' AND date < 123 AND type = 'Input'
        String selection = Contract.InputEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.InputEntry.COLUMN_DATE + " < " + lDate + " AND " +
                Contract.InputEntry.COLUMN_TYPE + " = '" + strings[0] + "'";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor.moveToFirst()) {

            int index = cursor.getColumnIndex(Contract.InputEntry.COLUMN_AMOUNT);
            amountInput = cursor.getInt(index);
        }

        cursor.close();
    }

    // получение суммы всех покупок акций
    private void getBuys(String lPortfolio, long lDate) {

        int costOfBuys = 0;
        final String COLUMN_COST = "cost";

        String [] strings = mContext.getResources().getStringArray(R.array.spinType_deal_array);

        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_DEALS)
                .appendPath(Contract.PATH_SUM)
                .build();
//Log.d(TAG, uri.toString() + " getBuys.uri");
        // SELECT portfolio, sum(price * volume) AS 'cost'
        String[] projection = {
                Contract.DealsEntry.COLUMN_PORTFOLIO,
                "sum (" + Contract.DealsEntry.COLUMN_PRICE + " * " +
                        Contract.DealsEntry.COLUMN_VOLUME+ ") AS '" +
                        COLUMN_COST + "'"
        };
//Log.d(TAG, projection[0] + " " + projection[1] + " getBuys.projection");
        // WHERE portfolio = '5838199' AND date < 123 AND type = 'Buy'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.DealsEntry.COLUMN_DATE + " < " + lDate + " AND " +
                Contract.DealsEntry.COLUMN_TYPE + " = '" + strings[1] + "'";
//Log.d(TAG, selection + " getBuys.selection");
        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor.moveToFirst()) {

            int index = cursor.getColumnIndex(COLUMN_COST);
            costOfBuys = cursor.getInt(index);
        }
//Log.d(TAG, costOfBuys + " getBuys().costOfBuys");
        cursor.close();
    }

    // получение суммы всех продаж акций
    private void getSells (String lPortfolio, long lDate) {

        int costOfSells = 0;
        final String COLUMN_COST = "cost";

        String [] strings = mContext.getResources().getStringArray(R.array.spinType_deal_array);

        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_DEALS)
                .appendPath(Contract.PATH_SUM)
                .build();
Log.d(TAG, uri.toString() + " getSells.uri");
        // SELECT portfolio, sum(price * volume) AS 'cost'
        String[] projection = {
                Contract.DealsEntry.COLUMN_PORTFOLIO,
                "sum (" + Contract.DealsEntry.COLUMN_PRICE + " * " +
                        Contract.DealsEntry.COLUMN_VOLUME+ ") AS '" +
                        COLUMN_COST + "'"
        };
Log.d(TAG, projection[0] + " " + projection[1] + " getSells.projection");
        // WHERE portfolio = '5838199' AND date < 123 AND type = 'Sell'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.DealsEntry.COLUMN_DATE + " < " + lDate + " AND " +
                Contract.DealsEntry.COLUMN_TYPE + " = '" + strings[0] + "'";
Log.d(TAG, selection + " getSells.selection");
        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor.moveToFirst()) {

            int index = cursor.getColumnIndex(COLUMN_COST);
            costOfSells = cursor.getInt(index);
        }
Log.d(TAG, costOfSells + " getSells().costOfSells");
        cursor.close();
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