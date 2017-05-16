package com.example.maxim.myinvesting;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.maxim.myinvesting.data.Contract;
import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 09.04.17.
 */

public class InfoDealActivity extends InfoActivity {

    private InfoDealAdapter mAdapter;

    @Override
    public RecyclerView.Adapter getAdapter() {

        mAdapter = new InfoDealAdapter();
        return mAdapter;
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
