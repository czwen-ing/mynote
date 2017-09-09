package com.example.mynote.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mynote.R;
import com.example.mynote.bean.Folder;

import java.util.List;

public class FoldersListAdapter extends BaseAdapter{
    private Context context;
    private List<Folder> folders;
    private boolean inDialog;

    public FoldersListAdapter(Context context, List<Folder> folders){
        this.context = context;
        this.folders = folders;
    }
    public FoldersListAdapter(Context context, List<Folder> folders, boolean inDialog){
        this.context = context;
        this.folders = folders;
        this.inDialog = inDialog;
    }


    @Override
    public int getCount() {
        return folders.size();
    }

    @Override
    public Object getItem(int position) {
        return folders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return folders.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.folder_item,parent,false);
            viewHolder.folderName = (TextView) convertView.findViewById(R.id.folder_name);
            viewHolder.notesCount = (TextView) convertView.findViewById(R.id.notes_count);
            if (!inDialog){
                convertView.setBackgroundResource(R.drawable.folder);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.folderName.setText(folders.get(position).getName());
        viewHolder.notesCount.setText("("+folders.get(position).getNoteCounts()+")");
        return convertView;
    }

    private class ViewHolder{
        TextView folderName;
        TextView notesCount;
    }
}
