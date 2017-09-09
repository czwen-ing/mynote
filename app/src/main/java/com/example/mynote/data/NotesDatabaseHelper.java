package com.example.mynote.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mynote.data.Notes.NoteColumns;
import com.example.mynote.data.Notes.MediaColumns;
import com.example.mynote.data.Notes.FolderColumns;


public class NotesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "notes.db";
    private static final int DB_VERSION = 4;

    private static final String CREATE_NOTE_TABLE =
            "CREATE TABLE " + Notes.TABLE_NOTE + "(" +
                    NoteColumns.ID + " INTEGER PRIMARY KEY," +
                    NoteColumns.PARENT_ID + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.BG_ID + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.CREATED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
                    NoteColumns.MODIFIED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
                    NoteColumns.ALARM_DATE + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.TEXT_LENGTH + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.LOCATION + " TEXT NOT NULL DEFAULT ''," +
                    NoteColumns.CONTENT + " TEXT NOT NULL DEFAULT ''"   +
                    ")";
    private static final String CREATE_MEDIA_TABLE =
            "CREATE TABLE " + Notes.TABLE_MEDIA + "(" +
                    MediaColumns.ID + " INTEGER PRIMARY KEY," +
                    MediaColumns.NOTE_ID + " INTEGER NOT NULL DEFAULT 0," +
                    MediaColumns.IMAGE_INDEX + " INTEGER NOT NULL DEFAULT 0," +
                    MediaColumns.IMAGE_PATH + " TEXT NOT NULL DEFAULT '',"   +
                    MediaColumns.IMAGE_BLOB + " BLOB NOT NULL "   +
                    ")";

    private static final String CREATE_FOLDER_TABLE =
            "CREATE TABLE " + Notes.TABLE_FOLDER + "(" +
                    FolderColumns.ID + " INTEGER PRIMARY KEY," +
                    FolderColumns.NOTES_COUNT + " INTEGER NOT NULL DEFAULT 0," +
                    FolderColumns.FOLDER_NAME + " TEXT NOT NULL DEFAULT ''" +
                    ")";

    private static final String NOTE_DELETE_MEDIA_ON_DELETE_NOTE_TRIGGER =
            "CREATE TRIGGER delete_media_on_delete_note" +
                    " AFTER DELETE ON " + Notes.TABLE_NOTE+
                    " BEGIN " +
                    "  DELETE FROM " + Notes.TABLE_MEDIA +
                    "  WHERE " + MediaColumns.NOTE_ID + "=old." + NoteColumns.ID +";" +
                    " END";

    private static final String NOTE_DECREASE_MEDIA_INDEX_ON_DELETE_TRIGGER =
            "CREATE TRIGGER decrease_media_index_on_delete " +
                    " AFTER DELETE ON " + Notes.TABLE_MEDIA+
                    " BEGIN " +
                    "  UPDATE " + Notes.TABLE_MEDIA +
                    "   SET " + MediaColumns.IMAGE_INDEX + "=" + MediaColumns.IMAGE_INDEX + "-1" +
                    "  WHERE " + MediaColumns.NOTE_ID + "=old." + MediaColumns.NOTE_ID +
                    "  AND " + MediaColumns.IMAGE_INDEX + ">=old."+MediaColumns.IMAGE_INDEX+";" +
                    " END";

    private static final String NOTE_INCREASE_MEDIA_INDEX_ON_INSERT_TRIGGER =
            "CREATE TRIGGER increase_media_index_on_insert " +
                    " BEFORE INSERT ON " + Notes.TABLE_MEDIA+
                    " BEGIN " +
                    "  UPDATE " + Notes.TABLE_MEDIA +
                    "   SET " + MediaColumns.IMAGE_INDEX + "=" + MediaColumns.IMAGE_INDEX + "+1" +
                    "  WHERE " + MediaColumns.NOTE_ID + "=new." + MediaColumns.NOTE_ID +
                    "  AND " + MediaColumns.IMAGE_INDEX + ">=new."+MediaColumns.IMAGE_INDEX+";" +
                    " END";

    private static final String NOTE_INCREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER =
            "CREATE TRIGGER increase_folder_count_on_update "+
                    " AFTER UPDATE OF " + NoteColumns.PARENT_ID + " ON " + Notes.TABLE_NOTE +
                    " BEGIN " +
                    "  UPDATE " + Notes.TABLE_FOLDER +
                    "   SET " + FolderColumns.NOTES_COUNT + "=" + FolderColumns.NOTES_COUNT + " + 1" +
                    "  WHERE " + FolderColumns.ID + "=new." + NoteColumns.PARENT_ID + ";" +
                    " END";

    private static final String NOTE_DECREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER =
            "CREATE TRIGGER decrease_folder_count_on_update " +
                    " AFTER UPDATE OF " + NoteColumns.PARENT_ID + " ON " + Notes.TABLE_NOTE +
                    " BEGIN " +
                    "  UPDATE " + Notes.TABLE_FOLDER +
                    "   SET " + FolderColumns.NOTES_COUNT + "=" + FolderColumns.NOTES_COUNT + "-1" +
                    "  WHERE " + FolderColumns.ID + "=old." + NoteColumns.PARENT_ID +
                    "  AND " + FolderColumns.NOTES_COUNT + ">0" + ";" +
                    " END";

    private static final String NOTE_DECREASE_FOLDER_COUNT_ON_DELETE_TRIGGER =
            "CREATE TRIGGER decrease_folder_count_on_delete " +
                    " AFTER DELETE ON " + Notes.TABLE_NOTE +
                    " BEGIN " +
                    "  UPDATE " + Notes.TABLE_FOLDER +
                    "   SET " + FolderColumns.NOTES_COUNT + "=" + FolderColumns.NOTES_COUNT + "-1" +
                    "  WHERE " + FolderColumns.ID + "=old." + NoteColumns.PARENT_ID +
                    "  AND " + FolderColumns.NOTES_COUNT + ">0;" +
                    " END";

    private static final String NOTE_INCREASE_FOLDER_COUNT_ON_INSERT_TRIGGER =
            "CREATE TRIGGER increase_folder_count_on_insert " +
                    " AFTER INSERT ON " + Notes.TABLE_NOTE +
                    " BEGIN " +
                    "  UPDATE " + Notes.TABLE_FOLDER +
                    "   SET " + FolderColumns.NOTES_COUNT + "=" + FolderColumns.NOTES_COUNT + " + 1" +
                    "  WHERE " + FolderColumns.ID + "=new." + NoteColumns.PARENT_ID + ";" +
                    " END";

    public NotesDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_NOTE_TABLE);
        db.execSQL(CREATE_MEDIA_TABLE);
        db.execSQL(CREATE_FOLDER_TABLE);
        db.execSQL(NOTE_DELETE_MEDIA_ON_DELETE_NOTE_TRIGGER);
        db.execSQL(NOTE_DECREASE_MEDIA_INDEX_ON_DELETE_TRIGGER);
        db.execSQL(NOTE_INCREASE_MEDIA_INDEX_ON_INSERT_TRIGGER);
        db.execSQL(NOTE_INCREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER);
        db.execSQL(NOTE_DECREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER);
        db.execSQL(NOTE_DECREASE_FOLDER_COUNT_ON_DELETE_TRIGGER);
        db.execSQL(NOTE_INCREASE_FOLDER_COUNT_ON_INSERT_TRIGGER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
