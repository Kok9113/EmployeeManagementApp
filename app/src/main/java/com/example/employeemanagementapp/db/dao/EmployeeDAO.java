package com.example.employeemanagementapp.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
        values.put(Constants.COLUMN_JOB, emp.getJobTitle());
        return db.insert(Constants.TABLE_EMPLOYEE, null, values);
    }

    public Cursor getAllEmployees() {
        return db.query(Constants.TABLE_EMPLOYEE, null, null, null, null, null, null);
    }

    public int updateEmployee(long id, Employee emp, byte[] image) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_FIRST_NAME, emp.getFirstName());
        values.put(Constants.COLUMN_LAST_NAME, emp.getLastName());
        values.put(Constants.COLUMN_IMAGE, image);
        values.put(Constants.COLUMN_PHONE_NUMBER, emp.getPhoneNumber());
        values.put(Constants.COLUMN_EMAIL, emp.getEmail());
        values.put(Constants.COLUMN_RESIDENCE, emp.getResidence());
        values.put(Constants.COLUMN_JOB, emp.getJobTitle());
        return db.update(Constants.TABLE_EMPLOYEE, values, Constants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteEmployee(long id) {
        return db.delete(Constants.TABLE_EMPLOYEE, Constants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor getEmployeeById(long id) {
        return db.query(Constants.TABLE_EMPLOYEE,
                null,
                Constants.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
    }

    public Cursor getAllEmployeesFiltered(String query) {
        String selection = Constants.COLUMN_FIRST_NAME + " LIKE ? OR " + Constants.COLUMN_LAST_NAME + " LIKE ?";
        String[] args = new String[]{"%" + query + "%", "%" + query + "%"};
        return db.query(Constants.TABLE_EMPLOYEE, null, selection, args, null, null, null);
    }

    public byte[] getEmployeeProfileImage(long id) {
        Cursor cursor = db.query(Constants.TABLE_EMPLOYEE,
                new String[]{Constants.COLUMN_IMAGE},
                Constants.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow(Constants.COLUMN_IMAGE));
            cursor.close();
            return image;
        }
        return null;
    }
}
