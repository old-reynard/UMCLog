package com.example.android.umclog;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.example.android.umclog.data.TermContract.AttendanceEntry;
import com.example.android.umclog.data.TermContract.PeriodEntry;
import com.example.android.umclog.data.TermContract.TermEntry;
import com.example.android.umclog.data.TermDbHelper;



import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Orientation;
import jxl.format.PageOrientation;
import jxl.format.PaperSize;
import jxl.write.BoldStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExportUtils {
    ExportUtils() {}

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String AND = " AND ";
    private static final String EQUALS = " = ";

    private static final long day;

    static {
        day = 24 * 60 * 60 * 1000;
    }

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     * @param activity context
     */
    private static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    public static void sendAttendanceToAdmin(Context c, int termId) {
        verifyStoragePermissions((Activity)c);

        File report = Environment.getExternalStorageDirectory();
        String reportFileName = "report.xls";

        File directory = new File(report.getAbsolutePath());

        if (!directory.isDirectory()) {
            //create directory if not exist
            boolean mkdirs = directory.mkdirs();

        }
        /* file path */
        File file = new File(directory, reportFileName);

        try {


            int vOffset = c.getResources().getInteger(R.integer.export_table_vertical_offset);
            int hOffset = c.getResources().getInteger(R.integer.export_table_horizontal_offset);

            /* excel book settings */
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "CA"));
            WritableWorkbook workbook;

            /* WritableWorkbook class helps to create Excel sheet */
            workbook = Workbook.createWorkbook(file, wbSettings);

            /* Excel sheet name. 0 represents first sheet */
            WritableSheet sheet = workbook.createSheet("attendance_report", 0);
            WritableFont.FontName usedFont = WritableFont.createFont("Calibri");

            WritableFont headerFont = new WritableFont(usedFont, 12, WritableFont.BOLD);
            CellView headerCellView = new CellView();
            headerCellView.setAutosize(true);
            WritableCellFormat headerFormat = new WritableCellFormat(headerFont);

            sheet.setPageSetup(PageOrientation.LANDSCAPE, PaperSize.A4, 0, 0);

            /* setting headers */
            sheet.setColumnView(hOffset, headerCellView);
            sheet.addCell(new Label(hOffset, 0, c.getString(R.string.table_name), headerFormat));
            sheet.addCell(new Label(hOffset, vOffset - 1, c.getString(R.string.column_student_name)));
            sheet.addCell(new Label(hOffset + 1, 0, c.getString(R.string.college_label), headerFormat));

            WritableCellFormat legendFormat = new WritableCellFormat(new WritableFont(usedFont, 9));

            String legend = getLegend(c);
            sheet.addCell(new Label(hOffset + 2, vOffset - 2, legend, legendFormat));

            /* getting the database */
            TermDbHelper helper = new TermDbHelper(c);
            SQLiteDatabase db = helper.getReadableDatabase();
            ContentResolver cr = c.getContentResolver();

            /* querying term information */
            Cursor termCursor = cr.query(TermEntry.CONTENT_URI,
                    new String[]{TermEntry.COLUMN_START_DATE, TermEntry.COLUMN_END_DATE},
                    TermEntry.TERM_ID + EQUALS + "?",
                    new String[]{Integer.toString(termId)},
                    null);
            long start = 0;
            long end = 0;

            if (termCursor != null && termCursor.moveToFirst()) {
                do {
                    start = termCursor.getLong(termCursor.getColumnIndex(TermEntry.COLUMN_START_DATE));
                    end = termCursor.getLong(termCursor.getColumnIndex(TermEntry.COLUMN_END_DATE));
                } while (termCursor.moveToNext());

                termCursor.close();
            }

            /* setting date labels for the term */
            List<Long> dates = new ArrayList<>();

            WritableFont dateFont = new WritableFont(usedFont, 9, WritableFont.BOLD);
            WritableCellFormat dateFormat = new WritableCellFormat(dateFont);
            dateFormat.setAlignment(Alignment.CENTRE);
            dateFormat.setBorder(Border.ALL, BorderLineStyle.THICK);
            CellView dateCellView = new CellView();
            dateCellView.setSize(1000);

            /* define format for time cells */
            WritableFont timeFont = new WritableFont(usedFont, 9, WritableFont.BOLD);
            WritableCellFormat timeFormat = new WritableCellFormat(timeFont);
            timeFormat.setBorder(Border.ALL, BorderLineStyle.THICK);
            timeFormat.setOrientation(Orientation.PLUS_90);

            CellView timeCellView = new CellView();
            timeCellView.setSize(1000);
            sheet.setColumnView(0, timeCellView);

            CellView emptyCellView = new CellView();
            emptyCellView.setSize(0);
            sheet.setColumnView(1, emptyCellView);

            Calendar calendar = Calendar.getInstance();
            for (long i = start; i < end; i += day) {
                Date today = new Date(i);
                calendar.setTime(today);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int thisCol = hOffset + dates.size() + 1;
                if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                    dates.add(i);
                    String dayLabel = new SimpleDateFormat("E", Locale.getDefault())
                            .format(today).substring(0, 1);
                    sheet.addCell(new Label(thisCol, vOffset - 1, dayLabel, dateFormat));
                    String dateLabel = GeneralUtils.getShortUtcDate(i);
                    dateLabel = dateLabel.substring(dateLabel.length() - 2);
                    sheet.addCell(new Label(thisCol, vOffset, dateLabel, dateFormat));
                    sheet.setColumnView(thisCol, dateCellView);
                }
            }

            /* querying periods' ids for the term */
            Cursor periodIdsCursor = cr.query(PeriodEntry.CONTENT_URI,
                    new String[]{PeriodEntry.COLUMN_PERIOD_ID},
                    PeriodEntry.COLUMN_TERM_ID + EQUALS + "?",
                    new String[]{Integer.toString(termId)}, null);

            String[] periodIdArgs;


            if (periodIdsCursor != null && periodIdsCursor.getCount() > 0) {
                int idx = 0;
                periodIdArgs = new String[periodIdsCursor.getCount()];
                if (periodIdsCursor.moveToFirst()) {
                    do {
                        String periodId = periodIdsCursor.
                                getString(periodIdsCursor.getColumnIndex(PeriodEntry.COLUMN_PERIOD_ID));
                        periodIdArgs[idx++] = periodId;
                    } while (periodIdsCursor.moveToNext());

                    periodIdsCursor.close();

                    int tablesStarts = vOffset + 1;
                    int tableEnds;
                    int period = 1;

                    /* attendance table starts here */
                    int periodOffset = 0;
                    for (String periodId: periodIdArgs) {

                        List<Student> studentNames = GeneralUtils.getStudentNames(db,
                                Integer.parseInt(periodId), 0);

                        /* setting attendance cells */
                        for (int s = 0; s < studentNames.size(); s++) {
                            int row = vOffset + periodOffset + s + 1;

                            boolean firstRow = row == vOffset + periodOffset + 1;
                            boolean lastRow = row == vOffset + periodOffset + studentNames.size();


                            WritableFont markFont = new WritableFont(usedFont, 9);
                            WritableCellFormat nameFormat =
                                    setNameCellFormat(firstRow, lastRow, markFont);

                            sheet.addCell(new Label(hOffset, row, "" + (s + 1) + ". " +
                                    studentNames.get(s).getName(), nameFormat));

                            for (int d = 0; d < dates.size(); d++) {
                                int col = hOffset + 1 + d;

                                boolean leftCol = col == hOffset + 1;
                                boolean lastCol = col == hOffset + dates.size();

                                WritableCellFormat markFormat = setMarkCellFormat
                                        (firstRow, lastRow, leftCol, lastCol, markFont);

                                Cursor thisCursor = db.query(
                                        AttendanceEntry.TABLE_NAME,
                                        new String[] {AttendanceEntry.COLUMN_ATTENDANCE_MARK},
                                        AttendanceEntry.COLUMN_PERIOD_ID + EQUALS + "?" + AND +
                                                AttendanceEntry.COLUMN_STUDENT_ID + EQUALS + "?" + AND +
                                                AttendanceEntry.COLUMN_ATTENDANCE_DATE + EQUALS + "?",
                                        new String[] {periodId, Integer.toString(studentNames.get(s)
                                                .getStudentId()), Long.toString(dates.get(d))},
                                        null, null, null
                                );

                                if (thisCursor != null && thisCursor.getCount() > 0) {
                                    thisCursor.moveToFirst();
                                    int mark = thisCursor.getInt(thisCursor.getColumnIndex
                                            (AttendanceEntry.COLUMN_ATTENDANCE_MARK));

                                    sheet.addCell(new Label(col, row,
                                            GeneralUtils.getLabelFromMark(mark, c.getResources()),
                                            markFormat));
                                } else {
                                    sheet.addCell(new Label(col, row, null, markFormat));
                                }
                                thisCursor.close();
                            }
                        }


                        periodOffset    += studentNames.size();
                        tableEnds       = tablesStarts + studentNames.size() - 1;

                        sheet.mergeCells(0, tablesStarts, 0, tableEnds);
                        String timeStamp = "Period " + period + ", " + getPeriodTime(period);
                        sheet.addCell(new Label(0, tablesStarts, timeStamp, timeFormat));
                        tablesStarts = tableEnds + 1;
                        period++;

                        if (period == 3) {
                            sheet.addRowPageBreak(tablesStarts);
                        }
                    }
                }
            }

            /* wrapping up */
            workbook.write();
            workbook.close();

            /* sending the report through an intent */
            Intent mail = new Intent(Intent.ACTION_SEND);
            mail.setType("text/plain");
            Uri path = Uri.fromFile(file);
            mail.putExtra(Intent.EXTRA_STREAM, path);
            mail.putExtra(Intent.EXTRA_EMAIL, new String[] { "gerasimovkit@gmail.com" });

            String subject = "Attendance for " + GeneralUtils.getUtcDate(start)
                    + " - " + GeneralUtils.getUtcDate(end);
            mail.putExtra(Intent.EXTRA_SUBJECT, subject);
            c.startActivity(mail);


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(c,
                    e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static WritableCellFormat setMarkCellFormat(boolean firstRow, boolean lastRow, boolean leftCol, boolean lastCol, WritableFont markFont)  {
        WritableCellFormat markFormat;

        try {
            if (firstRow && leftCol) {
                markFormat = new WritableCellFormat(markFont);
                markFormat.setBorder(Border.ALL, BorderLineStyle.THICK);
                markFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
                markFormat.setBorder(Border.RIGHT, BorderLineStyle.THIN);
            } else if (firstRow && lastCol) {
                markFormat = new WritableCellFormat(markFont);
                markFormat.setBorder(Border.ALL, BorderLineStyle.THICK);
                markFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
                markFormat.setBorder(Border.LEFT, BorderLineStyle.THIN);
            } else if (lastRow && leftCol) {
                markFormat = new WritableCellFormat(markFont);
                markFormat.setBorder(Border.ALL, BorderLineStyle.THICK);
                markFormat.setBorder(Border.TOP, BorderLineStyle.THIN);
                markFormat.setBorder(Border.RIGHT, BorderLineStyle.THIN);
            } else if (lastRow && lastCol) {
                markFormat = new WritableCellFormat(markFont);
                markFormat.setBorder(Border.ALL, BorderLineStyle.THICK);
                markFormat.setBorder(Border.TOP, BorderLineStyle.THIN);
                markFormat.setBorder(Border.LEFT, BorderLineStyle.THIN);
            } else if (firstRow) {
                markFormat = new WritableCellFormat(markFont);
                markFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
                markFormat.setBorder(Border.TOP, BorderLineStyle.THICK);
            } else if (lastRow) {
                markFormat = new WritableCellFormat(markFont);
                markFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
                markFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK);
            } else if (leftCol) {
                markFormat = new WritableCellFormat(markFont);
                markFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
                markFormat.setBorder(Border.LEFT, BorderLineStyle.THICK);
            } else if (lastCol) {
                markFormat = new WritableCellFormat(markFont);
                markFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
                markFormat.setBorder(Border.RIGHT, BorderLineStyle.THICK);
            } else {
                markFormat = new WritableCellFormat(markFont);
                markFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
            }

            markFormat.setAlignment(Alignment.CENTRE);
            return markFormat;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static WritableCellFormat setNameCellFormat(boolean firstRow, boolean lastRow, WritableFont markFont) {
        WritableCellFormat nameFormat;
        try {
            if (firstRow) {
                nameFormat = new WritableCellFormat(markFont);
                nameFormat.setBorder(Border.ALL, BorderLineStyle.THICK);
                nameFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
            } else if (lastRow) {
                nameFormat = new WritableCellFormat(markFont);
                nameFormat.setBorder(Border.ALL, BorderLineStyle.THICK);
                nameFormat.setBorder(Border.TOP, BorderLineStyle.THIN);
            } else {
                nameFormat = new WritableCellFormat(markFont);
                nameFormat.setBorder(Border.ALL, BorderLineStyle.THICK);
                nameFormat.setBorder(Border.TOP, BorderLineStyle.THIN);
                nameFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
            }
            return nameFormat;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getLegend(Context c) {
        return  c.getString(R.string.attendance_present_abbr)         + " - " +
                c.getString(R.string.attendance_present)      + ", "  +
                c.getString(R.string.attendance_absent_abbr)          + " - " +
                c.getString(R.string.attendance_absent)       + ", "  +
                c.getString(R.string.attendance_absent_we_abbr)       + " - " +
                c.getString(R.string.attendance_absent_we)    + ", "  +
                c.getString(R.string.attendance_late_abbr)            + " - " +
                c.getString(R.string.attendance_late)         + ", "  +
                c.getString(R.string.attendance_late_we_abbr)         + " - " +
                c.getString(R.string.attendance_late_we);
    }

    private static String getPeriodTime(int period) {
        switch (period) {
            case 1: return "09:00 - 10:30";
            case 2: return "10:45 - 12:15";
            case 3: return "13:00 - 14:30";
            default: return "Afternoon";
        }
    }
}
