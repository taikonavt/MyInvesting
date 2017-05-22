package com.example.maxim.myinvesting;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;

import com.example.maxim.myinvesting.data.Contract;

/**
 * Created by maxim on 16.05.17.
 */

public class InfoInputActivity extends InfoActivity {

    private InfoInputAdapter mAdapter;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = Contract.InputEntry.COLUMN_DATE + " ASC"; // ASC = по возрастанию

        return new CursorLoader(this,
                Contract.InputEntry.CONTENT_URI,
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

    @Override
    public RecyclerView.Adapter getAdapter() {

        mAdapter = new InfoInputAdapter();
        return mAdapter;
    }
}
