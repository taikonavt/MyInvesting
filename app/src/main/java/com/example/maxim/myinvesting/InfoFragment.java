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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.maxim.myinvesting.data.Contract;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 14.06.17.
 */

public abstract class InfoFragment extends Fragment // TODO: 20.06.17 узнать что будет если заменить на FragmentActivity как написано в мануале на v4
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    static final int INFO_LOADER_ID = 11;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_info, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_info);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        if (getUri() == Contract.DealsEntry.CONTENT_URI)
            ((MainActivity) getContext()).getSupportActionBar().
                    setTitle(getString(R.string.info_fragment_deals));
        if (getUri() == Contract.InputEntry.CONTENT_URI)
            ((MainActivity) getContext()).getSupportActionBar().
                    setTitle(getString(R.string.info_fragment_inputs));

        mAdapter = getAdapter();

        mRecyclerView.setAdapter(mAdapter);

        getLoaderManager().initLoader(INFO_LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(INFO_LOADER_ID, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        ((MainActivity) getContext()).getSupportActionBar().setTitle(null);
    }

    @Override
    public abstract Loader<Cursor> onCreateLoader(int id, Bundle args);

    public abstract RecyclerView.Adapter getAdapter();

    // используется для определения какой фрагмент сейчас активен
    public abstract Uri getUri();
}
