package com.example.maxim.myinvesting;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.maxim.myinvesting.data.Contract;

/**
 * Created by maxim on 09.04.17.
 */

public class InfoDealAdapter extends RecyclerView.Adapter <InfoDealAdapter.InfoViewHolder>{

    private Cursor mCursor;

    @Override
    public InfoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_info_deal;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        InfoViewHolder viewHolder = new InfoViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(InfoViewHolder holder, int position) {

        int idIndex = mCursor.getColumnIndex(Contract.DealsEntry._ID);
        int tickerIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_TICKER);
        int typeIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_TYPE);
        int dateIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_DATE);
        int priceIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_PRICE);
        int volumeIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_VOLUME);
        int feeIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_FEE);

        mCursor.moveToPosition(position);

        final int id = mCursor.getInt(idIndex);
        String ticker = mCursor.getString(tickerIndex);
        String type = mCursor.getString(typeIndex);
        int dateInMillis = mCursor.getInt(dateIndex);
        int price = mCursor.getInt(priceIndex);
        int volume = mCursor.getInt(volumeIndex);
        int fee = mCursor.getInt(feeIndex);
// TODO: 15.04.17 stop here
        holder.bind(ticker);

        // TODO: 15.04.17 do rotation: when vert - few piece of info, when horiz - all info is seen
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


    class InfoViewHolder extends RecyclerView.ViewHolder {

        TextView tvInfoItem;

        public InfoViewHolder(View itemView) {
            super(itemView);

            tvInfoItem = (TextView) itemView.findViewById(R.id.tv_info_item);
        }

        void bind(String listIndex) {
            tvInfoItem.setText(String.valueOf(listIndex));
        }
    }
}
