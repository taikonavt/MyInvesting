package com.example.maxim.myinvesting;

import android.content.Context;
import android.database.Cursor;
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


////                String [] projection = {Contract.DealsEntry.COLUMN_TICKER,
////                        "sum(" + Contract.DealsEntry.COLUMN_VOLUME + ")"};
////
////                String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = " + nameOfPortfolio;
////
////                String groupBy = Contract.DealsEntry.COLUMN_TICKER;
////
////                return new CursorLoader(getContext(),
////                        Contract.PortfolioEntry.CONTENT_URI,
////                        projection,
////                        selection,
////                        null,
////                        groupBy
////                        );
//
//        return new PortfolioCursorLoader(getActivity());

        String [] projection = {Contract.DealsEntry._ID,
                Contract.DealsEntry.COLUMN_TICKER,
                Contract.DealsEntry.COLUMN_VOLUME};

        return new CursorLoader(getContext(),
                Contract.DealsEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

//    static class PortfolioCursorLoader extends CursorLoader{
//
//        Context context;
//
//        public PortfolioCursorLoader(Context context) {
//
//            super(context);
//            this.context = context;
//        }
//
//        @Override
//        public Cursor loadInBackground() {
//
//            String [] projection = {Contract.DealsEntry.COLUMN_TICKER,
//                    Contract.DealsEntry.COLUMN_VOLUME};
//
//            String selection = ((MainActivity) context).getNameOfPortfolio();
//
//            getContext().getContentResolver().query(
//                    Contract.DealsEntry.CONTENT_URI, // TODO: 07.08.17 ввел не правильно изменить
//                    projection,
//                    selection,
//                    null,
//                    null
//            );
//
//            return super.loadInBackground();
//        }
//    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mAdapter.swapCursor(null);
    }
}
