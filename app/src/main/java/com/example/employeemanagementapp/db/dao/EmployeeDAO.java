package com.example.employeemanagementapp.db.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;

import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.db.model.Employee;
import com.example.employeemanagementapp.utils.Constants;

public class EmployeeDAO {
    private final SQLiteDatabase db;

    public EmployeeDAO(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    public long insertEmployee(Employee emp, byte[] image) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_FIRST_NAME, emp.getFirstName());
        values.put(Constants.COLUMN_LAST_NAME, emp.getLastName());
        values.put(Constants.COLUMN_IMAGE, image);
        values.put(Constants.COLUMN_PHONE_NUMBER, emp.getPhoneNumber());
        values.put(Constants.COLUMN_EMAIL, emp.getEmail());
        values.put(Constants.COLUMN_RESIDENCE, emp.getResidence());
        values.put(Constants.COLUMN_DEPARTMENT_ID, emp.getDepartmentId());
        values.put(Constants.COLUMN_POSITION, emp.getPosition());
        return db.insert(Constants.TABLE_EMPLOYEE, null, values);
    }

    public int updateEmployee(long id, Employee emp, byte[] image) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_FIRST_NAME, emp.getFirstName());
        values.put(Constants.COLUMN_LAST_NAME, emp.getLastName());
        values.put(Constants.COLUMN_IMAGE, image);
        values.put(Constants.COLUMN_PHONE_NUMBER, emp.getPhoneNumber());
        values.put(Constants.COLUMN_EMAIL, emp.getEmail());
        values.put(Constants.COLUMN_RESIDENCE, emp.getResidence());
        values.put(Constants.COLUMN_DEPARTMENT_ID, emp.getDepartmentId());
        values.put(Constants.COLUMN_POSITION, emp.getPosition());
        return db.update(Constants.TABLE_EMPLOYEE, values, Constants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor getEmployeesByDepartment(long deptId) {
        String query = "SELECT * FROM " + Constants.TABLE_EMPLOYEE + " WHERE " + Constants.COLUMN_DEPARTMENT_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(deptId)});
    }

    // New method: Filter employees by department and name query
    public Cursor getEmployeesByDepartmentFiltered(long deptId, String query) {
        String selection = Constants.COLUMN_DEPARTMENT_ID + " = ? AND (" + Constants.COLUMN_FIRST_NAME + " LIKE ? OR " + Constants.COLUMN_LAST_NAME + " LIKE ?)";
        String[] selectionArgs = new String[]{String.valueOf(deptId), "%" + query + "%", "%" + query + "%"};
        return db.query(Constants.TABLE_EMPLOYEE, null, selection, selectionArgs, null, null, null);
    }

    public boolean hasEmployeesInDepartment(long departmentId) {
        Cursor cursor = db.query(Constants.TABLE_EMPLOYEE, new String[]{Constants.COLUMN_ID},
                Constants.COLUMN_DEPARTMENT_ID + "=?", new String[]{String.valueOf(departmentId)},
                null, null, null);
        boolean hasEmployees = cursor.getCount() > 0;
        cursor.close();
        return hasEmployees;
    }


    public Cursor getAllEmployees() {
        return db.rawQuery("SELECT * FROM " + Constants.TABLE_EMPLOYEE, null);
    }

    public Cursor getEmployeeById(long id) {
        return db.query(Constants.TABLE_EMPLOYEE, null, Constants.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }

    public byte[] getEmployeeProfileImage(long employeeId) {
        Cursor cursor = db.query(Constants.TABLE_EMPLOYEE, new String[]{Constants.COLUMN_IMAGE}, Constants.COLUMN_ID + "=?", new String[]{String.valueOf(employeeId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") byte[] image = cursor.getBlob(cursor.getColumnIndex(Constants.COLUMN_IMAGE));
            cursor.close();
            return image;
        }
        return null;
    }

    public int deleteEmployee(long id) {
        int rowsDeleted = db.delete(Constants.TABLE_EMPLOYEE, Constants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted;
    }

    public Cursor getAllEmployeesFiltered(String query) {
        String selection = Constants.COLUMN_FIRST_NAME + " LIKE ? OR " + Constants.COLUMN_LAST_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};
        return db.query(Constants.TABLE_EMPLOYEE, null, selection, selectionArgs, null, null, null);
    }


}