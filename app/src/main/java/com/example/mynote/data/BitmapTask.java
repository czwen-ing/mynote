package com.example.mynote.data;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.example.mynote.bean.Note;
import com.example.mynote.utils.DataUtils;
import com.example.mynote.utils.ImageUtils;

public class BitmapTask extends AsyncTask<Note,Void,Bitmap>{
    private ImageView imageView;
    private Context context;
    private int width;
    public BitmapTask(Context context,ImageView imageView, int width){
        this.imageView = imageView;
        this.context = context;
        this.width = width;
    }
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected Bitmap doInBackground(Note... params) {
        Note note = params[0];
        Bitmap bitmap = DataUtils.getImageFromDB(context.getContentResolver(),note.getNoteId());
        return ImageUtils.scaleBitmapInSameSize(bitmap,width);
    }
}
