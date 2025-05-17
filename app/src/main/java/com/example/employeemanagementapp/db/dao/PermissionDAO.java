package com.example.employeemanagementapp.db.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public PermissionDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public List<String> getUserPermissions(int userId) {
        Set<String> permissions = new HashSet<>();

        // Truy vấn hợp nhất để lấy quyền từ role_permissions và user_permissions
        String query = "SELECT DISTINCT p." + Constants.COLUMN_PERMISSION_NAME + " " +
                "FROM " + Constants.TABLE_PERMISSIONS + " p " +
                "LEFT JOIN " + Constants.TABLE_ROLE_PERMISSIONS + " rp " +
                "ON p." + Constants.COLUMN_PERMISSION_ID + " = rp." + Constants.COLUMN_ROLE_PERMISSION_PERMISSION_ID + " " +
                "LEFT JOIN " + Constants.TABLE_USER_ROLES + " ur " +
                "ON rp." + Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + " = ur." + Constants.COLUMN_USER_ROLE_ROLE_ID + " " +
                "LEFT JOIN " + Constants.TABLE_USER_PERMISSIONS + " up " +
                "ON p." + Constants.COLUMN_PERMISSION_ID + " = up." + Constants.COLUMN_USER_PERMISSION_PERMISSION_ID + " " +
                "WHERE ur." + Constants.COLUMN_USER_ROLE_USER_ID + " = ? OR up." + Constants.COLUMN_USER_PERMISSION_USER_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(userId)})) {
            int nameIndex = cursor.getColumnIndex(Constants.COLUMN_PERMISSION_NAME);
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                do {
                    String permissionName = cursor.getString(nameIndex);
                    if (permissionName != null) {
                        permissions.add(permissionName);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("PermissionDAO", "Lỗi khi lấy permissions: " + e.getMessage());
        }

        return new ArrayList<>(permissions);
    }

    public List<Long> getPermissionIdsByNames(List<String> permissionNames) {
        List<Long> permissionIds = new ArrayList<>();
        if (permissionNames == null || permissionNames.isEmpty()) {
            return permissionIds;
        }

        String query = "SELECT " + Constants.COLUMN_PERMISSION_ID + " " +
                "FROM " + Constants.TABLE_PERMISSIONS + " " +
                "WHERE " + Constants.COLUMN_PERMISSION_NAME + " = ?";

        for (String name : permissionNames) {
            try (Cursor cursor = db.rawQuery(query, new String[]{name})) {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_PERMISSION_ID));
                    permissionIds.add(id);
                }
            } catch (Exception e) {
                android.util.Log.e("PermissionDAO", "Lỗi khi lấy permission ID cho " + name + ": " + e.getMessage());
            }
        }

        return permissionIds;
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
        dbHelper.close();
    }
}