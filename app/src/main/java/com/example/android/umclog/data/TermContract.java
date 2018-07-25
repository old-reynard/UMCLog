package com.example.android.umclog.data;

import android.net.Uri;
import android.provider.BaseColumns;

//import java.net.URI;

/**
 * Defines table and column names for the term database.
 */
public class TermContract {

    private TermContract() {}
    /*
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * Play Store.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.umclog";
    /* URIs */
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_STUDENTS                = "students";
    public static final String PATH_PERIODS                 = "periods";
    public static final String PATH_TERMS                   = "terms";
    public static final String PATH_STUDENT_TO_PERIOD       = "student_to_period";
    public static final String PATH_ATTENDANCE              = "attendance";
    public static final String PATH_MARKS                   = "marks";
    public static final String PATH_ASSIGNMENTS             = "assignments";

    /* Inner class that defines the table contents of the terms table */
    public static final class StudentsEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Student_Names table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
                appendPath(PATH_STUDENTS).build();

        /* table constants */
        public static final String TABLE_NAME               = "students";
        public static final String STUDENT_ID               = BaseColumns._ID;
        public static final String COLUMN_STUDENT_NAME      = "student_name";
        public static final String COLUMN_LEVEL             = "level";
    }

    public static final class PeriodEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
                appendPath(PATH_PERIODS).build();

        public static final String TABLE_NAME               = "periods";
        public static final String COLUMN_PERIOD_ID         = BaseColumns._ID;
        public static final String COLUMN_PERIOD_NAME       = "period_name";
        public static final String COLUMN_LEVEL             = "level";
        public static final String COLUMN_TERM_ID           = "term_id";
    }

    public static final class StudentToPeriodEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
                appendPath(PATH_STUDENT_TO_PERIOD).build();

        public static final String TABLE_NAME               = "student_to_period";
        public static final String COLUMN_PERIOD_ID         = "period_id";
        public static final String COLUMN_STUDENT_ID        = "student_id";
    }

    public static final class TermEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
                appendPath(PATH_TERMS).build();

        public static final String TABLE_NAME               = "terms";
        public static final String TERM_ID                  = BaseColumns._ID;
        public static final String COLUMN_START_DATE        = "start_date";
        public static final String COLUMN_END_DATE          = "end_date";
    }

    public static final class AttendanceEntry {

//        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
//                appendPath(PATH_ATTENDANCE).build();

        public static final String TABLE_NAME               = "attendance";
        public static final String COLUMN_ATTENDANCE_ID     = "attendance_id";
        public static final String COLUMN_ATTENDANCE_DATE   = "attendance_date";
        public static final String COLUMN_ATTENDANCE_MARK   = "attendance_mark";
        public static final String COLUMN_PERIOD_ID         = "period_id";
        public static final String COLUMN_STUDENT_ID        = "student_id";
    }

    public static final class MarkEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
                appendPath(PATH_MARKS).build();

        public static final String TABLE_NAME               = "marks";
        public static final String COLUMN_MARK_ID           = BaseColumns._ID;
        public static final String COLUMN_MARK_VALUE        = "mark_value";
        public static final String COLUMN_ASSIGNMENT_ID     = "assignment_id";
        public static final String COLUMN_STUDENT_ID        = "student_id";
    }

    public static final class AssignmentEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
                appendPath(PATH_ASSIGNMENTS).build();

        public static final String TABLE_NAME               = "assignments";
        public static final String COLUMN_ASSIGNMENT_ID     = BaseColumns._ID;
        public static final String COLUMN_MARK_TYPE         = "mark_type";
        public static final String COLUMN_PERIOD_ID         = "period_id";
        public static final String COLUMN_DATE              = "date";
        public static final String COLUMN_MAX_VALUE         = "max_value";
        public static final String COLUMN_NAME              = "name";
        public static final String COLUMN_DESCRIPTION       = "description";

    }
}
