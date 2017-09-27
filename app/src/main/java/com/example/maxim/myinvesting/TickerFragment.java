package com.example.maxim.myinvesting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.maxim.myinvesting.data.PortfolioItem;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 05.09.17.
 */

public class TickerFragment extends Fragment {

    PortfolioItem portfolioItem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragmant_ticker, container, false);

        TextView tvName = (TextView) rootView.findViewById(R.id.tv_name_ticker_fragment);
        portfolioItem.getName(tvName);

        return rootView;
    }

    public void putPortfolioItem(PortfolioItem portfolioItem) {

        this.portfolioItem = portfolioItem;
    }
}
