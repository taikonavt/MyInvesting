package com.example.maxim.myinvesting;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.TextView;

import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.data.PortfolioData;
import com.example.maxim.myinvesting.data.PortfolioItem;
import com.example.maxim.myinvesting.data.TickerItem;
import com.example.maxim.myinvesting.utilities.TickerLoader;
import com.example.maxim.myinvesting.utilities.PortfolioLoader;

import java.util.ArrayList;

import static com.example.maxim.myinvesting.data.Const.MILLIS_IN_DAY;
import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 05.09.17.
 */

public class TickerFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<TickerItem>> {

    PortfolioItem portfolioItem;

    String nameOfPortfolio;

    RecyclerView mRecyclerView;

    TickerAdapter mAdapter;

    int TICKER_LOADER_ID = 5;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragmant_ticker, container, false);

        TextView tvName = (TextView) rootView.findViewById(R.id.tv_name_ticker_fragment);
        portfolioItem.getName(tvName);

        nameOfPortfolio = ((MainActivity) getContext()).getNameOfPortfolio();

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_ticker_fragment);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mAdapter = new TickerAdapter();

        mRecyclerView.setAdapter(mAdapter);

        getLoaderManager().initLoader(TICKER_LOADER_ID, null, this);

        return rootView;
    }

    public void putPortfolioItem(PortfolioItem portfolioItem) {

        this.portfolioItem = portfolioItem;
    }

    @Override
    public Loader<ArrayList<TickerItem>> onCreateLoader(int id, Bundle args) {

        AsyncTaskLoader<ArrayList<TickerItem>> loader = null;

        if (id == TICKER_LOADER_ID) {
            loader = new TickerLoader(getActivity(), nameOfPortfolio, portfolioItem);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<TickerItem>> loader, ArrayList<TickerItem> data) {

        mAdapter.swapArray(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<TickerItem>> loader) {

        mAdapter.swapArray(null);
    }
}
