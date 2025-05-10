package com.example.employeemanagementapp.db.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.employeemanagementapp.db.DatabaseHelper;

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
        String queryRolePerms = "SELECT p.name " +
                "FROM Permissions p " +
                "JOIN RolePermissions rp ON p.id = rp.permission_id " +
                "JOIN UserRoles ur ON rp.role_id = ur.role_id " +
                "WHERE ur.user_id = ?";

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
