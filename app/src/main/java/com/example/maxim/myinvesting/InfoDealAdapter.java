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
import com.example.maxim.myinvesting.utilities.DateUtils;

import static com.example.maxim.myinvesting.data.Const.TAG;

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
Log.d(TAG, "InfoDealAdapter.onBindViewHolder " + Integer.toString(position));
        final int id = mCursor.getInt(idIndex);
        String ticker = mCursor.getString(tickerIndex);
        String type = mCursor.getString(typeIndex);
        long dateInMillis = mCursor.getLong(dateIndex);
        int price = mCursor.getInt(priceIndex);
        int volume = mCursor.getInt(volumeIndex);
        int fee = mCursor.getInt(feeIndex);

        holder.itemView.setTag(id);

        String dateNormal = DateUtils.getNormalTimeForMoscow(dateInMillis);

        holder.bind(ticker, type, dateNormal, price, volume, fee);

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

        TextView tvInfoItemTicker;
        TextView tvInfoItemType;
        TextView tvInfoItemDate;
        TextView tvInfoItemPrice;
        TextView tvInfoItemVolume;
        TextView tvInfoItemFee;

        public InfoViewHolder(View itemView) {
            super(itemView);

            tvInfoItemTicker = (TextView) itemView.findViewById(R.id.tv_info_item_ticker);
            tvInfoItemType = (TextView) itemView.findViewById(R.id.tv_info_item_type);
            tvInfoItemDate = (TextView) itemView.findViewById(R.id.tv_info_item_date);
            tvInfoItemPrice = (TextView) itemView.findViewById(R.id.tv_info_item_price);
            tvInfoItemVolume = (TextView) itemView.findViewById(R.id.tv_info_item_volume);
            tvInfoItemFee = (TextView) itemView.findViewById(R.id.tv_info_item_fee);
        }

        void bind(String lTicker,
                  String lType,
                  String lDate,
                  int lPrice,
                  int lVolume,
                  int lFee) {
            tvInfoItemTicker.setText(lTicker);
            tvInfoItemType.setText(lType);
            tvInfoItemDate.setText(lDate);
            tvInfoItemPrice.setText(String.valueOf(lPrice));
            tvInfoItemVolume.setText(String.valueOf(lVolume));
            tvInfoItemFee.setText(String.valueOf(lFee));
Log.d(TAG, lType);
        }
    }
}
