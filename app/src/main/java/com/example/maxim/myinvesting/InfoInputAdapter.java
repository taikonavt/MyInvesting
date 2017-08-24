package com.example.maxim.myinvesting;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.utilities.DateUtils;

import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;
import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 16.05.17.
 */

public class InfoInputAdapter extends RecyclerView.Adapter <InfoInputAdapter.InfoViewHolder>{

    private Cursor mCursor;

    @Override
    public InfoInputAdapter.InfoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_info_input;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        InfoInputAdapter.InfoViewHolder viewHolder = new InfoInputAdapter.InfoViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(InfoInputAdapter.InfoViewHolder holder, int position) {

        int idIndex = mCursor.getColumnIndex(Contract.InputEntry._ID);
        int typeIndex = mCursor.getColumnIndex(Contract.InputEntry.COLUMN_TYPE);
        int dateIndex = mCursor.getColumnIndex(Contract.InputEntry.COLUMN_DATE);
        int amountIndex = mCursor.getColumnIndex(Contract.InputEntry.COLUMN_AMOUNT);
        int currencyIndex = mCursor.getColumnIndex(Contract.InputEntry.COLUMN_CURRENCY);
        int feeIndex = mCursor.getColumnIndex(Contract.InputEntry.COLUMN_FEE);
        int portfoiloIndex = mCursor.getColumnIndex(Contract.InputEntry.COLUMN_PORTFOLIO);
        int noteIndex = mCursor.getColumnIndex(Contract.InputEntry.COLUMN_NOTE);

        mCursor.moveToPosition(position);

        final int id = mCursor.getInt(idIndex);
        String type = mCursor.getString(typeIndex);
        long dateInMillis = mCursor.getLong(dateIndex);
        long amount = mCursor.getLong(amountIndex);
        String currency = mCursor.getString(currencyIndex);
        int fee = mCursor.getInt(feeIndex);
        int portfolio = mCursor.getInt(portfoiloIndex);
        String note = mCursor.getString(noteIndex);

        holder.itemView.setTag(id);

        String dateNormal = DateUtils.getNormalTimeForMoscow(dateInMillis);

        holder.bind(type, dateNormal, amount, currency, fee, portfolio, note);

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


    class InfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvInfoInputItemType;
        TextView tvInfoInputItemDate;
        TextView tvInfoInputItemAmount;
        TextView tvInfoInputItemCurrency;
        TextView tvInfoInputItemFee;
        TextView tvInfoInputItemPortfolio;
        TextView tvInfoInputItemNote; // TODO: 16.05.17 Добавить Note для горизонт. экрана
        TableRow tableRowInput;
        ImageButton deleteButton;

        public InfoViewHolder(View itemView) {
            super(itemView);

            tvInfoInputItemType = (TextView) itemView.findViewById(R.id.tv_info_input_item_type);
            tvInfoInputItemDate = (TextView) itemView.findViewById(R.id.tv_info_input_item_date);
            tvInfoInputItemAmount = (TextView) itemView.findViewById(R.id.tv_info_input_item_amount);
            //tvInfoInputItemCurrency = (TextView) itemView.findViewById(R.id.tv_info_input_item_currency);
            tvInfoInputItemFee = (TextView) itemView.findViewById(R.id.tv_info_input_item_fee);
            tvInfoInputItemPortfolio = (TextView) itemView.findViewById(R.id.tv_info_input_item_portfolio);
            tableRowInput = (TableRow) itemView.findViewById(R.id.tr_info_input);
            deleteButton = (ImageButton) itemView.findViewById(R.id.ib_input_delete);

            deleteButton.setOnClickListener(this);
        }

        void bind(String lType,
                  String lDate,
                  long lAmount,
                  String lCurrency,
                  int lFee,
                  int lPortfolio,
                  String lNote) {
            tvInfoInputItemType.setText(lType);
            tvInfoInputItemDate.setText(lDate);
            tvInfoInputItemAmount.setText(
                    String.valueOf(lAmount/MULTIPLIER_FOR_MONEY) +
                    " " + lCurrency);
            tvInfoInputItemFee.setText("- " + String.valueOf(lFee) + " " + lCurrency);
            tvInfoInputItemPortfolio.setText(String.valueOf(lPortfolio));

            switch (lType) {
                case "Output": tableRowInput.setBackgroundColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.colorSell));
                    break;

                case "Input": tableRowInput.setBackgroundColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.colorBuy));
                    break;

                default:
                    tableRowInput.setBackgroundColor(0);
                    Log.d(TAG, "InfoDealAdapter.java, switch(lType) {default}");
            }
        }

        @Override
        public void onClick(View v) {

            int id = (int) itemView.getTag();

            String stringId = Integer.toString(id);

            Uri uri = Contract.InputEntry.CONTENT_URI;;
            uri = uri.buildUpon().appendPath(stringId).build();

            itemView.getContext().getContentResolver().delete(uri, null, null);
        }
    }
}