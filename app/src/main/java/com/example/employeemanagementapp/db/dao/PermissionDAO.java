package com.example.employeemanagementapp.db.dao;

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
    private final SQLiteDatabase db;

    public PermissionDAO(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    public List<String> getUserPermissions(int userId) {
        Set<String> permissions = new HashSet<>();

        // 1. Permission từ role
        String queryRolePerms = "SELECT p." + Constants.COLUMN_PERMISSION_NAME + " " +
                "FROM " + Constants.TABLE_PERMISSIONS + " p " +
                "JOIN " + Constants.TABLE_ROLE_PERMISSIONS + " rp ON p." + Constants.COLUMN_PERMISSION_ID + " = rp." + Constants.COLUMN_PERMISSION_IDD + " " +
                "JOIN " + Constants.TABLE_USER_ROLES + " ur ON rp." + Constants.COLUMN_ROLE_IDD + " = ur." + Constants.COLUMN_ROLE_IDD + " " +
                "WHERE ur." + Constants.COLUMN_USER_ID + " = ?";

        Cursor cursor1 = db.rawQuery(queryRolePerms, new String[]{String.valueOf(userId)});
        if (cursor1 != null && cursor1.moveToFirst()) {
            do {
                permissions.add(cursor1.getString(0));
            } while (cursor1.moveToNext());
            cursor1.close();
        }

        return new ArrayList<>(permissions);
    }
}
