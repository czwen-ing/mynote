package com.example.mynote.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mynote.bean.Note;
import com.example.mynote.R;
import com.example.mynote.data.Notes;
import com.example.mynote.utils.DataUtils;
import com.example.mynote.utils.ImageUtils;
import com.example.mynote.utils.ResourceParser;
import com.example.mynote.utils.ResourceParser.NoteBgResources;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.TwoSpanViewHolder> {
    private Context mContext;
    private List<Note> notes;
    private boolean longClickable = true;
    private boolean choiceMode = false;
    private HashMap<Integer,Boolean> mSelectedItems;
    private int sortWay;
    private int type = 0;
    private int size;

    public NoteRecyclerAdapter(Context context, List<Note> notes,int sortWay) {
        mContext = context;
        this.notes = notes;
        this.sortWay = sortWay;
        mSelectedItems = new HashMap<>();
    }

    public NoteRecyclerAdapter(Context context, List<Note> notes,int sortWay,int type) {
        mContext = context;
        this.notes = notes;
        this.sortWay = sortWay;
        mSelectedItems = new HashMap<>();
        this.type = type;
    }

    @Override
    public int getItemViewType(int position) {
        if (type == 0){
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public TwoSpanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0){
            return new TwoSpanViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.note_item, parent, false));
        } else if (viewType == 1){
            return new TwoSpanViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.note_item2, parent, false));
        }

        return null;
    }


    @Override
    public void onBindViewHolder(final TwoSpanViewHolder holder,int position) {
        final Note note = notes.get(position);
        holder.contentText.setText(note.getContent());
        Bitmap bitmap = DataUtils.getImageFromDB(mContext.getContentResolver(),note.getNoteId());
        if (bitmap != null){
            int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
            int height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
            holder.viewGroup.measure(width,height);
            if (type == 0){
                if (size == 0){
                    size = holder.viewGroup.getMeasuredWidth();
                }
            } else {
                if (size == 0){
                    size = holder.viewGroup.getMeasuredHeight();
                }
            }
            Log.e("TAG"," itemView size   " + size);
            holder.imageView.setImageBitmap(ImageUtils.scaleBitmapInSameSize(bitmap,size));
            holder.imageView.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setVisibility(View.GONE);
            holder.itemView.setBackgroundResource(NoteBgResources.getNoteListBgResource(note.getBgId()));
        }
        if (sortWay == ResourceParser.SORT_BY_CREATE_DATE){
            holder.timeText.setText(getCurrentDateAndTime(note.getCreatedTime()));
        } else {
            holder.timeText.setText(getCurrentDateAndTime(note.getModifiedTime()));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, holder.getAdapterPosition(), note.getNoteId());
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickable && mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(v, holder.getAdapterPosition(),note.getNoteId());
                    return true;
                } else {
                    return false;
                }
            }
        });
        if (choiceMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(isSelectedItem(position));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        if (note.getAlarmDate() > System.currentTimeMillis()){
            holder.clockView.setVisibility(View.VISIBLE);
        } else {
            holder.clockView.setVisibility(View.GONE);
        }

    }

    private String getCurrentDateAndTime(long currentTime) {
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        Date date = new Date(currentTime);
        String dateStr = dateFormat.format(date);
        String timeStr = timeFormat.format(date);
        if (dateStr.equals(dateFormat.format(new Date(System.currentTimeMillis())))) {
            return timeStr;
        } else {
            return dateStr + timeStr;
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }



    class TwoSpanViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ViewGroup viewGroup;
        TextView contentText;
        TextView timeText;
        CheckBox checkBox;
        ImageView clockView;

        public TwoSpanViewHolder(View itemView) {
            super(itemView);
            viewGroup = (ViewGroup) itemView.findViewById(R.id.note_ll);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            contentText = (TextView) itemView.findViewById(R.id.content_text);
            timeText = (TextView) itemView.findViewById(R.id.time_text);
            checkBox = (CheckBox) itemView.findViewById(R.id.check_box);
            clockView = (ImageView) itemView.findViewById(R.id.clock_iv);
        }
    }


    /**
     * 设置是否可以长按
     * @param longClickable
     */
    public void setLongClickable(boolean longClickable) {
        this.longClickable = longClickable;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, int id);
    }

    private OnItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position,int id);
    }

    private OnItemLongClickListener mOnItemLongClickListener = null;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    /**
     * 设置是否进入选择删除模式
     * @param choiceMode
     */
    public void setChoiceMode(boolean choiceMode) {
        mSelectedItems.clear();
        this.choiceMode = choiceMode;
    }

    public boolean isInChoiceMode() {
        return choiceMode;
    }

    /**
     * 将被选中的Item放入mSelectedItems健值集合中
     * @param position
     * @param checked
     */
    public void setCheckItem(int position, boolean checked){
        mSelectedItems.put(position,checked);
        notifyDataSetChanged();
    }

    /**
     * 判断在mSelectedItems是否包含所选的Item，判断是否被选中
     * @param position
     * @return
     */
    public boolean isSelectedItem(int position){
        if (null == mSelectedItems.get(position)) {
            return false;
        }
        return mSelectedItems.get(position);
    }

    /**
     * 全选
     * @param isAllSelectec
     */
    public void selectAll(boolean isAllSelectec) {
        for (int i = 0; i < notes.size(); i++){
            setCheckItem(i,isAllSelectec);
        }
    }

    public int getSelectedCount(){
        Collection<Boolean> values = mSelectedItems.values();
        Iterator<Boolean> iter = values.iterator();
        int count = 0;
        while (iter.hasNext()) {
            if (iter.next()) {
                count++;
            }
        }
        return count;
    }

    public boolean isAllSelected(){
        int selectedCount = getSelectedCount();
        return selectedCount != 0 && selectedCount == notes.size();
    }

    /**
     * 获取被选中的Item的NoteId，用以从数据库中删除数据
     * @return
     */
    public HashSet<Long> getSelectedItemIds() {
        HashSet<Long> itemsIds = new HashSet<>();
        for (Integer position : mSelectedItems.keySet()){
            if (mSelectedItems.get(position)){
                long id = getItemId(position);
                itemsIds.add(id);
            }
        }
        return itemsIds;
    }

    @Override
    public long getItemId(int position) {
        return notes.get(position).getNoteId();
    }

    private Bitmap getImageFromDB(long noteId) {
        Bitmap bitmap = null;
        Cursor cursor = mContext.getContentResolver().query(Notes.CONTENT_MEDIA_URI,
                new String[]{Notes.MediaColumns.IMAGE_BLOB},
                Notes.MediaColumns.NOTE_ID + "=?", new String[]{
                        String.valueOf(noteId)}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                byte[] bmpBlob = cursor.getBlob(cursor.getColumnIndex(Notes.MediaColumns.IMAGE_BLOB));
                bitmap = BitmapFactory.decodeByteArray(bmpBlob,0,bmpBlob.length);
            }
            cursor.close();
        } else {
            throw new IllegalArgumentException("未能找到此Id的数据" + noteId);
        }
        return bitmap;
    }

    public void setItemView(int type){
        this.type = type;

    }


}
