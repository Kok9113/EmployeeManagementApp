package com.example.employeemanagementapp.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private final SQLiteDatabase db;

    public UserDAO(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    // Đăng ký người dùng mới
    public boolean registerUser(String username, String password) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_NAME, username);
        values.put(Constants.COLUMN_USER_PASSWORD, password);

        long result = db.insert(Constants.TABLE_USERS, null, values);
        return result != -1;
    }

    // Kiểm tra thông tin đăng nhập
    public boolean checkLogin(String username, String password) {
        Cursor cursor = db.query(Constants.TABLE_USERS,
                new String[]{Constants.COLUMN_USER_ID},
                Constants.COLUMN_USER_NAME + "=? AND " + Constants.COLUMN_USER_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

}
