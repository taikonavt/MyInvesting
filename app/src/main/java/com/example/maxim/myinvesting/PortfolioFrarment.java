package com.example.maxim.myinvesting;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
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
 * Created by maxim on 04.08.17.
 */

public class PortfolioFrarment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

//        Uri uri = Contract.PortfolioEntry.CONTENT_URI.buildUpon()
//                .appendPath(
//                        Contract.DealsEntry.COLUMN_TICKER)
//                .appendPath(
//                        Contract.DealsEntry.COLUMN_TYPE)
//                .build();
//
//        // SELECT _ID, ticker, sum(volume) AS 'volume'
//        String [] projection = {Contract.DealsEntry._ID,
//                Contract.DealsEntry.COLUMN_TICKER,
//                "sum (" + Contract.DealsEntry.COLUMN_VOLUME
//                        + ") AS '" + Contract.DealsEntry.COLUMN_VOLUME + "'",
//                Contract.DealsEntry.COLUMN_TYPE
//        };
//
//        // WHERE portfolio = '5838194'
//        String selection = "portfolio = " + ((MainActivity) getContext()).getNameOfPortfolio();
//
//        return new CursorLoader(getContext(),
//                uri,
//                projection,
//                selection,
//                null,
//                null);


        return new PortfolioCursorLoader(getActivity());
    }

    static class PortfolioCursorLoader extends CursorLoader{

        Context context;

        public PortfolioCursorLoader(Context context) {

            super(context);
            this.context = context;
        }

        @Override
        public Cursor loadInBackground() {

            Uri uri = Contract.PortfolioEntry.CONTENT_URI.buildUpon()
                    .appendPath(
                            Contract.DealsEntry.COLUMN_TICKER)
                    .build();

            // SELECT _ID, ticker, sum(volume) AS 'volume'
            String [] projection = {Contract.DealsEntry._ID,
                    Contract.DealsEntry.COLUMN_TICKER,
                    "sum (" + Contract.DealsEntry.COLUMN_VOLUME
                            + ") AS '" + Contract.DealsEntry.COLUMN_VOLUME + "'",
            };

            // WHERE portfolio = '5838194'
            String selection = "portfolio = " + ((MainActivity) context).getNameOfPortfolio();

            Cursor cursor = getContext().getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    null,
                    null);

            return cursor;
        }
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
