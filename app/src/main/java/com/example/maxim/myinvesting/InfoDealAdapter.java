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

public class InfoDealAdapter extends RecyclerView.Adapter <InfoDealAdapter.InfoViewHolder>{

    private int mNumberItems;

    public InfoDealAdapter (int numberOfItems) {
        mNumberItems = numberOfItems;
    }

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

        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mNumberItems;
    }


    class InfoViewHolder extends RecyclerView.ViewHolder {

        TextView tvInfoItem;

        public InfoViewHolder(View itemView) {
            super(itemView);

            tvInfoItem = (TextView) itemView.findViewById(R.id.tv_info_item);
        }

        void bind(int listIndex) {
            tvInfoItem.setText(String.valueOf(listIndex));
        }
    }
}
