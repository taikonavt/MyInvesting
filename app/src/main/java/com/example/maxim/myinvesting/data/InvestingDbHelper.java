package com.example.maxim.myinvesting.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.maxim.myinvesting.data.Const.TAG;
import com.example.maxim.myinvesting.data.Contract.DealsEntry;
import com.example.maxim.myinvesting.data.Contract.InputEntry;
import com.example.maxim.myinvesting.data.Contract.PortfolioEntry;

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

        // TODO: 25.07.17 добавить валюту сделки
        final String SQL_CREATE_DEALS_TABLE =
                "CREATE TABLE " + DealsEntry.TABLE_NAME + " (" +

                        DealsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        // номер портфеля
                        DealsEntry.COLUMN_PORTFOLIO + " STRING, " +

                        DealsEntry.COLUMN_TICKER + " STRING, " +

                        // тип операции: Sell, Buy
                        DealsEntry.COLUMN_TYPE + " STRING NOT NULL, " +

                        // дата сделки
                        DealsEntry.COLUMN_DATE + " INTEGER NOT NULL, " +

                        // цена одной акции
                        DealsEntry.COLUMN_PRICE + " INTEGER NOT NULL, " +

                        // количество акций в сделке
                        DealsEntry.COLUMN_VOLUME + " INTEGER NOT NULL, " +

                        // уплаченная комиссия
                        DealsEntry.COLUMN_FEE + " INTEGER" +

                        ");";

        db.execSQL(SQL_CREATE_DEALS_TABLE);

        final String SQL_CREATE_INPUT_TABLE =
                "CREATE TABLE " + InputEntry.TABLE_NAME + " (" +

                        InputEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        // input, output
                        InputEntry.COLUMN_TYPE + " STRING NOT NULL, " +

                        InputEntry.COLUMN_DATE + " INTEGER NOT NULL, " +

                        InputEntry.COLUMN_AMOUNT + " INTEGER NOT NULL, " +

                        InputEntry.COLUMN_CURRENCY + " STRING NOT NULL, " +

                        InputEntry.COLUMN_FEE + " INTEGER NOT NULL, " +

                        InputEntry.COLUMN_PORTFOLIO + " STRING NOT NULL, " +

                        InputEntry.COLUMN_NOTE + " STRING" +

                        ");";

        db.execSQL(SQL_CREATE_INPUT_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DealsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + InputEntry.TABLE_NAME);
        onCreate(db);
    }
}
