package com.example.employeemanagementapp.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.employeemanagementapp.utils.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EMPLOYEE_TABLE =
                "CREATE TABLE " + Constants.TABLE_EMPLOYEE + " (" +
                        Constants.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Constants.COLUMN_FIRST_NAME + " TEXT," +
                        Constants.COLUMN_LAST_NAME + " TEXT," +
                        Constants.COLUMN_IMAGE + " BLOB," +
                        Constants.COLUMN_PHONE_NUMBER + " TEXT," +
                        Constants.COLUMN_JOB + " TEXT," +
                        Constants.COLUMN_EMAIL + " TEXT," +
                        Constants.COLUMN_RESIDENCE + " TEXT)";
        db.execSQL(CREATE_EMPLOYEE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_EMPLOYEE);
        onCreate(db);
    }
}