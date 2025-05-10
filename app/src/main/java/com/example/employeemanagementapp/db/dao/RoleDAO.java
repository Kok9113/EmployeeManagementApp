package com.example.employeemanagementapp.db.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.employeemanagementapp.db.DatabaseHelper;

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
        String query = "SELECT r.name FROM Roles r " +
                "JOIN UserRoles ur ON r.id = ur.role_id " +
                "WHERE ur.user_id = ?";

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
