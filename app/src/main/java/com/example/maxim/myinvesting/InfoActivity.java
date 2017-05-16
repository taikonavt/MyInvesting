package com.example.maxim.myinvesting;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;

import com.example.maxim.myinvesting.data.Contract;
import static com.example.maxim.myinvesting.data.Const.TAG;
import static com.example.maxim.myinvesting.data.Const.DEALS;
import static com.example.maxim.myinvesting.data.Const.INPUTS;
import static com.example.maxim.myinvesting.data.Const.KEY;

/**
 * Created by maxim on 16.05.17.
 */

public abstract class InfoActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    protected RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private String key;

    private static final int INFO_DEAL_LOADER_ID = 11;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_deal);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_info_deals);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        key = getIntent().getStringExtra(KEY);

        // в зависимости от кеу переданного из mainActivity использую разные адаптеры

        mAdapter = getAdapter();

        mRecyclerView.setAdapter(mAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportLoaderManager().initLoader(0, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                int id = (int) viewHolder.itemView.getTag();

                String stringId = Integer.toString(id);
                Uri uri = Contract.DealsEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();

                getContentResolver().delete(uri, null, null);

                getSupportLoaderManager().restartLoader(INFO_DEAL_LOADER_ID,
                        null, InfoActivity.this);
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        getSupportLoaderManager().restartLoader(INFO_DEAL_LOADER_ID, null, this);
    }

    @Override
    public abstract Loader<Cursor> onCreateLoader(int id, Bundle args);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public abstract RecyclerView.Adapter getAdapter();
}
