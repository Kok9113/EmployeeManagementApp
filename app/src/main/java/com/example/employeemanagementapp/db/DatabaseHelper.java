package com.example.employeemanagementapp.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "employees.db";
    private static final int DATABASE_VERSION = 8;

    // Employee table
    public static final String TABLE_EMPLOYEES = "employees";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_RESIDENCE = "residence";
    public static final String COLUMN_DEPARTMENT_ID = "department_id";
    public static final String COLUMN_POSITION = "position";

    // Department table
    public static final String TABLE_DEPARTMENTS = "departments";
    public static final String COLUMN_DEPT_ID = "_id";
    public static final String COLUMN_DEPT_NAME = "name";
    public static final String COLUMN_DEPT_POSITIONS = "positions";

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "_id";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";
    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_EMAIL + " TEXT UNIQUE, " +
                    COLUMN_USER_PASSWORD + " TEXT)";

    private static final String TABLE_CREATE_EMPLOYEES =
            "CREATE TABLE " + TABLE_EMPLOYEES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FIRST_NAME + " TEXT, " +
                    COLUMN_LAST_NAME + " TEXT, " +
                    COLUMN_IMAGE + " BLOB, " +
                    COLUMN_PHONE_NUMBER + " TEXT, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_RESIDENCE + " TEXT, " +
                    COLUMN_DEPARTMENT_ID + " INTEGER, " +
                    COLUMN_POSITION + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_DEPARTMENT_ID + ") REFERENCES " + TABLE_DEPARTMENTS + "(" + COLUMN_DEPT_ID + "))";

    private static final String TABLE_CREATE_DEPARTMENTS =
            "CREATE TABLE " + TABLE_DEPARTMENTS + " (" +
                    COLUMN_DEPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DEPT_NAME + " TEXT, " +
                    COLUMN_DEPT_POSITIONS + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_DEPARTMENTS);
        db.execSQL(TABLE_CREATE_EMPLOYEES);
        db.execSQL(TABLE_CREATE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE " + TABLE_EMPLOYEES + " ADD COLUMN " + COLUMN_DEPARTMENT_ID + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EMPLOYEES + " ADD COLUMN " + COLUMN_POSITION + " TEXT");
        }
        if (oldVersion < 8) {
            db.execSQL(TABLE_CREATE_USERS);
        }
    }

    // Kiểm tra xem phòng ban có nhân viên liên quan không
    public boolean hasEmployeesInDepartment(long departmentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMPLOYEES, new String[]{COLUMN_ID},
                COLUMN_DEPARTMENT_ID + "=?", new String[]{String.valueOf(departmentId)},
                null, null, null);
        boolean hasEmployees = cursor.getCount() > 0;
        cursor.close();
        return hasEmployees;
    }

    // Employee CRUD methods
    public long insertEmployee(String firstName, String lastName, byte[] image, String phoneNumber, String email, String residence, long departmentId, String position) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_IMAGE, image);
        values.put(COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_RESIDENCE, residence);
        values.put(COLUMN_DEPARTMENT_ID, departmentId);
        values.put(COLUMN_POSITION, position);
        long result = db.insert(TABLE_EMPLOYEES, null, values);
        db.close();
        return result;
    }

    public Cursor getAllEmployees() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES, null);
    }

    public Cursor getEmployeeById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EMPLOYEES, null, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }

    public byte[] getEmployeeProfileImage(long employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMPLOYEES, new String[]{COLUMN_IMAGE}, COLUMN_ID + "=?", new String[]{String.valueOf(employeeId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") byte[] image = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE));
            cursor.close();
            return image;
        }
        return null;
    }

    public int updateEmployee(long id, String firstName, String lastName, byte[] image, String phoneNumber, String email, String residence, long departmentId, String position) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_IMAGE, image);
        values.put(COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_RESIDENCE, residence);
        values.put(COLUMN_DEPARTMENT_ID, departmentId);
        values.put(COLUMN_POSITION, position);
        int rowsAffected = db.update(TABLE_EMPLOYEES, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }

    public int deleteEmployee(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_EMPLOYEES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted;
    }

    public Cursor getAllEmployeesFiltered(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_FIRST_NAME + " LIKE ? OR " + COLUMN_LAST_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};
        return db.query(TABLE_EMPLOYEES, null, selection, selectionArgs, null, null, null);
    }

    // Department CRUD methods
    public long insertDepartment(String name, String positions) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEPT_NAME, name);
        values.put(COLUMN_DEPT_POSITIONS, positions);
        long result = db.insert(TABLE_DEPARTMENTS, null, values);
        db.close();
        return result;
    }

    public Cursor getAllDepartments() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_DEPARTMENTS, null);
    }

    public Cursor getDepartmentById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_DEPARTMENTS, null, COLUMN_DEPT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }

    public int updateDepartment(long id, String name, String positions) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEPT_NAME, name);
        values.put(COLUMN_DEPT_POSITIONS, positions);
        int rowsAffected = db.update(TABLE_DEPARTMENTS, values, COLUMN_DEPT_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }

    public int deleteDepartment(long id) {
        if (hasEmployeesInDepartment(id)) {
            return -1; // Trả về -1 nếu có nhân viên liên quan
        }
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_DEPARTMENTS, COLUMN_DEPT_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted;
    }

    public Cursor searchDepartments(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_DEPT_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};
        return db.query(TABLE_DEPARTMENTS, null, selection, selectionArgs, null, null, null);
    }

    // Đăng ký người dùng mới
    public boolean registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Kiểm tra thông tin đăng nhập

    public boolean checkLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USER_EMAIL + "=? AND " + COLUMN_USER_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }


}