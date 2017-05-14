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
 * Created by maxim on 09.04.17.
 */

public class InfoDealActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private InfoDealAdapter mAdapter;
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
        switch (key) {
            case DEALS:
                mAdapter = new InfoDealAdapter();
                break;
            // TODO: 14.05.17 Make Inputs
//            case INPUTS:
//                mAdapter = new InfoInputAdapter();
        }

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
                        null, InfoDealActivity.this);
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        getSupportLoaderManager().restartLoader(INFO_DEAL_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // TODO: 15.04.17 Добавить возможность загрузки только сделок с определенным тикером
        // TODO: 15.04.17 Добавить возможность загрузки сделок после определенной даты
        //Uri dealsSinceDate = Contract.DealsEntry.

//        в зависимости от key переданного из mainActivity использую разные адаптеры
        switch (key) {
            case DEALS:
                String sortOrder = Contract.DealsEntry.COLUMN_DATE + " ASC"; // ASC = по возрастанию

                return new CursorLoader(this,
                        Contract.DealsEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        sortOrder);
           // TODO: 14.05.17  Сделать Inputs
            case INPUTS:
                return null;
            default:
                return null;
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
