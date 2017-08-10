package com.example.maxim.myinvesting.utilities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.maxim.myinvesting.data.PortfolioItem;

import static com.example.maxim.myinvesting.data.Const.KEY_FOR_LOADER;
import static com.example.maxim.myinvesting.data.Const.TAG;

import java.util.ArrayList;

/**
 * Created by maxim on 09.08.17.
 */

public class PortfolioLoader extends AsyncTaskLoader<ArrayList<PortfolioItem>> {


    private String portfolio;

    public PortfolioLoader(Context context, Bundle bundle) {
        super(context);

        portfolio = bundle.getString(KEY_FOR_LOADER);
    }

    @Override
    public ArrayList<PortfolioItem> loadInBackground() {

        ArrayList<PortfolioItem> linkedHashMap = new ArrayList<>();

            PortfolioItem portfolioItem1 = new PortfolioItem(1, "SBER", 10);
            PortfolioItem portfolioItem2 = new PortfolioItem(2, "MVID", 10);
            PortfolioItem portfolioItem3 = new PortfolioItem(3, "MAGN", 10);
            PortfolioItem portfolioItem4 = new PortfolioItem(4, "ALRS", 10);
            PortfolioItem portfolioItem5 = new PortfolioItem(5, "ALFT", 10);

            linkedHashMap.add(portfolioItem1);
            linkedHashMap.add(portfolioItem2);
            linkedHashMap.add(portfolioItem3);
            linkedHashMap.add(portfolioItem4);
            linkedHashMap.add(portfolioItem5);

Log.d(TAG, linkedHashMap.size() + " loadInBackground");

            return linkedHashMap;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
Log.d(TAG, "onStartLoading");
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
Log.d(TAG, "onStopLoading");
    }

    @Override
    public void onCanceled(ArrayList<PortfolioItem> data) {
        super.onCanceled(data);
        onReleaseResources();
Log.d(TAG, "onCanceled");
    }

    protected void onReleaseResources() {

    }
}

//    private String portfolio;
//    private GetTask getTask;
//
//    public PortfolioLoader(Context context, Bundle bundle) {
//        super(context);
//
//        portfolio = bundle.getString(KEY_FOR_LOADER);
//    }
//
//    @Override
//    protected void onStartLoading() {
//        super.onStartLoading();
//Log.d(TAG, "onStartLoading");
//    }
//
//    @Override
//    protected void onForceLoad() {
//        super.onForceLoad();
//Log.d(TAG, "onForceLoad");
//        if (getTask != null)
//            getTask.cancel(true);
//
//        getTask = new GetTask();
//        getTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, portfolio);
//    }
//
//    class GetTask extends AsyncTask<String, Void, LinkedHashMap<String, PortfolioItem>> {
//
//        @Override
//        protected LinkedHashMap<String, PortfolioItem> doInBackground(String... params) {
//
//            LinkedHashMap<String, PortfolioItem> linkedHashMap = new LinkedHashMap<String, PortfolioItem>(5);
//
//            PortfolioItem portfolioItem1 = new PortfolioItem(1, "SBER", 10);
//            PortfolioItem portfolioItem2 = new PortfolioItem(2, "MVID", 10);
//            PortfolioItem portfolioItem3 = new PortfolioItem(3, "MAGN", 10);
//            PortfolioItem portfolioItem4 = new PortfolioItem(4, "ALRS", 10);
//            PortfolioItem portfolioItem5 = new PortfolioItem(5, "ALFT", 10);
//
//            linkedHashMap.put("SBER", portfolioItem1);
//            linkedHashMap.put("MVID", portfolioItem2);
//            linkedHashMap.put("MAGN", portfolioItem3);
//            linkedHashMap.put("ALRS", portfolioItem4);
//            linkedHashMap.put("ALFT", portfolioItem5);
//Log.d(TAG, "AsyncTask");
//            return linkedHashMap;
//        }
//    }
//}




//    private LinkedHashMap<String, PortfolioItem> readDB() {
//
//        Uri uri = Contract.PortfolioEntry.CONTENT_URI.buildUpon()
//                .appendPath(
//                        Contract.DealsEntry.COLUMN_TICKER)
//                .build();
//
//        // SELECT _ID, ticker, sum(volume) AS 'volume'
//        String [] projection = {Contract.DealsEntry._ID,
//                Contract.DealsEntry.COLUMN_TICKER,
//                "sum (" + Contract.DealsEntry.COLUMN_VOLUME
//                        + ") AS '" + Contract.DealsEntry.COLUMN_VOLUME + "'",
//        };
//
//        // WHERE portfolio = '5838194'
//        String selection = "portfolio = " + ((MainActivity) context).getNameOfPortfolio();
//
//        Cursor cursor = getContext().getContentResolver().query(
//                uri,
//                projection,
//                selection,
//                null,
//                null);
//    }