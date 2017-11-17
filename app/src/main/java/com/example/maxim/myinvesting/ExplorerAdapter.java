package com.example.maxim.myinvesting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 16.11.17.
 */

public class ExplorerAdapter extends RecyclerView.Adapter<ExplorerAdapter.ExplorerViewHolder> {

    private String [] folderContent;

    private Context context;

    @Override
    public ExplorerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();

        int layoutIdForListItem = R.layout.item_explorer;

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        boolean shouldAttachToParentImmediately = false;

        View view = layoutInflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        ExplorerViewHolder viewHolder = new ExplorerViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ExplorerAdapter.ExplorerViewHolder holder, int position) {

        String string = folderContent[position];

        holder.bind(string);
    }

    @Override
    public int getItemCount() {

        if (folderContent == null)
            return 0;

        return folderContent.length;
    }

    void swap(String [] folderContent) {

        if (this.folderContent == folderContent)
            return;

        this.folderContent = folderContent;

        if (folderContent != null) {

            this.notifyDataSetChanged();
        }
    }

    class ExplorerViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        ExplorerViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.tv_item_explorer);
        }

        void bind(final String string) {

            textView.setText(string);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ((Explorer) context).setPath(string);
                }
            });
        }
    }
}