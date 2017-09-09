package com.example.mynote.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mynote.R;
import com.example.mynote.utils.ResourceParser.NoteBgResources;


public class BgSelectorAdapter extends RecyclerView.Adapter<BgSelectorAdapter.ViewHolder> {

    private Context mContext;

    public BgSelectorAdapter(Context context) {
        mContext = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.bg_selector_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.bgSelector_iv.setImageResource(NoteBgResources.getNoteListBgResource(position));
        holder.bgSelector_tv.setText(NoteBgResources.getNoteBgName(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return NoteBgResources.getBgResourcesCount();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bgSelector_iv;
        TextView bgSelector_tv;

        public ViewHolder(View itemView) {
            super(itemView);
            bgSelector_iv = (ImageView) itemView.findViewById(R.id.bg_selector_iv);
            bgSelector_tv = (TextView) itemView.findViewById(R.id.bg_selector_tv);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
}
