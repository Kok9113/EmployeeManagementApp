package com.example.employeemanagementapp.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.employeemanagementapp.db.dao.EmployeeDAO;
import com.example.employeemanagementapp.utils.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = Constants.DATABASE_NAME;
    private static final int DATABASE_VERSION = Constants.DATABASE_VERSION;
    // Employee table


    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + Constants.TABLE_USERS + " (" +
                    Constants.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.COLUMN_USER_NAME + " TEXT UNIQUE, " +
                    Constants.COLUMN_USER_PASSWORD + " TEXT)";

    private static final String TABLE_CREATE_EMPLOYEES =
            "CREATE TABLE " + Constants.TABLE_EMPLOYEE + " (" +
                    Constants.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.COLUMN_FIRST_NAME + " TEXT, " +
                    Constants.COLUMN_LAST_NAME + " TEXT, " +
                    Constants.COLUMN_IMAGE + " BLOB, " +
                    Constants.COLUMN_PHONE_NUMBER + " TEXT, " +
                    Constants.COLUMN_EMAIL + " TEXT, " +
                    Constants.COLUMN_RESIDENCE + " TEXT, " +
                    Constants.COLUMN_DEPARTMENT_ID + " INTEGER, " +
                    Constants.COLUMN_POSITION + " TEXT, " +
                    "FOREIGN KEY(" + Constants.COLUMN_DEPARTMENT_ID + ") REFERENCES " + Constants.TABLE_DEPARTMENTS + "(" + Constants.COLUMN_DEPT_ID + "))";

    private static final String TABLE_CREATE_DEPARTMENTS =
            "CREATE TABLE " + Constants.TABLE_DEPARTMENTS + " (" +
                    Constants.COLUMN_DEPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.COLUMN_DEPT_NAME + " TEXT, " +
                    Constants.COLUMN_DEPT_POSITIONS + " TEXT)";

    private static final String TABLE_CREATE_ROLES =
            "CREATE TABLE " + Constants.TABLE_ROLES + " (" +
                    Constants.COLUMN_ROLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.COLUMN_ROLE_NAME + " TEXT NOT NULL)";

    private static final String TABLE_CREATE_PERMISSIONS =
            "CREATE TABLE " + Constants.TABLE_PERMISSIONS + " (" +
                    Constants.COLUMN_PERMISSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.COLUMN_PERMISSION_NAME + " TEXT NOT NULL)";

    private static final String TABLE_CREATE_ROLE_PERMISSIONS =
            "CREATE TABLE " + Constants.TABLE_ROLE_PERMISSIONS + " (" +
                    Constants.COLUMN_ROLE_IDD + " INTEGER NOT NULL, " +
                    Constants.COLUMN_PERMISSION_IDD + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + Constants.COLUMN_ROLE_IDD+ ") REFERENCES " + Constants.TABLE_ROLES + "(" + Constants.COLUMN_ROLE_ID + "), " +
                    "FOREIGN KEY(" + Constants.COLUMN_PERMISSION_IDD + ") REFERENCES " + Constants.TABLE_PERMISSIONS + "(" + Constants.COLUMN_PERMISSION_ID + "), " +
                    "PRIMARY KEY(" + Constants.COLUMN_ROLE_IDD + ", " + Constants.COLUMN_PERMISSION_IDD + "))";
    private static final String TABLE_CREATE_USER_ROLES =
            "CREATE TABLE " + Constants.TABLE_USER_ROLES + " (" +
                    Constants.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    Constants.COLUMN_ROLE_IDD + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + Constants.COLUMN_USER_ID + ") REFERENCES " + Constants.TABLE_USERS + "(" + Constants.COLUMN_USER_ID + "), " +
                    "FOREIGN KEY(" + Constants.COLUMN_ROLE_IDD + ") REFERENCES " + Constants.TABLE_ROLES + "(" + Constants.COLUMN_ROLE_ID + "), " +
                    "PRIMARY KEY(" + Constants.COLUMN_USER_ID + ", " + Constants.COLUMN_ROLE_ID + "))";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_DEPARTMENTS);
        db.execSQL(TABLE_CREATE_EMPLOYEES);
        db.execSQL(TABLE_CREATE_USERS);
        db.execSQL(TABLE_CREATE_ROLES);
        db.execSQL(TABLE_CREATE_PERMISSIONS);
        db.execSQL(TABLE_CREATE_ROLE_PERMISSIONS);
        db.execSQL(TABLE_CREATE_USER_ROLES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE " + Constants.TABLE_EMPLOYEE + " ADD COLUMN " + Constants.COLUMN_DEPARTMENT_ID + " INTEGER");
            db.execSQL("ALTER TABLE " + Constants.TABLE_EMPLOYEE + " ADD COLUMN " + Constants.COLUMN_POSITION + " TEXT");
        }
        if (oldVersion < 8) {
            db.execSQL(TABLE_CREATE_USERS);
        }
    }







}
