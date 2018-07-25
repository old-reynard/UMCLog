package com.example.android.umclog.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.umclog.data.TermContract.StudentsEntry;
import com.example.android.umclog.data.TermContract.AssignmentEntry;
import com.example.android.umclog.data.TermContract.MarkEntry;


public class TermProvider extends ContentProvider {

    /* Tag for the log messages */
    public static final String LOG_TAG = TermProvider.class.getSimpleName();

    /* DataBase helper */
    TermDbHelper mTermHelper;

    /* URI matcher constants*/
    private static final int STUDENTS           = 100;
    private static final int STUDENTS_ID        = 101;
    private static final int PERIODS            = 200;
    private static final int PERIODS_ID         = 201;
    private static final int TERMS              = 300;
    private static final int TERMS_ID           = 300;
    private static final int STUDENT_TO_PERIOD  = 400;
    private static final int ATTENDANCE         = 500;
    private static final int MARKS              = 600;
    private static final int ASSIGNMENTS        = 700;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(TermContract.CONTENT_AUTHORITY, TermContract.PATH_STUDENTS, STUDENTS);
        sUriMatcher.addURI(TermContract.CONTENT_AUTHORITY, TermContract.PATH_STUDENTS + "/#", STUDENTS_ID);
        sUriMatcher.addURI(TermContract.CONTENT_AUTHORITY, TermContract.PATH_PERIODS, PERIODS);
        sUriMatcher.addURI(TermContract.CONTENT_AUTHORITY, TermContract.PATH_PERIODS + "/#", PERIODS_ID);
        sUriMatcher.addURI(TermContract.CONTENT_AUTHORITY, TermContract.PATH_TERMS, TERMS);
        sUriMatcher.addURI(TermContract.CONTENT_AUTHORITY, TermContract.PATH_TERMS + "/#", TERMS_ID);
        sUriMatcher.addURI(TermContract.CONTENT_AUTHORITY, TermContract.PATH_STUDENT_TO_PERIOD, STUDENT_TO_PERIOD);
        sUriMatcher.addURI(TermContract.CONTENT_AUTHORITY, TermContract.PATH_ATTENDANCE, ATTENDANCE);
        sUriMatcher.addURI(TermContract.CONTENT_AUTHORITY, TermContract.PATH_MARKS, MARKS);
        sUriMatcher.addURI(TermContract.CONTENT_AUTHORITY, TermContract.PATH_ASSIGNMENTS, ASSIGNMENTS);
    }

    @Override
    public boolean onCreate() {
        mTermHelper = new TermDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mTermHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case TERMS:
                cursor = db.query(TermContract.TermEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, null);
                break;
            case PERIODS:
                cursor = db.query(TermContract.PeriodEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, null);
                break;
            case STUDENT_TO_PERIOD:
                cursor = db.rawQuery(selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        /* this notifies the cursor about any changes */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TERMS:
                return insertTerm(uri, values);
            case PERIODS:
                return insertPeriods(uri, values);
            case STUDENTS:
                return insertStudent(uri, values);
            case STUDENT_TO_PERIOD:
                return insertStudentToPeriod(uri, values);
            case ASSIGNMENTS:
                return insertAssignment(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        int deleted;
        SQLiteDatabase db = mTermHelper.getWritableDatabase();

        switch (match) {
            case MARKS:
                return db.delete(MarkEntry.TABLE_NAME, selection, selectionArgs);
            case ASSIGNMENTS:
            deleted = db.delete(AssignmentEntry.TABLE_NAME, selection, selectionArgs);
            if (deleted != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
                return deleted;
            }
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mTermHelper.getWritableDatabase();
        int updated;
        switch (sUriMatcher.match(uri)) {
            case STUDENTS:
                String name = values.getAsString(TermContract.StudentsEntry.COLUMN_STUDENT_NAME);
                if (name.isEmpty()) throw new IllegalArgumentException("Student needs a name!");
                updated = db.update(StudentsEntry.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return updated;

            case ASSIGNMENTS:
                long date = values.getAsLong(AssignmentEntry.COLUMN_DATE);
                if (date == 0) throw new IllegalArgumentException("Assignment needs a date!");

                int max = values.getAsInteger(AssignmentEntry.COLUMN_MAX_VALUE);
                if (max == 0) throw new IllegalArgumentException("Assignment needs a max value!");
                updated = db.update(AssignmentEntry.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return updated;

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private Uri insertStudent(Uri uri, ContentValues values) {
        String name = values.getAsString(TermContract.StudentsEntry.COLUMN_STUDENT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Student needs a name");
        }

        Integer level = values.getAsInteger(TermContract.StudentsEntry.COLUMN_LEVEL);
        if (level == null) {
            throw new IllegalArgumentException("Student needs a valid level");
        }

        SQLiteDatabase db = mTermHelper.getWritableDatabase();

        long id = db.insert(TermContract.StudentsEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertTerm(Uri uri, ContentValues values) {
        SQLiteDatabase db = mTermHelper.getWritableDatabase();
        long termId = db.insert(TermContract.TermEntry.TABLE_NAME, null, values);
        if (termId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, termId);
    }

    private Uri insertPeriods(Uri uri, ContentValues values) {
        SQLiteDatabase db = mTermHelper.getWritableDatabase();
        long id = db.insert(TermContract.PeriodEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertStudentToPeriod(Uri uri, ContentValues values) {
        SQLiteDatabase db = mTermHelper.getWritableDatabase();
        long id = db.insert(TermContract.StudentToPeriodEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertAssignment(Uri uri, ContentValues values) {

        long date = values.getAsLong(AssignmentEntry.COLUMN_DATE);
        if (date == 0) throw new IllegalArgumentException("Assignment needs a date!");

        int max = values.getAsInteger(AssignmentEntry.COLUMN_MAX_VALUE);
        if (max == 0) throw new IllegalArgumentException("Assignment needs a max value!");

        SQLiteDatabase db = mTermHelper.getWritableDatabase();
        long id = db.insert(AssignmentEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

}
