package com.example.maxim.myinvesting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by maxim on 09.04.17.
 */

public class InfoDealAdapter extends RecyclerView.Adapter<InfoDealAdapter.DealViewHolder> {

    private int mNumberItems;

    InfoDealAdapter(int numberOfItems) {
        mNumberItems = numberOfItems;
    }

    @Override
    public InfoDealAdapter.DealViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.item_info_deal;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToPatentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToPatentImmediately);
        DealViewHolder viewHolder = new DealViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(InfoDealAdapter.DealViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class DealViewHolder extends RecyclerView.ViewHolder{

        TextView tvInfoItem;

        public DealViewHolder(View itemView) {
            super(itemView);

            tvInfoItem = (TextView) itemView.findViewById(R.id.tv_info_item);
        }

        public void bind (int listIndex) {
            tvInfoItem.setText(String.valueOf(listIndex));
        }
    }
}
