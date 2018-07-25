package com.example.android.umclog.data;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.test.InstrumentationRegistry;
import static junit.framework.Assert.assertEquals;


import org.junit.Test;

import java.util.HashSet;


public class TermProviderTest {

    private SQLiteDatabase database;
    private SQLiteOpenHelper dbHelper;

    @Test
    public void testCreateDb() {
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(TermContract.TermEntry.TABLE_NAME);
        String dataBaseNotOpen = "The database should be open and isn't";

        assertEquals(dataBaseNotOpen, true, database.isOpen());
    }
}