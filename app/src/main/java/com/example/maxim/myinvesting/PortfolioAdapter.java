package com.example.maxim.myinvesting;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.maxim.myinvesting.data.Contract;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 04.08.17.
 */

public class PortfolioAdapter extends RecyclerView.Adapter <PortfolioAdapter.PortfolioViewHolder>{

    private Cursor mCursor;

    @Override
    public PortfolioAdapter.PortfolioViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_portfolio;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = layoutInflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        PortfolioViewHolder viewHolder = new PortfolioViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PortfolioAdapter.PortfolioViewHolder holder, int position) {

        int idIndex = mCursor.getColumnIndex(Contract.DealsEntry._ID);
        int tickerIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_TICKER);
        int volumeIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_VOLUME);

        mCursor.moveToPosition(position);

        final int id = mCursor.getInt(idIndex);
        String ticker = mCursor.getString(tickerIndex);
        int volume = mCursor.getInt(volumeIndex);
Log.d(TAG, ticker);
        holder.itemView.setTag(id);

        holder.bind(ticker, volume);
    }

    @Override
    public int getItemCount() {

        if (mCursor == null) {
            return 0;
        }

        return mCursor.getCount();
    }

    // Функция заменяет старый курсор на новый когда данные изменились
    public Cursor swapCursor(Cursor c) {

        // проверяем тот же ли это курсор, если да то возвращаемся - ничего не поменялось
        if (mCursor == c) {
            return null;
        }

        Cursor temp = mCursor;
        this.mCursor = c; // установлен новое значение

        if (c != null) {
            this.notifyDataSetChanged();
        }

        return temp;
    }

    class PortfolioViewHolder extends RecyclerView.ViewHolder {

        TextView tvPortfolioTicker;
        TextView tvPortfolioVolume;

        public PortfolioViewHolder(View itemView) {
            super(itemView);

            tvPortfolioTicker = (TextView) itemView.findViewById(R.id.tv_item_portfolio_ticker);
            tvPortfolioVolume = (TextView) itemView.findViewById(R.id.tv_item_portfolio_volume);
        }

        void bind (String lTicker, int lVolume) {

            tvPortfolioTicker.setText(lTicker);
            tvPortfolioVolume.setText(String.valueOf(lVolume));
        }
    }
}
