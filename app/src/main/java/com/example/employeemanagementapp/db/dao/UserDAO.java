package com.example.employeemanagementapp.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.db.model.User;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private final SQLiteDatabase db;

    public UserDAO(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    // Lấy tất cả người dùng
    public Cursor getAllUsers() {
        return db.rawQuery("SELECT user_id AS _id, username, password FROM " + Constants.TABLE_USERS, null);
    }

    // Tìm kiếm người dùng theo tên
    public Cursor searchUsers(String keyword) {
        String query = "SELECT user_id AS _id, username, password FROM " + Constants.TABLE_USERS + " WHERE " + Constants.COLUMN_USER_NAME + " LIKE ?";
        return db.rawQuery(query, new String[]{"%" + keyword + "%"});
    }

    // Các phương thức khác giữ nguyên...
    public boolean registerUser(String username, String password) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_NAME, username);
        values.put(Constants.COLUMN_USER_PASSWORD, password);

        Cursor cursor = db.query(Constants.TABLE_USERS, null,
                Constants.COLUMN_USER_NAME + " = ?", new String[]{username},
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();

        if (exists) {
            return false;
        }

        long result = db.insert(Constants.TABLE_USERS, null, values);
        return result != -1;
    }

    public User getUserById(int userId) {
        Cursor cursor = db.query(
                Constants.TABLE_USERS,
                null,
                Constants.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_NAME));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_PASSWORD));
            cursor.close();
            return new User(userId, username, password);
        }

        if (cursor != null) {
            cursor.close();
        }

        return null;
    }

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

    public long addUser(User user) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_NAME, user.getUsername());
        values.put(Constants.COLUMN_USER_PASSWORD, user.getPassword());

        long userId = db.insert(Constants.TABLE_USERS, null, values);

        if (userId != -1) {
            addUserRoles(userId, user.getRoleIds());
            addUserPermissions(userId, user.getPermissionIds());
        }
        return userId;
    }

    private void addUserRoles(long userId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            Cursor cursor = db.query(Constants.TABLE_USER_ROLES, null,
                    Constants.COLUMN_USER_ROLE_USER_ID + " = ? AND " + Constants.COLUMN_USER_ROLE_ROLE_ID + " = ?",
                    new String[]{String.valueOf(userId), String.valueOf(roleId)}, null, null, null);

            if (cursor.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(Constants.COLUMN_USER_ROLE_USER_ID, userId);
                values.put(Constants.COLUMN_USER_ROLE_ROLE_ID, roleId);
                db.insert(Constants.TABLE_USER_ROLES, null, values);
            }
            cursor.close();
        }
    }

    private void addUserPermissions(long userId, List<Long> permissionIds) {
        for (Long permissionId : permissionIds) {
            Cursor cursor = db.query(Constants.TABLE_USER_PERMISSIONS, null,
                    Constants.COLUMN_USER_PERMISSION_USER_ID + " = ? AND " + Constants.COLUMN_USER_PERMISSION_PERMISSION_ID + " = ?",
                    new String[]{String.valueOf(userId), String.valueOf(permissionId)}, null, null, null);

            if (cursor.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(Constants.COLUMN_USER_PERMISSION_USER_ID, userId);
                values.put(Constants.COLUMN_USER_PERMISSION_PERMISSION_ID, permissionId);
                db.insert(Constants.TABLE_USER_PERMISSIONS, null, values);
            }
            cursor.close();
        }
    }

    public int updateUser(User user) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_NAME, user.getUsername());
        values.put(Constants.COLUMN_USER_PASSWORD, user.getPassword());

        return db.update(Constants.TABLE_USERS, values,
                Constants.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(user.getId())});
    }

    public int updateUsername(long userId, String newUsername) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_NAME, newUsername);

        return db.update(Constants.TABLE_USERS, values,
                Constants.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    public int updatePassword(long userId, String newPassword) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_PASSWORD, newPassword);

        return db.update(Constants.TABLE_USERS, values,
                Constants.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    public void deleteUser(long userId) {
        db.delete(Constants.TABLE_USER_ROLES, Constants.COLUMN_USER_ROLE_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.delete(Constants.TABLE_USER_PERMISSIONS, Constants.COLUMN_USER_PERMISSION_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.delete(Constants.TABLE_USERS, Constants.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    public List<Long> getUserRoles(long userId) {
        List<Long> roles = new ArrayList<>();
        String query = "SELECT " + Constants.COLUMN_USER_ROLE_ROLE_ID + " FROM " + Constants.TABLE_USER_ROLES +
                " WHERE " + Constants.COLUMN_USER_ROLE_USER_ID + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            int roleIdIndex = cursor.getColumnIndex(Constants.COLUMN_USER_ROLE_ROLE_ID);
            if (roleIdIndex >= 0 && cursor.moveToFirst()) {
                do {
                    long roleId = cursor.getLong(roleIdIndex);
                    roles.add(roleId);
                } while (cursor.moveToNext());
            } else if (roleIdIndex < 0) {
                android.util.Log.e("UserDAO", "Cột " + Constants.COLUMN_USER_ROLE_ROLE_ID + " không tồn tại trong truy vấn");
            }
        }
        return roles;
    }

    public List<Long> getUserPermissions(long userId) {
        List<Long> permissions = new ArrayList<>();
        String query = "SELECT " + Constants.COLUMN_USER_PERMISSION_PERMISSION_ID + " FROM " + Constants.TABLE_USER_PERMISSIONS +
                " WHERE " + Constants.COLUMN_USER_PERMISSION_USER_ID + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            int permissionIdIndex = cursor.getColumnIndex(Constants.COLUMN_USER_PERMISSION_PERMISSION_ID);
            if (permissionIdIndex >= 0 && cursor.moveToFirst()) {
                do {
                    long permissionId = cursor.getLong(permissionIdIndex);
                    permissions.add(permissionId);
                } while (cursor.moveToNext());
            } else if (permissionIdIndex < 0) {
                android.util.Log.e("UserDAO", "Cột " + Constants.COLUMN_USER_PERMISSION_PERMISSION_ID + " không tồn tại trong truy vấn");
            }
        }
        return permissions;
    }
}