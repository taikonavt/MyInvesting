package com.example.maxim.myinvesting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.maxim.myinvesting.data.PortfolioItem;
import com.example.maxim.myinvesting.utilities.PortfolioLoader;
import com.example.maxim.myinvesting.data.Contract;

import java.util.ArrayList;

import static com.example.maxim.myinvesting.data.Const.TAG;
import static com.example.maxim.myinvesting.data.Const.KEY_FOR_LOADER;

/**
 * Created by maxim on 04.08.17.
 */

public class PortfolioFrarment extends Fragment
        implements LoaderManager.LoaderCallbacks<ArrayList<PortfolioItem>>{

    private RecyclerView mRecyclerView;
    private PortfolioAdapter mAdapter;

    private static final int PORTFOLIO_LOADER_ID = 12;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_info, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_info);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mAdapter = new PortfolioAdapter();

        mRecyclerView.setAdapter(mAdapter);

        getLoaderManager().initLoader(PORTFOLIO_LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(PORTFOLIO_LOADER_ID, null, this);
    }

    @Override
    public Loader<ArrayList<PortfolioItem>> onCreateLoader(int id, Bundle args) {

        AsyncTaskLoader<ArrayList<PortfolioItem>> loader = new PortfolioLoader(getActivity());

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<PortfolioItem>> loader, ArrayList<PortfolioItem> data) {

        mAdapter.swapArray(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<PortfolioItem>> loader) {

        mAdapter.swapArray(null);
    }
}
