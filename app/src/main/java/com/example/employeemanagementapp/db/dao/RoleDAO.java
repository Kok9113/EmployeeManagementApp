package com.example.employeemanagementapp.db.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class RoleDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public RoleDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public List<String> getUserRoles(int userId) {
        List<String> roles = new ArrayList<>();
        String query = "SELECT r." + Constants.COLUMN_ROLE_NAME + " " +
                "FROM " + Constants.TABLE_ROLES + " r " +
                "JOIN " + Constants.TABLE_USER_ROLES + " ur " +
                "ON r." + Constants.COLUMN_ROLE_ID + " = ur." + Constants.COLUMN_USER_ROLE_ROLE_ID + " " +
                "WHERE ur." + Constants.COLUMN_USER_ROLE_USER_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            int nameIndex = cursor.getColumnIndex(Constants.COLUMN_ROLE_NAME);
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                do {
                    String roleName = cursor.getString(nameIndex);
                    if (roleName != null) {
                        roles.add(roleName);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("RoleDAO", "Lỗi khi lấy roles: " + e.getMessage());
        }

        return roles;
    }

    public List<Long> getRoleIdsByNames(List<String> roleNames) {
        List<Long> roleIds = new ArrayList<>();
        if (roleNames == null || roleNames.isEmpty()) {
            return roleIds;
        }

        String query = "SELECT " + Constants.COLUMN_ROLE_ID + " " +
                "FROM " + Constants.TABLE_ROLES + " " +
                "WHERE " + Constants.COLUMN_ROLE_NAME + " = ?";

        for (String name : roleNames) {
            try (Cursor cursor = db.rawQuery(query, new String[]{name})) {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_ROLE_ID));
                    roleIds.add(id);
                }
            } catch (Exception e) {
                android.util.Log.e("RoleDAO", "Lỗi khi lấy role ID cho " + name + ": " + e.getMessage());
            }
        }

        return roleIds;
    }

    public boolean hasRole(int userId, String roleName) {
        String query = "SELECT 1 FROM " + Constants.TABLE_USER_ROLES + " ur " +
                "INNER JOIN " + Constants.TABLE_ROLES + " r " +
                "ON ur." + Constants.COLUMN_USER_ROLE_ROLE_ID + " = r." + Constants.COLUMN_ROLE_ID + " " +
                "WHERE ur." + Constants.COLUMN_USER_ROLE_USER_ID + " = ? AND r." + Constants.COLUMN_ROLE_NAME + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), roleName})) {
            return cursor.moveToFirst();
        } catch (Exception e) {
            android.util.Log.e("RoleDAO", "Lỗi khi kiểm tra role " + roleName + ": " + e.getMessage());
            return false;
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
        dbHelper.close();
    }
}