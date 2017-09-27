package com.example.maxim.myinvesting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.data.PortfolioItem;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;
import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 04.08.17.
 */

public class PortfolioAdapter extends RecyclerView.Adapter <PortfolioAdapter.PortfolioViewHolder>{

    private ArrayList<PortfolioItem> arrayList;

    Context context;

    @Override
    public PortfolioAdapter.PortfolioViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_portfolio;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = layoutInflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        PortfolioViewHolder viewHolder = new PortfolioViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PortfolioAdapter.PortfolioViewHolder holder, int position) {

        PortfolioItem portfolioItem = arrayList.get(position);

        int id = portfolioItem.getId();

        holder.itemView.setTag(id);
        holder.bind(portfolioItem);
    }

    @Override
    public int getItemCount() {

        if (arrayList == null)
            return 0;

        return arrayList.size();
    }

    // Функция заменяет старый курсор на новый когда данные изменились
    public ArrayList<PortfolioItem> swapArray(ArrayList<PortfolioItem> a) {

        // проверяем тот же ли это курсор, если да то возвращаемся - ничего не поменялось
        if (arrayList == a) {
            return null;
        }

        ArrayList<PortfolioItem> temp = arrayList;
        this.arrayList = a; // установлен новое значение

        if (a != null) {
            this.notifyDataSetChanged();
        }

        return temp;
    }

    class PortfolioViewHolder extends RecyclerView.ViewHolder {

        TextView tvPortfolioTicker;
        TextView tvPortfolioVolume;
        TextView tvPortfolioPrice;
        TextView tvPortfolioCost;

        public PortfolioViewHolder(View itemView) {
            super(itemView);

            tvPortfolioTicker = (TextView) itemView.findViewById(R.id.tv_item_portfolio_ticker);
            tvPortfolioVolume = (TextView) itemView.findViewById(R.id.tv_item_portfolio_volume);
            tvPortfolioPrice = (TextView) itemView.findViewById(R.id.tv_item_portfolio_price);
            tvPortfolioCost = (TextView) itemView.findViewById(R.id.tv_item_portfolio_cost);
        }

        void bind (final PortfolioItem portfolioItem) {

            tvPortfolioTicker.setText(portfolioItem.getTicker());
            tvPortfolioVolume.setText(String.valueOf(portfolioItem.getVolume()));
            portfolioItem.getPriceAndCost(tvPortfolioPrice, tvPortfolioCost);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) context).onPortfolioItemClick(portfolioItem);
                }
            });
        }
    }
}
