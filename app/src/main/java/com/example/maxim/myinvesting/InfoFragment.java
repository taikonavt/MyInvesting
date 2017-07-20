package com.example.maxim.myinvesting;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.maxim.myinvesting.data.Contract;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 14.06.17.
 */

public abstract class InfoFragment extends Fragment // TODO: 20.06.17 узнать что будет если заменить на FragmetnActivity как написано в мануале на v4
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    private static final int INFO_DEAL_LOADER_ID = 11;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_info, container, false);
Log.d(TAG, "InfoFragment.onCreateView");
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_info);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mAdapter = getAdapter();

        mRecyclerView.setAdapter(mAdapter);

        // удаляю потому что относится к MainActivity?
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getLoaderManager().initLoader(0, null, this);

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
                Uri uri = getUri();
                uri = uri.buildUpon().appendPath(stringId).build();

                getContext().getContentResolver().delete(uri, null, null);

                getLoaderManager().restartLoader(INFO_DEAL_LOADER_ID,
                        null, InfoFragment.this);
            }
        }).attachToRecyclerView(mRecyclerView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(INFO_DEAL_LOADER_ID, null, this);
    }

    @Override
    public abstract Loader<Cursor> onCreateLoader(int id, Bundle args);

    public abstract RecyclerView.Adapter getAdapter();

    public abstract Uri getUri();
}
