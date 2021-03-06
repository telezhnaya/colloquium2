package com.csc.telezhnaya.todo2;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

public class MyContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.csc.telezhnaya.todo2";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri TASKS_URI = Uri.withAppendedPath(MyContentProvider.CONTENT_URI, "tasks");
    public static final Uri TAGS_URI = Uri.withAppendedPath(MyContentProvider.CONTENT_URI, "tags");

    public static final int TASK_LIST = 1;
    public static final int TASK = 2;
    public static final int TAG_LIST = 3;
    public static final int TAG = 4;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "/tasks", TASK_LIST);
        uriMatcher.addURI(AUTHORITY, "/tasks/#", TASK);
        uriMatcher.addURI(AUTHORITY, "/tags", TAG_LIST);
        uriMatcher.addURI(AUTHORITY, "/tags/#", TAG);
    }

    private MyOpenHelper helper;

    public MyContentProvider() {
        helper = new MyOpenHelper(getContext());
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) != TAG_LIST) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
        int id = helper.getWritableDatabase().delete(TagTable.TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return id;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) == TASK_LIST) {
            long id = helper.getWritableDatabase().insert(TaskTable.TABLE_NAME, null, values);
            Uri inserted = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(inserted, null);
            return inserted;
        }
        if (uriMatcher.match(uri) == TAG_LIST) {
            long id = helper.getWritableDatabase().insert(TagTable.TABLE_NAME, null, values);
            Uri inserted = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(inserted, null);
            return inserted;
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        helper = new MyOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        if (uriMatcher.match(uri) == TAG_LIST) {
            builder.setTables(TagTable.TABLE_NAME);
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }

        builder.setTables(TaskTable.TABLE_NAME);
        SQLiteDatabase db = helper.getReadableDatabase();

        if (uriMatcher.match(uri) == TASK_LIST) {
            Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
        if (uriMatcher.match(uri) == TASK) {
            long position = ContentUris.parseId(uri);
            Cursor cursor = builder.query(db, new String[]{TaskTable.COLUMN_DESCRIPTION},
                    TaskTable._ID + "=?", new String[]{String.valueOf(position)}, null, null, sortOrder);

            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (uriMatcher.match(uri) != TASK) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        int rowId = helper.getWritableDatabase().update(TaskTable.TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rowId;
    }
}
