package com.example.maxim.myinvesting;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;

import com.example.maxim.myinvesting.data.Contract;

/**
 * Created by maxim on 15.06.17.
 */

public class InfoDealFragment extends InfoFragment {

    private InfoDealAdapter mAdapter;

    // метод используется для доступа к экземпляру адаптера из суперкласса
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

        return new CursorLoader(getContext(),
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

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
