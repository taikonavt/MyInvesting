package com.example.maxim.myinvesting.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


import static com.example.maxim.myinvesting.data.Contract.DealsEntry;
import static com.example.maxim.myinvesting.data.Contract.InputEntry;
import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 26.03.17.
 */

public class InvestingProvider extends ContentProvider{

    public static final int CODE_DEALS = 100;
    public static final int CODE_DEAL_WITH_ID = 101;
    public static final int CODE_DEALS_SUM = 102;
    public static final int CODE_DEALS_DATE_PRICE = 103;
    public static final int CODE_DEALS_FEES = 104;

    public static final int CODE_INPUT = 200;
    public static final int CODE_INPUT_WITH_ID = 201;
    public static final int CODE_INPUT_SUM = 202;

    public static final int CODE_PORTFOLIO = 300;
    public static final int CODE_PORTFOLIO_WITH_TICKER = 301;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Contract.CONTENT_AUTHORITY;

        matcher.addURI(authority, Contract.PATH_DEALS, CODE_DEALS);
        matcher.addURI(authority, Contract.PATH_DEALS + "/#", CODE_DEAL_WITH_ID);
        matcher.addURI(authority, Contract.PATH_DEALS + "/" + Contract.PATH_SUM, CODE_DEALS_SUM);
        matcher.addURI(authority, Contract.PATH_DEALS + "/" +
                Contract.PATH_DATE_PRICE, CODE_DEALS_DATE_PRICE);
        matcher.addURI(authority, Contract.PATH_DEALS + "/" +
                Contract.PATH_FEES, CODE_DEALS_FEES);

        matcher.addURI(authority, Contract.PATH_INPUT, CODE_INPUT);
        matcher.addURI(authority, Contract.PATH_INPUT + "/#", CODE_INPUT_WITH_ID);
        matcher.addURI(authority, Contract.PATH_INPUT + "/" + Contract.PATH_SUM, CODE_INPUT_SUM);

        matcher.addURI(authority, Contract.PATH_PORTFOLIO, CODE_PORTFOLIO);
        matcher.addURI(authority, Contract.PATH_PORTFOLIO + "/*", CODE_PORTFOLIO_WITH_TICKER);

        return  matcher;
    }

    private InvestingDbHelper mOpenHelper;

    @Override
    public boolean onCreate() {

        mOpenHelper = new InvestingDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor cursor;

        switch (match) {

            case CODE_DEALS:
                cursor = db.query(DealsEntry.TABLE_NAME,
                        projection, // список возвращаемых полей
                        selection, // where
                        selectionArgs, // значения аргументов
                        null,
                        null,
                        sortOrder
                        );
                break;

            case CODE_DEALS_SUM: {

                String groupBy = uri.getPathSegments().get(1);

                cursor = db.query(DealsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        groupBy,
                        null,
                        null
                );
                break;
            }

            case CODE_DEALS_DATE_PRICE: {
// TODO: 20.10.17 заменить на названия из контракта
                String groupBy = "date, price";

                cursor = db.query(DealsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        groupBy,
                        null,
                        null
                );
                break;
            }

            case CODE_DEALS_FEES: {

                String groupBy = DealsEntry.COLUMN_PORTFOLIO;

                cursor = db.query(DealsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        groupBy,
                        null,
                        null
                );
                break;
            }

            case CODE_INPUT:
                cursor = db.query(InputEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                        );
                break;

            case CODE_INPUT_SUM: {

                String groupBy = uri.getPathSegments().get(1);

                cursor = db.query(InputEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        groupBy,
                        null,
                        null
                        );
                break;
            }

            case CODE_PORTFOLIO_WITH_TICKER: {

                String groupBy = uri.getPathSegments().get(1);

                cursor = db.query(DealsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        groupBy,
                        null,
                        sortOrder
                        );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // // TODO: 14.04.17 не очень понятно, надо попробовать
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Uri returnUri;

        switch (sUriMatcher.match(uri)) {

            case CODE_DEALS: {

                long id = db.insert(DealsEntry.TABLE_NAME, null, values);

                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(Contract.DealsEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
            }
                break;

            case CODE_INPUT: {

                long id = db.insert(InputEntry.TABLE_NAME, null, values);

                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(InputEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int rowDeleted;
        String id;

        switch (match) {

            case CODE_DEAL_WITH_ID:

                id = uri.getPathSegments().get(1);

                rowDeleted = db.delete(DealsEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;

            case CODE_INPUT_WITH_ID:

                id = uri.getPathSegments().get(1);

                rowDeleted = db.delete(InputEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowDeleted !=0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
