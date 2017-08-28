package com.example.maxim.myinvesting;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.maxim.myinvesting.data.Contract;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 20.06.17.
 */

public class InfoInputFragment extends InfoFragment
                                implements InfoInputAdapter.AdapterInterface {

    private InfoInputAdapter mAdapter;

    boolean inActionMode = false;

    // метод используется для доступа к экземпляру адаптера из суперкласса
    @Override
    public RecyclerView.Adapter getAdapter() {

        mAdapter = new InfoInputAdapter(this);
        return mAdapter;
    }

    // возвращает CONTENT_URI для InfoFragment.onSwiped();
    @Override
    public Uri getUri() {
        return Contract.InputEntry.CONTENT_URI;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO: 15.04.17 Добавить возможность загрузки только сделок с определенным тикером
        // TODO: 15.04.17 Добавить возможность загрузки сделок после определенной даты

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

    @Override
    public void notifyAdapter() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void setInActionMode(boolean inActionMode) {
        this.inActionMode = inActionMode;
    }
}
