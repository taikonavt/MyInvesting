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

    public String nameOfPortfolio;

    private RecyclerView mRecyclerView;
    private PortfolioAdapter mAdapter;
    private Cursor [] mCursor = new Cursor[2];
    private Loader<Cursor> loader = null;

    private static final int BUY_LOADER_ID = 12;
    private static final int SELL_LOADER_ID = 13;

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

        loader = getLoaderManager().initLoader(BUY_LOADER_ID, null, this);
        loader = getLoaderManager().initLoader(SELL_LOADER_ID, null, this);
        loader = null;

        sendToAdapter();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(loader.getId(), null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


//                String [] projection = {Contract.DealsEntry.COLUMN_TICKER,
//                        "sum(" + Contract.DealsEntry.COLUMN_VOLUME + ")"};
//
//                String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = " + nameOfPortfolio;
//
//                String groupBy = Contract.DealsEntry.COLUMN_TICKER;
//
//                return new CursorLoader(getContext(),
//                        Contract.PortfolioEntry.CONTENT_URI,
//                        projection,
//                        selection,
//                        null,
//                        groupBy
//                        );

        return new PortfolioCursorLoader(getContext());
    }

    static class PortfolioCursorLoader extends CursorLoader{

        public PortfolioCursorLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {

            String [] projection = {Contract.DealsEntry.COLUMN_TICKER,
                    Contract.DealsEntry.COLUMN_VOLUME};

//            String selection = PortfolioFrarment.nameOfPortfolio;

            getContext().getContentResolver().query(

            )

            return super.loadInBackground();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {

            case BUY_LOADER_ID:
                mCursor[0] = data;
                break;
            case SELL_LOADER_ID:
                mCursor[1] = data;
                break;
            default:
                Log.d(TAG, "Unexpected loader id");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {

            case BUY_LOADER_ID:
                mCursor[0] = null;
                break;
            case SELL_LOADER_ID:
                mCursor[1] = null;
                break;
            default:
                Log.d(TAG, "Unexpected loader id");
        }
    }

    private void sendToAdapter() {
        mAdapter.swapCursor(mCursor);
    }
}
