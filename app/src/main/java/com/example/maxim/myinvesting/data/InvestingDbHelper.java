package com.example.maxim.myinvesting.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.maxim.myinvesting.MainActivity.TAG;
import com.example.maxim.myinvesting.data.Contract.DealsEntry;

/**
 * Created by maxim on 26.03.17.
 */

public class InvestingDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "investing.db";

    private static final int DATABASE_VERSION = 1;

    public InvestingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_DEALS_TABLE =
                "CREATE TABLE " + DealsEntry.TABLE_NAME + " (" +

                        DealsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DealsEntry.COLUMN_TICKER + " STRING, " +

                        // тип бумаги: S - акция, B - облигация, D - дивиденд, T - налог
                        DealsEntry.COLUMN_TYPE + " STRING NOT NULL, " +

                        DealsEntry.COLUMN_DATE + " INTEGER NOT NULL, " +

                        // цена на одну единицу товара в копейках
                        DealsEntry.COLUMN_PRICE + " INTEGER NOT NULL, " +

                        DealsEntry.COLUMN_VOLUME + " INTEGER NOT NULL," +

                        DealsEntry.COLUMN_FEE + " INTEGER" +

                        ");";

        db.execSQL(SQL_CREATE_DEALS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DealsEntry.TABLE_NAME);
        onCreate(db);

    }
}
