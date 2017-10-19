package com.example.maxim.myinvesting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.maxim.myinvesting.data.PortfolioItem;
import com.example.maxim.myinvesting.utilities.DateUtils;
import com.example.maxim.myinvesting.data.TickerItem;

import java.util.ArrayList;

import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;

/**
 * Created by maxim on 28.09.17.
 */

class TickerAdapter extends RecyclerView.Adapter <TickerAdapter.TickerViewHolder> {

    private ArrayList<TickerItem> arrayList;

    @Override
    public TickerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_ticker;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = layoutInflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        TickerViewHolder viewHolder = new TickerViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TickerViewHolder holder, int position) {

        TickerItem tickerItem = arrayList.get(position);

        holder.bind(tickerItem);
    }

    @Override
    public int getItemCount() {

        if (arrayList == null)
            return 0;

        return arrayList.size();
    }

    // Функция заменяет старый курсор на новый когда данные изменились
    ArrayList<TickerItem> swapArray(ArrayList<TickerItem> a) {

        // проверяем тот же ли это курсор, если да то возвращаемся - ничего не поменялось
        if (arrayList == a) {
            return null;
        }

        ArrayList<TickerItem> temp = arrayList;
        this.arrayList = a; // установлен новое значение

        if (a != null) {
            this.notifyDataSetChanged();
        }

        return temp;
    }

    class TickerViewHolder extends RecyclerView.ViewHolder {

        TextView tvVolumeBuy;
        TextView tvPriceBuy;
        TextView tvDateBuy;
        TextView tvVolumeSell;
        TextView tvPriceSell;
        TextView tvDateSell;
        TextView tvProfit;
        TextView tvPeriod;

        TickerViewHolder(View itemView) {
            super(itemView);

            tvVolumeBuy = (TextView) itemView.findViewById(R.id.tv_item_ticker_volume_buy);
            tvPriceBuy = (TextView) itemView.findViewById(R.id.tv_item_ticker_price_buy);
            tvDateBuy = (TextView) itemView.findViewById(R.id.tv_item_ticker_date_buy);
            tvVolumeSell = (TextView) itemView.findViewById(R.id.tv_item_ticker_volume_sell);
            tvPriceSell = (TextView) itemView.findViewById(R.id.tv_item_ticker_price_sell);
            tvDateSell = (TextView) itemView.findViewById(R.id.tv_item_ticker_date_sell);
            tvProfit = (TextView) itemView.findViewById(R.id.tv_item_ticker_profit);
            tvPeriod = (TextView) itemView.findViewById(R.id.tv_item_ticker_period);
        }

        void bind(TickerItem tickerItem) {

            tvVolumeBuy.setText(String.valueOf(tickerItem.getVolumeBuy()));
            tvPriceBuy.setText(String.valueOf(
                    (float) tickerItem.getPriceBuy() / MULTIPLIER_FOR_MONEY));
            tvDateBuy.setText(DateUtils.getNormalTimeForMoscow(tickerItem.getDateBuy()));
            tvVolumeSell.setText(String.valueOf(tickerItem.getVolumeSell()));
            tvPriceSell.setText(String.valueOf(
                    (float) tickerItem.getPriceSell() / MULTIPLIER_FOR_MONEY));
            tvDateSell.setText(DateUtils.getNormalTimeForMoscow(tickerItem.getDateSell()));
            tvProfit.setText(String.valueOf((float) tickerItem.getProfit() / MULTIPLIER_FOR_MONEY));
            tvPeriod.setText(String.valueOf(tickerItem.getPeriod()));
        }
    }
}
