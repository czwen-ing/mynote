package com.example.mynote.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.mynote.data.Notes.NoteColumns;
import com.example.mynote.data.Notes.MediaColumns;
import com.example.mynote.data.Notes.FolderColumns;

public class NotesProvider extends ContentProvider {

    private NotesDatabaseHelper dbHelper;
    private static UriMatcher uriMatcher;
    private static final int URI_NOTE = 0;
    private static final int URI_NOTE_ITEM = 1;

    private static final int URI_MEDIA = 2;
    private static final int URI_MEDIA_ITEM = 3;

    private static final int URI_FOLDER = 4;
    private static final int URI_FOLDER_ITEM = 5;

    private static final int URI_SEARCH = 6;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Notes.AUTHORITY,"note", URI_NOTE);
        uriMatcher.addURI(Notes.AUTHORITY,"note/#", URI_NOTE_ITEM);
        uriMatcher.addURI(Notes.AUTHORITY,"media",URI_MEDIA);
        uriMatcher.addURI(Notes.AUTHORITY,"media/#",URI_MEDIA_ITEM);
        uriMatcher.addURI(Notes.AUTHORITY,"folder",URI_FOLDER);
        uriMatcher.addURI(Notes.AUTHORITY,"folder/#",URI_FOLDER_ITEM);
        uriMatcher.addURI(Notes.AUTHORITY, "search", URI_SEARCH);
    }
    public NotesProvider() {
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        dbHelper = new NotesDatabaseHelper(getContext());
        return true;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int delteteRows = 0;
        switch (uriMatcher.match(uri)){
            case URI_NOTE:
                db.delete(Notes.TABLE_NOTE,selection,selectionArgs);
                break;
            case URI_NOTE_ITEM:
                String id = uri.getPathSegments().get(1);
                delteteRows = db.delete(Notes.TABLE_NOTE, NoteColumns.ID + "=" + id
                        + parseSelection(selection), selectionArgs);
                break;
            case URI_FOLDER:
                db.delete(Notes.TABLE_FOLDER,selection,selectionArgs);
                break;
            case URI_FOLDER_ITEM:
                String folderId = uri.getPathSegments().get(1);
                delteteRows = db.delete(Notes.TABLE_FOLDER, FolderColumns.ID + "=" + folderId
                        + parseSelection(selection), selectionArgs);
                break;
            case URI_MEDIA:
                db.delete(Notes.TABLE_MEDIA,selection,selectionArgs);
                break;
            case URI_MEDIA_ITEM:
                String mediaId = uri.getPathSegments().get(1);
                delteteRows = db.delete(Notes.TABLE_MEDIA, MediaColumns.ID + "=" + mediaId
                        + parseSelection(selection), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return delteteRows;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri uriReturn = null;
        long newId = 0;
        switch (uriMatcher.match(uri)){
            case URI_NOTE:
                newId = db.insert(Notes.TABLE_NOTE,null,values);
                uriReturn = ContentUris.withAppendedId(uri,newId);
                break;
            case URI_MEDIA:
                newId = db.insert(Notes.TABLE_MEDIA,null,values);
                uriReturn = ContentUris.withAppendedId(uri,newId);
                break;
            case URI_FOLDER:
                newId = db.insert(Notes.TABLE_FOLDER,null,values);
                uriReturn = ContentUris.withAppendedId(uri,newId);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);

        }
        return uriReturn;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)){
            case URI_NOTE:
                cursor = db.query(Notes.TABLE_NOTE,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case URI_NOTE_ITEM:
                String id = uri.getPathSegments().get(1);
                cursor = db.query(Notes.TABLE_NOTE, projection, NoteColumns.ID + "=" + id
                        + parseSelection(selection), selectionArgs, null, null, sortOrder);
                break;
            case URI_MEDIA:
                cursor = db.query(Notes.TABLE_MEDIA,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case URI_MEDIA_ITEM:
                String mediaId = uri.getPathSegments().get(1);
                cursor = db.query(Notes.TABLE_MEDIA, projection, MediaColumns.ID + "=" + mediaId
                        + parseSelection(selection), selectionArgs, null, null, sortOrder);
                break;

            case URI_FOLDER:
                cursor = db.query(Notes.TABLE_FOLDER,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case URI_FOLDER_ITEM:
                String folderId = uri.getPathSegments().get(1);
                cursor = db.query(Notes.TABLE_FOLDER, projection, MediaColumns.ID + "=" + folderId
                        + parseSelection(selection), selectionArgs, null, null, sortOrder);
                break;
            case URI_SEARCH:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        int count = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)){
            case URI_NOTE:
                count = db.update(Notes.TABLE_NOTE,values,selection,selectionArgs);
                break;
            case URI_NOTE_ITEM:
                String id = uri.getPathSegments().get(1);
                count = db.update(Notes.TABLE_NOTE,values,NoteColumns.ID + "=" + id
                        + parseSelection(selection),selectionArgs);
                break;
            case URI_MEDIA:
                count = db.update(Notes.TABLE_MEDIA,values,selection,selectionArgs);
                break;
            case URI_MEDIA_ITEM:
                String mediaId = uri.getPathSegments().get(1);
                count = db.update(Notes.TABLE_MEDIA,values,MediaColumns.ID + "=" + mediaId
                        + parseSelection(selection),selectionArgs);
                break;
            case URI_FOLDER:
                count = db.update(Notes.TABLE_FOLDER,values,selection,selectionArgs);
                break;
            case URI_FOLDER_ITEM:
                String folderId = uri.getPathSegments().get(1);
                count = db.update(Notes.TABLE_FOLDER,values,MediaColumns.ID + "=" + folderId
                        + parseSelection(selection),selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);

        }
        return count;
    }

    private void increaseNoteVersion(long id, String selection, String[] selectionArgs) {
        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ");
        sql.append(Notes.TABLE_NOTE);
        sql.append(" SET ");
        sql.append(NoteColumns.VERSION);
        sql.append("=" + NoteColumns.VERSION + "+1 ");

        if (id > 0 || !TextUtils.isEmpty(selection)) {
            sql.append(" WHERE ");
        }
        if (id > 0) {
            sql.append(NoteColumns.ID + "=").append(String.valueOf(id));
        }
        if (!TextUtils.isEmpty(selection)) {
            String selectString = id > 0 ? parseSelection(selection) : selection;
            for (String args : selectionArgs) {
                selectString = selectString.replaceFirst("\\?", args);
            }
            sql.append(selectString);
        }

        dbHelper.getWritableDatabase().execSQL(sql.toString());
    }
    private String parseSelection(String selection) {
        return (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
    }

}
