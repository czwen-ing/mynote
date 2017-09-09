package com.example.mynote.data;


import android.net.Uri;

public class Notes {

    public static final String PREFERENCES_FONT_SIZE = "font_size";
    public static final String PREFERENCES_SORT_WAY = "sort_way";
    public static final String PREFERENCES_QUICK_DELETE = "quick——delete";
    public static final String PREFERENCES_SHOW_TEXT_LENGTH = "show_text_length";

    public static final int RESULT_DELETE = 2;
    public static final int RESULT_NEW = 3;
    public static final int RESULT_MODIFIED = 4;
    public static final String TABLE_NOTE = "note";
    public static final String TABLE_MEDIA = "media";
    public static final String TABLE_FOLDER = "folder";
    public static final String AUTHORITY = "com.example.mynote.notesprovider";
    public static final Uri CONTENT_NOTE_URI = Uri.parse("content://" + AUTHORITY + "/note");
    public static final Uri CONTENT_MEDIA_URI = Uri.parse("content://" + AUTHORITY + "/media");
    public static final Uri CONTENT_FOLDER_URI = Uri.parse("content://" + AUTHORITY + "/folder");

    public static class NoteColumns {
        /**
         * The unique ID for a row
         * <P> Type: INTEGER (long) </P>
         */
        public static final String ID = "_id";

        /**
         * The parent's id for note or folder
         * <P> Type: INTEGER (long) </P>
         */
        public static final String PARENT_ID = "parent_id";

        /**
         * Created data for note or folder
         * <P> Type: INTEGER (long) </P>
         */
        public static final String CREATED_DATE = "created_date";

        /**
         * Latest modified date
         * <P> Type: INTEGER (long) </P>
         */
        public static final String MODIFIED_DATE = "modified_date";


        /**
         * Alert date
         * <P> Type: INTEGER (long) </P>
         */
        public static final String ALARM_DATE = "alarm_date";


        /**
         * Note's widget id
         * <P> Type: INTEGER (long) </P>
         */
        public static final String WIDGET_ID = "widget_id";

        /**
         * Note's widget type
         * <P> Type: INTEGER (long) </P>
         */
        public static final String WIDGET_TYPE = "widget_type";

        /**
         * Note's background color's id
         * <P> Type: INTEGER (long) </P>
         */
        public static final String BG_ID = "bg_id";

        /**
         * For text note, it doesn't has attachment, for multi-media
         * note, it has at least one attachment
         * <P> Type: INTEGER </P>
         */
        public static final String HAS_ATTACHMENT = "has_attachment";


        /**
         * The file type: folder or note
         * <P> Type: INTEGER </P>
         */
        public static final String TYPE = "type";

        /**
         * The last sync id
         * <P> Type: INTEGER (long) </P>
         */
        public static final String SYNC_ID = "sync_id";

        /**
         * Sign to indicate local modified or not
         * <P> Type: INTEGER </P>
         */
        public static final String LOCAL_MODIFIED = "local_modified";

        /**
         * Original parent id before moving into temporary folder
         * <P> Type : INTEGER </P>
         */
        public static final String ORIGIN_PARENT_ID = "origin_parent_id";

        /**
         * The version code
         * <P> Type : INTEGER (long) </P>
         */
        public static final String VERSION = "version";

        /**
         * Data's content
         * <P> Type: TEXT </P>
         */
        public static final String CONTENT = "content";

        public static final String TEXT_LENGTH = "text_length";

        public static final String LOCATION = "location";
    }

    public static class MediaColumns {
        /**
         * The unique ID for a row
         * <P> Type: INTEGER (long) </P>
         */
        public static final String ID = "_id";


        /**
         * The reference id to note that this data belongs to
         * <P> Type: INTEGER (long) </P>
         */
        public static final String NOTE_ID = "note_id";

        public static final String IMAGE_INDEX = "image_position";

        public static final String IMAGE_PATH = "image_path";

        public static final String IMAGE_BLOB = "image_blob";

    }

    public static final class FolderColumns {

        public static final String ID = "_id";

        public static final String FOLDER_NAME = "folder_name";

        public static final String NOTES_COUNT = "notes_count";
    }
}
