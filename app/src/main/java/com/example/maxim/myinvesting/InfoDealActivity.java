package com.example.maxim.myinvesting;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.maxim.myinvesting.data.Contract;

/**
 * Created by maxim on 09.04.17.
 */

public class InfoDealActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private InfoDealAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private static final int INFO_DEAL_LOADER_ID = 11;

    // todo change number into real when it become clear
    private static final int numOfItems = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_deal);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_info_deals);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mAdapter = new InfoDealAdapter();
        mRecyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        getSupportLoaderManager().restartLoader(INFO_DEAL_LOADER_ID, null, this);
    }

    public void onClick(View view) {

        Intent intent = new Intent(this, AddDealActivity.class);
        startActivity(intent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // TODO: 15.04.17 Добавить возможность загрузки только сделок с определенным тикером
        // TODO: 15.04.17 Добавить возможность загрузки сделок после определенной даты
        //Uri dealsSinceDate = Contract.DealsEntry.

        String sortOrder = Contract.DealsEntry.COLUMN_DATE + " ASC"; // ASC = по возрастанию

        return new CursorLoader(this,
                Contract.DealsEntry.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.
     * onLoaderReset removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
