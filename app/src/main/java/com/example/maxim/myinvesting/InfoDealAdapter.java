package com.example.maxim.myinvesting;


import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.utilities.DateUtils;

import java.util.ArrayList;

import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_CURRENCY;
import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;
import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 09.04.17.
 */

class InfoDealAdapter extends RecyclerView.Adapter <InfoDealAdapter.InfoViewHolder>{

    private Cursor mCursor;

    private Context mContext;

    private ActionMode mActionMode;

    private InfoDealFragment fragment; // используется для доступа к dataNotify

    private boolean multiSelect = false; // состояние "в ActionMode". Используется при выборе items
    private ArrayList<Integer> selectedItems = new ArrayList<Integer>(); // id выбранных для удалелния элементов

    InfoDealAdapter(InfoDealFragment fragment) {

        this.fragment = fragment;
    }

    // actionMode для удаления строк
    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {

        // вызывается при создании ActionMode
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            // установка состояния "in ActionMode" во фрагменте
            // испольлзуется для обновления адаптера и показа checkboxes
            fragment.setInActionMode(true);

            multiSelect = true;

            mode.getMenuInflater().inflate(R.menu.action_mode_menu, menu);

            return true;
        }

        // используется для взаимодействия с ActionMode во время работы
        // для сработки метода нужно вызвать myActionMode.invalidate();
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            // устанавливаю количество выбранных для удаления элементов
            mode.setTitle(mContext.getString(R.string.checked_act_mode) +
                    selectedItems.size() + mContext.getString(R.string.items_act_mode));

            return true;
        }

        // нажатия на кнопку (в данном случае удаления)
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            // копирую список удаляемых элементов для передачи в АлертДиалог
            final ArrayList<Integer> tempSelectedItems = new ArrayList<>(selectedItems);

            AlertDialog diaBox = new AlertDialog.Builder(mContext)
                    .setTitle(mContext.getResources().getString(R.string.title_deletion))
                    .setMessage(mContext.getResources().getString(R.string.tv_confirm_hint))
                    .setPositiveButton(mContext.getResources().getString(R.string.btn_df_ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    for (Integer intItem : tempSelectedItems) {

                                        String stringId = Integer.toString(intItem);

                                        Uri uri = fragment.getUri();

                                        uri = uri.buildUpon().appendPath(stringId).build();

                                        mContext.getContentResolver().delete(uri, null, null);
                                    }

                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(mContext.getResources().getString(R.string.btn_df_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                }
                            })
                    .create();

            diaBox.show();

            mode.finish();

            return true;
        }

        // срабатывает при закрытии ActionMode
        @Override
        public void onDestroyActionMode(ActionMode mode) {

            multiSelect = false;

            mActionMode = null;

            selectedItems.clear();

            fragment.setInActionMode(false);

            notifyDataSetChanged();
        }
    };

    @Override
    public InfoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        mContext = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_info_deal;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        InfoViewHolder viewHolder = new InfoViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(InfoViewHolder holder, int position) {

        int idIndex = mCursor.getColumnIndex(Contract.DealsEntry._ID);
        int portfolioIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_PORTFOLIO);
        int tickerIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_TICKER);
        int typeIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_TYPE);
        int dateIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_DATE);
        int priceIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_PRICE);
        int volumeIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_VOLUME);
        int feeIndex = mCursor.getColumnIndex(Contract.DealsEntry.COLUMN_FEE);

        mCursor.moveToPosition(position);

        final int id = mCursor.getInt(idIndex);
        String portfolio = mCursor.getString(portfolioIndex);
        String ticker = mCursor.getString(tickerIndex);
        String type = mCursor.getString(typeIndex);
        long dateInMillis = mCursor.getLong(dateIndex);
        int price = mCursor.getInt(priceIndex);
        int volume = mCursor.getInt(volumeIndex);
        int fee = mCursor.getInt(feeIndex);

        holder.itemView.setTag(id);

        String dateNormal = DateUtils.getNormalTimeForMoscow(dateInMillis);

        holder.bind(id, portfolio, ticker, type, dateNormal, price, volume, fee);

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

        LinearLayout llInfoItem;
        TextView tvInfoItemPortfolio;
        TextView tvInfoItemTicker;
        TextView tvInfoItemType;
        TextView tvInfoItemDate;
        TextView tvInfoItemPrice;
        TextView tvInfoItemVolume;
        TextView tvInfoItemCost;
        LinearLayout llRowInfo;
        CheckBox checkBox;

        InfoViewHolder(View itemView) {
            super(itemView);

            llInfoItem  = (LinearLayout) itemView.findViewById(R.id.ll_item_info_deal);
            tvInfoItemPortfolio = (TextView) itemView.findViewById(R.id.tv_portfolio);
            tvInfoItemTicker = (TextView) itemView.findViewById(R.id.tv_info_item_ticker);
            tvInfoItemType = (TextView) itemView.findViewById(R.id.tv_info_item_type);
            tvInfoItemDate = (TextView) itemView.findViewById(R.id.tv_info_item_date);
            tvInfoItemPrice = (TextView) itemView.findViewById(R.id.tv_info_item_price);
            tvInfoItemVolume = (TextView) itemView.findViewById(R.id.tv_info_item_volume);
            tvInfoItemCost = (TextView) itemView.findViewById(R.id.tv_info_item_cost);
            llRowInfo = (LinearLayout) itemView.findViewById(R.id.tr_info_deal);
            checkBox = (CheckBox) itemView.findViewById(R.id.chb_item_info_deal);
        }

        void bind(final Integer id,
                  String lPortfolio,
                  String lTicker,
                  String lType,
                  String lDate,
                  int lPrice,
                  int lVolume,
                  int lFee) {

            tvInfoItemPortfolio.setText(lPortfolio);
            tvInfoItemTicker.setText(lTicker);
            tvInfoItemType.setText(lType);
            tvInfoItemDate.setText(lDate);
            tvInfoItemPrice.setText(String.valueOf( (float) lPrice/MULTIPLIER_FOR_MONEY));
            tvInfoItemVolume.setText(String.valueOf(lVolume));

            // если список содержит id то помечаю item как отмеченный
            if (selectedItems.contains(id)) {

                llInfoItem.setBackgroundColor(Color.LTGRAY);

                checkBox.setChecked(true);

            } else {

                llInfoItem.setBackgroundColor(ResourcesCompat.
                        getColor(mContext.getResources(), android.R.color.background_light, null));

                checkBox.setChecked(false);
            }

            // если не в режиме "ActionMode" то скрываю checkboxes
            if (fragment.inActionMode)
                checkBox.setVisibility(View.VISIBLE);
            else checkBox.setVisibility(View.GONE);

            switch (lType) {
                case "Sell": {
                    llRowInfo.setBackgroundColor(ContextCompat.getColor(
                            itemView.getContext(), R.color.colorSell));

                    String string = "+" + String.valueOf((
                            (double) (lPrice * lVolume - lFee) / MULTIPLIER_FOR_MONEY));

                    tvInfoItemCost.setText(string);

                    break;
                }
                case "Buy": {
                    llRowInfo.setBackgroundColor(ContextCompat.getColor(
                            itemView.getContext(), R.color.colorBuy));

                            String string = "-" + String.valueOf(
                            (double) (lPrice * lVolume + lFee)/ MULTIPLIER_FOR_MONEY);

                    tvInfoItemCost.setText(string);

                    break;
                }
                case "Dividend": {
                    llRowInfo.setBackgroundColor(ContextCompat.getColor(
                            itemView.getContext(), R.color.colorDiv));

                    String string = "+" + String.valueOf(
                            (double) (lPrice * lVolume + lFee)/ MULTIPLIER_FOR_MONEY);

                    tvInfoItemCost.setText(string);
                }

                default: Log.d(TAG, "InfoDealAdapter.java, switch(lType) {default}");
            }

            // вход в режим "ActionMode" при долгом нажатии
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    mContext = v.getContext();

                    // если ActionMode уже запущен, то выхожу
                    if (mActionMode != null) {

                        selectItem(id);

                        return false;
                    }

                    // старт ActionMode
                    mActionMode = ((AppCompatActivity) mContext).
                            startSupportActionMode(actionModeCallbacks);

                    // оповещаю адаптер через фрагмент для показа checkboxes
                    fragment.notifyAdapter();

                    selectItem(id);

                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectItem(id);
                }
            });
        }

        // помещаю item в  список выбранных для удаления
        void selectItem(Integer item) {

            if (multiSelect) {
                if (selectedItems.contains(item)) {

                    selectedItems.remove(item);

                    mActionMode.invalidate();

                    llInfoItem.setBackgroundColor(ResourcesCompat.
                            getColor(mContext.getResources(), android.R.color.background_light, null));

                    checkBox.setChecked(false);
                } else {

                    selectedItems.add(item);

                    mActionMode.invalidate();

                    llInfoItem.setBackgroundColor(Color.LTGRAY);

                    checkBox.setChecked(true);
                }
            }
        }
    }

    // интерфейс, реализуемый в InfoInputFragment для обновления адаптера
    interface AdapterInterface {

        void notifyAdapter();

        void setInActionMode(boolean inActionMode);
    }
}
