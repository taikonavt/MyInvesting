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


import static com.example.maxim.myinvesting.data.Contract.DealsEntry.TABLE_NAME;
import static com.example.maxim.myinvesting.MainActivity.TAG;

/**
 * Created by maxim on 26.03.17.
 */

public class InvestingProvider extends ContentProvider{

    public static final int CODE_DEALS = 100;
    public static final int CODE_DEAL_WITH_ID = 101;
    public static final int CODE_DEALS_WITH_DATE = 102; // пока не используется

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Contract.CONTENT_AUTHORITY;

        matcher.addURI(authority, Contract.PATH_DEALS, CODE_DEALS);
        matcher.addURI(authority, Contract.PATH_DEALS + "/#", CODE_DEAL_WITH_ID);
        matcher.addURI(authority, Contract.PATH_DEALS + "/#/#", CODE_DEALS_WITH_DATE);

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
                cursor = db.query(TABLE_NAME,
                        projection, // список возвращаемых полей
                        selection, // where
                        selectionArgs, // значения аргументов
                        null,
                        null,
                        sortOrder
                        );
                break;
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

            case CODE_DEALS:

                long id = db.insert(TABLE_NAME, null, values);

                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(Contract.DealsEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return returnUri;
    }

    // todo сделать swipe для удаления
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int rowDeleted;

        switch (match) {

            case CODE_DEAL_WITH_ID:

                String id = uri.getPathSegments().get(1);

                rowDeleted = db.delete(TABLE_NAME, "_id=?", new String[]{id});
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
