package com.example.android.umclog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import com.example.android.umclog.data.TermContract.StudentsEntry;
import com.example.android.umclog.data.TermContract.StudentToPeriodEntry;
import com.example.android.umclog.data.TermDbHelper;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * Used for utility methods for different purposes
 */
public class GeneralUtils {

    public static final int BUTTONS_PER_SCREEN = 5;

    private GeneralUtils() {}

    /**
     * When creating a new period, it is implied that a list of students' names is bulk uploaded
     * from Excel or other outside source. This method helps process those names
     * @param list of names from outer sources such as Excel or similar
     * @return a string array of names for further processing
     */
    public static ArrayList<String> getNamesFromOutside(String list) {
        ArrayList<String> names = new ArrayList<>();
        Scanner scanner = new Scanner(list);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.isEmpty()) continue;
            names.add(capitalize(line));
        }
        return names;
    }

    /**
     * Used to capitalize all students' names properly
     * @param name name of a student that will be split for processing
     * @return properly capitalized String with the name
     */
    public static String capitalize(String name) {
        String[] nameArray = name.split("\\s+");
//        String capitalizedName = "";
        StringBuilder capitalizedName = new StringBuilder();
        try {
            for (String thisName:nameArray) {
                String capitalized = Character.toUpperCase(thisName.charAt(0)) + thisName.substring(1);
                capitalizedName.append(capitalized);
                capitalizedName.append(" ");
            }
        } catch (Exception e) {
            Log.e("There is ", "an exception");
            // todo complete this properly
        }
        return capitalizedName.toString().trim();
    }


    /**
     * Used to format date strings from milliseconds
     * @param timeInMillis input date
     * @return String representation of the chosen date
     */
    public static String getUtcDate(long timeInMillis) {
        Locale locale = Locale.getDefault();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy", locale);
        return formatter.format(timeInMillis);
    }

    /**
     * Used to format date strings for Attendance and Marks adapters
     * @param timeInMillis input date for every date
     * @return String representation of the chosen date
     */
    public static String getShortUtcDate(long timeInMillis) {
        Locale locale = Locale.getDefault();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d", locale);
        return formatter.format(timeInMillis);
    }

    /**
     * generic function to find the index of an element in an object array in Java
     * @param a array
     * @param target item to be found
     * @param <T> type
     * @return index of the item or -1 if not found
     */

    public static<T> int find(T[] a, T target) {
        for (int i = 0; i < a.length; i++)
            if (target.equals(a[i]))
                return i;
        return -1;
    }

    /**
     * utility method to query students for particular period
     * @param db database reference
     * @param thisPeriodId current period id used for the query
     * @param thisPeriodLevel current level, used to set level to queried students
     * @return list of students with names, ids and levels
     */
    public static List<Student> getStudentNames(@NonNull SQLiteDatabase db, int thisPeriodId, int thisPeriodLevel) {
        String DOT = ".";
        String studentToPeriodSql =
                "SELECT " + StudentsEntry.TABLE_NAME            + DOT +
                        StudentsEntry.COLUMN_STUDENT_NAME       + ", " +
                        StudentsEntry.TABLE_NAME                + DOT +
                        StudentsEntry.STUDENT_ID                + " FROM " +
                        StudentToPeriodEntry.TABLE_NAME         + " LEFT JOIN " +
                        StudentsEntry.TABLE_NAME                + " ON " +
                        StudentToPeriodEntry.TABLE_NAME         + DOT +
                        StudentToPeriodEntry.COLUMN_STUDENT_ID  + " = " +
                        StudentsEntry.TABLE_NAME                + DOT +
                        StudentsEntry.STUDENT_ID                + " WHERE " +
                        StudentToPeriodEntry.TABLE_NAME         + DOT +
                        StudentToPeriodEntry.COLUMN_PERIOD_ID   + " = " +
                        thisPeriodId;

        Cursor cursor = db.rawQuery(studentToPeriodSql, null);

        List<Student> students = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(
                    StudentsEntry.TABLE_NAME + "." + StudentsEntry.COLUMN_STUDENT_NAME));
            int thisStudentId = cursor.getInt(cursor.getColumnIndex(
                    StudentsEntry.TABLE_NAME + "." + StudentsEntry.STUDENT_ID));

            Student thisStudent = new Student(name);
            thisStudent.setStudentId(thisStudentId);

            thisStudent.setLevel(thisPeriodLevel);
            students.add(thisStudent);
        }
        cursor.close();
        return students;
    }

    /**
     * utility method, used to convert database int parameters into assignment type,
     * applied in Marks activity
     * @param r resources to form the array of assignment types
     * @param type queried int value
     * @return String values with assignment type
     */
    public static String getAssignmentType(Resources r, int type) {

        String[] types = r.getStringArray(R.array.assignment_types);
        return types[type];
    }

    /**
     * used to add a new student or edit an existing one in the attendance or marks activity
     * @param c current activity
     */
    public static void addOrEditStudent(final Context c, final Student s, final Period p, final boolean edit) {

        /*  setup the edit field */
        final EditText nameField = new EditText(c);
        nameField.setLines(2);
        nameField.setHint(R.string.add_student_hint);


        final AlertDialog.Builder builder = new AlertDialog.Builder(c);
        if (s != null) {
            nameField.setText(s.getName());
            builder.setTitle(R.string.edit_student_dialog_label);
        } else {
            builder.setTitle(R.string.add_student_menu_label);
        }


        builder.setView(nameField)
            .setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (nameField.getText().toString().isEmpty()) {
                        String warning = c.getString(R.string.empty_name_warning);
                        Toast.makeText(c, warning, Toast.LENGTH_SHORT).show();
                    }

                    ContentValues studentValues = new ContentValues();
                    studentValues.put(StudentsEntry.COLUMN_STUDENT_NAME,
                            GeneralUtils.capitalize(nameField.getText().toString()));

                    /* if an existing student is being edited */
                    if (edit) {
                        if (s != null) {
                            int updated = c.getContentResolver().update(
                                    StudentsEntry.CONTENT_URI,
                                    studentValues,
                                    StudentsEntry.STUDENT_ID + "=?",
                                    new String[]{Integer.toString(s.getStudentId())}
                            );
                            showIfStudentSaved(c, updated != 0);

                        }

                    /* it's a new student being created */
                    } else {
                        if (p != null) {
                            /* in this scenario to tables will have to be used - Students and StoP */
                            studentValues.put(StudentsEntry.COLUMN_LEVEL, p.getLevel());
                            Uri newStudentUri = c.getContentResolver().
                                    insert(StudentsEntry.CONTENT_URI, studentValues);

                            long newId = ContentUris.parseId(newStudentUri);

                            ContentValues stopValues = new ContentValues();
                            stopValues.put(StudentToPeriodEntry.COLUMN_STUDENT_ID, newId);
                            stopValues.put(StudentToPeriodEntry.COLUMN_PERIOD_ID, p.getId());

                            Uri stopUri = c.getContentResolver().insert(StudentToPeriodEntry.CONTENT_URI,
                                    stopValues);

                            showIfStudentSaved(c, newId != 0 && stopUri != null);
                        }
                    }

                    Activity activity = (Activity)c;
                    activity.recreate();
                    }
                })
                .setNegativeButton(R.string.confirm_dialog_negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    /**
     * use in the addOrEditStudent method to make a Toast upon the method's results
     * @param c activity where it happens
     * @param condition whether database returns null or non-null data
     */
    private static void showIfStudentSaved(Context c, boolean condition) {
        String warning;
        if (condition) {
            warning = c.getString(R.string.student_saved_warning);
        } else {
            warning = c.getString(R.string.student_not_saved_warning);
        }
        Toast.makeText(c, warning, Toast.LENGTH_SHORT).show();
    }

    public static String getLabelFromMark(int mark, Resources r) {
        switch (mark) {
            case 1: return r.getString(R.string.attendance_present_abbr);
            case 2: return r.getString(R.string.attendance_absent_abbr);
            case 3: return r.getString(R.string.attendance_absent_we_abbr);
            case 4: return r.getString(R.string.attendance_late_abbr);
            case 5: return r.getString(R.string.attendance_late_we_abbr);
            default: return r.getString(R.string.attendance_empty_label);
        }
    }
}
