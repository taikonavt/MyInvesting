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
import com.example.maxim.myinvesting.utilities.MyApp;

import java.util.ArrayList;

import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;
import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 16.05.17.
 */

public class InfoInputAdapter extends RecyclerView.Adapter <InfoInputAdapter.InfoViewHolder> {

    private Context mContext;

    private Cursor mCursor;

    private ActionMode mActionMode;
    private InfoInputFragment fragment; // используется для доступа к dataNotify

    private boolean multiSelect = false; // состояние "в ActionMode". Используется при выборе items
    private ArrayList<Integer> selectedItems = new ArrayList<Integer>(); // id выбранных для удалелния элементов

    InfoInputAdapter(InfoInputFragment fragment) {

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
    public InfoInputAdapter.InfoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        mContext = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_info_input;
        LayoutInflater inflater = LayoutInflater.from(mContext);
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

        holder.bind(id, type, dateNormal, amount, currency, fee, portfolio, note);

        // TODO: 15.04.17 do rotation: when vert - few piece of info, when horiz - all info is seen
    }

    // возвращает количество создаваемых элементов
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

        LinearLayout llInfoInputItem;
        TextView tvInfoInputItemType;
        TextView tvInfoInputItemDate;
        TextView tvInfoInputItemAmount;
        TextView tvInfoInputItemCurrency;
        TextView tvInfoInputItemFee;
        TextView tvInfoInputItemPortfolio;
        TextView tvInfoInputItemNote; // TODO: 16.05.17 Добавить Note для горизонт. экрана
        LinearLayout llRowInput;
        CheckBox checkBox;

        InfoViewHolder(View itemView) {
            super(itemView);

            llInfoInputItem = (LinearLayout) itemView.findViewById(R.id.ll_item_info_input);
            tvInfoInputItemType = (TextView) itemView.findViewById(R.id.tv_info_input_item_type);
            tvInfoInputItemDate = (TextView) itemView.findViewById(R.id.tv_info_input_item_date);
            tvInfoInputItemAmount = (TextView) itemView.
                    findViewById(R.id.tv_info_input_item_amount);
            //tvInfoInputItemCurrency = (TextView) itemView.findViewById(R.id.tv_info_input_item_currency);
            tvInfoInputItemFee = (TextView) itemView.findViewById(R.id.tv_info_input_item_fee);
            tvInfoInputItemPortfolio = (TextView) itemView.
                    findViewById(R.id.tv_info_input_item_portfolio);
            llRowInput = (LinearLayout) itemView.findViewById(R.id.ll_item_info_input_row);
            checkBox = (CheckBox) itemView.findViewById(R.id.chb_item_info_input);
        }

        void bind(final Integer id,
                  String lType,
                  String lDate,
                  long lAmount,
                  String lCurrency,
                  int lFee,
                  int lPortfolio,
                  String lNote) {

            // если список содержит id то помечаю item как отмеченный
            if (selectedItems.contains(id)) {

                llInfoInputItem.setBackgroundColor(Color.LTGRAY);

                checkBox.setChecked(true);

            } else {

                llInfoInputItem.setBackgroundColor(ResourcesCompat.
                        getColor(mContext.getResources(), android.R.color.background_light, null));

                checkBox.setChecked(false);
            }

            tvInfoInputItemType.setText(lType);
            tvInfoInputItemDate.setText(lDate);

            String temp = "- " + String.valueOf(lFee/MULTIPLIER_FOR_MONEY) + " " + lCurrency;
            tvInfoInputItemFee.setText(temp);
            tvInfoInputItemPortfolio.setText(String.valueOf(lPortfolio));

            // если не в режиме "ActionMode" то скрываю checkboxes
            if (fragment.inActionMode)
                checkBox.setVisibility(View.VISIBLE);
            else checkBox.setVisibility(View.GONE);

            // если ввод то обозначаю item зеленым, если вывод то красным
            String[] inputType = MyApp.getAppContext().getResources().getStringArray(R.array.spinType_input_array);

            if (lType.equals(inputType[1])) {
                llRowInput.setBackgroundColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.colorSell));

                String string = "-" + String.valueOf(lAmount / MULTIPLIER_FOR_MONEY) +
                        " " + lCurrency;

                tvInfoInputItemAmount.setText(string);
            }
            else if (lType.equals(inputType[0])){
                llRowInput.setBackgroundColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.colorBuy));

                String string = "+" + String.valueOf(lAmount / MULTIPLIER_FOR_MONEY) +
                        " " + lCurrency;

                tvInfoInputItemAmount.setText(string);
            }
            else if (lType.equals(inputType[2])) {

                llRowInput.setBackgroundColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.colorDiv));

                String string = String.valueOf(lAmount / MULTIPLIER_FOR_MONEY) +
                        " " + lCurrency;

                tvInfoInputItemAmount.setText(string);
            }
            else {
                llRowInput.setBackgroundColor(0);
                Log.d(TAG, "InfoDealAdapter.java, switch(lType) {default}");
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

                    llInfoInputItem.setBackgroundColor(ResourcesCompat.
                            getColor(mContext.getResources(),
                                    android.R.color.background_light, null));

                    checkBox.setChecked(false);
                } else {

                    selectedItems.add(item);

                    mActionMode.invalidate();

                    llInfoInputItem.setBackgroundColor(Color.LTGRAY);

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