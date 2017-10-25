package com.example.maxim.myinvesting.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by maxim on 25.10.17.
 */

public class PortfolioNames {

    public static void savePortfolioName(Context context, String string) {

        Uri uri = Contract.PortfolioEntry.CONTENT_URI;

        ContentValues cv = new ContentValues();

        cv.put(Contract.PortfolioEntry.COLUMN_PORTFOLIO, string);

        context.getContentResolver().insert(uri, cv);
    }

    public static String[] readPortfoliosNames(Context context) {

        Uri uri = Contract.PortfolioEntry.CONTENT_URI;

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        int portfolioIndex = cursor.getColumnIndex(Contract.PortfolioEntry.COLUMN_PORTFOLIO);

        String [] portfolios = new String[cursor.getCount()];

        int i = 0;

        if (cursor.moveToFirst()) {

            do {

                String string = cursor.getString(portfolioIndex);

                portfolios[i] = string;

                i++;

            } while (cursor.moveToNext());
        }

        return portfolios;
    }
}
