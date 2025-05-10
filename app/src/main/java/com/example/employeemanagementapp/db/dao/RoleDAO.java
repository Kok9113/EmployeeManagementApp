package com.example.employeemanagementapp.db.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class RoleDAO {
    private final SQLiteDatabase db;

    public RoleDAO(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }
    public List<String> getUserRoles(int userId) {
        List<String> roles = new ArrayList<>();
        String query = "SELECT r." + Constants.COLUMN_ROLE_NAME + " " +
                "FROM " + Constants.TABLE_ROLES + " r " +
                "JOIN " + Constants.TABLE_USER_ROLES + " ur ON r." + Constants.COLUMN_ROLE_ID + " = ur." + Constants.COLUMN_ROLE_IDD + " " +
                "WHERE ur." + Constants.COLUMN_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                roles.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return roles;
    }
}
