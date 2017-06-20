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
 * Created by maxim on 20.06.17.
 */

public class InfoInputFragment extends InfoFragment {

    private InfoInputAdapter mAdapter;

    // метод используется для доступа к экземпляру адаптера из суперкласса
    @Override
    public RecyclerView.Adapter getAdapter() {
        Log.d(TAG, "InfoInputFragment.getAdapter");
        mAdapter = new InfoInputAdapter();
        return mAdapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO: 15.04.17 Добавить возможность загрузки только сделок с определенным тикером
        // TODO: 15.04.17 Добавить возможность загрузки сделок после определенной даты
        Log.d(TAG, "InfoInputFragment.onCreateLoader");
        String sortOrder = Contract.InputEntry.COLUMN_DATE + " ASC"; // ASC = по возрастанию

        return new CursorLoader(getContext(),
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

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
