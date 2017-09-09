package com.example.mynote.utils;


import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.example.mynote.bean.Folder;
import com.example.mynote.bean.Note;
import com.example.mynote.data.Notes;
import com.example.mynote.data.Notes.NoteColumns;
import com.example.mynote.data.Notes.FolderColumns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static com.example.mynote.R.drawable.notes;

public class DataUtils {

    public static final String TAG = "DataUtils";
    public static final String[] NOTE_PROJECTION = new String[]{
            NoteColumns.ID,
            NoteColumns.PARENT_ID,
            NoteColumns.BG_ID,
            NoteColumns.CREATED_DATE,
            NoteColumns.MODIFIED_DATE,
            NoteColumns.CONTENT,
            NoteColumns.ALARM_DATE
    };

    public static void deleteBatchNotes(ContentResolver resolver, HashSet<Long> ids){

        if (ids == null) {
            Log.d(TAG, "the ids is null");
            return ;
        }
        if (ids.size() == 0) {
            Log.d(TAG, "no id is in the hashset");
            return ;
        }
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (long id : ids){
            ContentProviderOperation.Builder builder = ContentProviderOperation
                    .newDelete(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI,id));
            operations.add(builder.build());

            resolver.delete(Notes.CONTENT_MEDIA_URI, Notes.MediaColumns.NOTE_ID + "=?", new String[]{
                    String.valueOf(id)});
        }
        try {
            ContentProviderResult[] results = resolver.applyBatch(Notes.AUTHORITY,operations);
            if (results.length == 0 || results[0] == null) {
                Log.d(TAG, "delete notes failed, ids:" + ids.toString());
            }
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public static void deleteNote(ContentResolver resolver, int noteId){

        int i = resolver.delete(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI,noteId),null,null);

        int j = resolver.delete(Notes.CONTENT_MEDIA_URI, Notes.MediaColumns.NOTE_ID + "=?", new String[]{
                String.valueOf(noteId)});

        if (i < 0){
            Log.d(TAG, "delete notes failed, id:" + noteId);
        }
        if (j < 0){
            Log.d(TAG, "delete images failed, id:" + noteId);
        }
    }

    public static String getContentById(ContentResolver resolver, int noteId) {
        Cursor cursor = resolver.query(Notes.CONTENT_NOTE_URI,
                new String [] { NoteColumns.CONTENT },
                NoteColumns.ID + "=?",
                new String [] { String.valueOf(noteId)},
                null);

        if (cursor != null) {
            String content = "";
            if (cursor.moveToFirst()) {
                content = cursor.getString(0);
            }
            cursor.close();
            return content;
        }
        throw new IllegalArgumentException("Note is not found with id: " + noteId);
    }

    public static boolean removeImageFromDB(ContentResolver resolver, int noteId,int index){
        int count = resolver.delete(Notes.CONTENT_MEDIA_URI, Notes.MediaColumns.NOTE_ID + "=?"
                + " AND " + Notes.MediaColumns.IMAGE_INDEX + "=?", new String[]{
                String.valueOf(noteId),String.valueOf(index)});
        return count > -1;
    }

    public static boolean addImageToDB(ContentResolver resolver,int noteId,int index,String imagePath,byte[] bmpBlob){
        ContentValues values = new ContentValues();
        values.put(Notes.MediaColumns.NOTE_ID, noteId);
        values.put(Notes.MediaColumns.IMAGE_INDEX,index);
        values.put(Notes.MediaColumns.IMAGE_PATH,imagePath);
        values.put(Notes.MediaColumns.IMAGE_BLOB,bmpBlob);
        Uri uri = resolver.insert(Notes.CONTENT_MEDIA_URI,values);
        //判断位置之后是否有图片，有的话将所有图片向后移动一

        return uri != null;
    }

    public static boolean existInNoteDatabase(ContentResolver resolver, long noteId, long parentId) {
        Cursor cursor = resolver.query(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId),
                new String[]{NoteColumns.PARENT_ID}, NoteColumns.PARENT_ID+"="+parentId, null, null);

        boolean exist = false;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                exist = true;
            }
            cursor.close();
        }
        return exist;
    }

    public static boolean batchMoveToFolder(ContentResolver resolver, HashSet<Long> ids, long folderId) {
        if (ids == null) {
            Log.d(TAG, "the ids is null");
            return true;
        }

        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        for (long id : ids) {
            Uri uri = ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, id);
            if (existInNoteDatabase(resolver,id,folderId)){
                Log.d("TAG","此id"+ id + "已存在便签夹"+ folderId+"中");
                continue;
            }
            ContentProviderOperation.Builder builder = ContentProviderOperation
                    .newUpdate(uri);
            builder.withValue(NoteColumns.PARENT_ID, folderId);
            operationList.add(builder.build());

        }

        try {
            ContentProviderResult[] results = resolver.applyBatch(Notes.AUTHORITY, operationList);
            if (results.length == 0 || results[0] == null) {
                Log.d(TAG, "移动便签失败, ids:" + ids.toString());
                return false;
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Folder> getFoldersFromDB(ContentResolver resolver){
        Cursor cursor = resolver.query(Notes.CONTENT_FOLDER_URI,null,null,null,null);
        List<Folder> folders = new ArrayList<>();
        if (cursor != null){
            while (cursor.moveToNext()) {
                Folder folder = new Folder();
                folder.setId(cursor.getLong(cursor.getColumnIndex(FolderColumns.ID)));
                folder.setNoteCounts(cursor.getInt(cursor.getColumnIndex(FolderColumns.NOTES_COUNT)));
                Log.e("TAG"," folder notes folders NOTES_COUNT"+ folder.getNoteCounts());
                folder.setName(cursor.getString(cursor.getColumnIndex(FolderColumns.FOLDER_NAME)));
                folders.add(folder);
            }
            cursor.close();
        } else {
            return null;
        }
        return folders;
    }

    public static boolean checkVisibleFolderName(ContentResolver resolver, String name) {
        Cursor cursor = resolver.query(Notes.CONTENT_FOLDER_URI, null,
                FolderColumns.FOLDER_NAME + "=?",
                new String[] { name }, null);
        boolean exist = false;
        if(cursor != null) {
            if(cursor.getCount() > 0) {
                exist = true;
            }
            cursor.close();
        }
        return exist;
    }

    /**
     * 从数据库中获取所有便签
     */
    public static List<Note> getNotesFromDataBase(ContentResolver resolver
            ,Uri uri,String selection , String[] selectionArgs,final int sortWay) {
        List<Note> notes = new ArrayList<>();
        Cursor cursor = resolver.query(uri, NOTE_PROJECTION, selection, selectionArgs, null);

        if (cursor != null) {
            Log.e("TAG"," folder cursor.getCount()"+ cursor.getCount());
            while (cursor.moveToNext()) {
                Note note = new Note();
                note.setNoteId(cursor.getInt(cursor.getColumnIndex(NoteColumns.ID)));
                note.setParentId(cursor.getLong(cursor.getColumnIndex(NoteColumns.PARENT_ID)));
                note.setBgId(cursor.getInt(cursor.getColumnIndex(NoteColumns.BG_ID)));
                note.setCreatedTime(cursor.getLong(cursor.getColumnIndex(NoteColumns.CREATED_DATE)));
                note.setModifiedTime(cursor.getLong(cursor.getColumnIndex(NoteColumns.MODIFIED_DATE)));
                note.setContent(cursor.getString(cursor.getColumnIndex(NoteColumns.CONTENT)));
                note.setAlarmDate(cursor.getLong(cursor.getColumnIndex(NoteColumns.ALARM_DATE)));
                notes.add(note);
            }
            Collections.sort(notes, new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {//按时间的长短排序，长的在前
                    if (sortWay == ResourceParser.SORT_BY_CREATE_DATE){
                        if (o1.getCreatedTime() > o2.getCreatedTime()) {
                            return -1;
                        } else if (o1.getCreatedTime() > o2.getCreatedTime()) {
                            return 0;
                        } else {
                            return 1;
                        }
                    } else {
                        if (o1.getModifiedTime() > o2.getModifiedTime()) {
                            return -1;
                        } else if (o1.getModifiedTime() > o2.getModifiedTime()) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                }
            });
            cursor.close();
        }else {
            return null;
        }
        return notes;
    }

    public static Bitmap getImageFromDB(ContentResolver resolver,long noteId) {
        Bitmap bitmap = null;
        Cursor cursor = resolver.query(Notes.CONTENT_MEDIA_URI,
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
}
