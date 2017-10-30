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
import android.widget.TextView;

import com.example.maxim.myinvesting.data.PortfolioData;
import com.example.maxim.myinvesting.utilities.PortfolioLoader;

import java.text.DecimalFormat;

import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;
import static com.example.maxim.myinvesting.data.Const.TAG;
import static com.example.maxim.myinvesting.data.Const.KEY_FOR_LOADER;

/**
 * Created by maxim on 04.08.17.
 */

public class PortfolioFrarment extends Fragment
        implements LoaderManager.LoaderCallbacks<PortfolioData>{

    private RecyclerView mRecyclerView;
    private PortfolioAdapter mAdapter;
    private TextView tvCost;
    private TextView tvProfit;

    private static final int PORTFOLIO_LOADER_ID = 12;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_portfolio, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_portfolio_fragment);
        tvCost = (TextView) rootView.findViewById(R.id.tv_total_cost_fragment_portfolio);
        tvProfit = (TextView) rootView.findViewById(R.id.tv_profitability_fragment_portfolio);

        ((MainActivity) getContext()).getSupportActionBar().setTitle(
                ((MainActivity) getContext()).getNameOfPortfolio());

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

    }

    @Override
    public Loader<PortfolioData> onCreateLoader(int id, Bundle args) {


        AsyncTaskLoader<PortfolioData> loader = null;

        if (id == PORTFOLIO_LOADER_ID) {
            loader = new PortfolioLoader(getActivity());
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<PortfolioData> loader, PortfolioData data) {

        data.setProfitAndCostOfPortfolio(tvProfit, tvCost);

        mAdapter.swapArray(data.getPortfolioItems());
    }

    @Override
    public void onLoaderReset(Loader<PortfolioData> loader) {

        mAdapter.swapArray(null);
    }
}
