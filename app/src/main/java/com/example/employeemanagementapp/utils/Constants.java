package com.example.employeemanagementapp.utils;

public class Constants {
    public static final String DATABASE_NAME = "employees.db";
    public static final int DATABASE_VERSION = 8;

    public static final String TABLE_EMPLOYEE = "employees";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_RESIDENCE = "residence";
    public static final String COLUMN_DEPARTMENT_ID = "department_id";
    public static final String COLUMN_POSITION = "position";


    // Department table
    public static final String TABLE_DEPARTMENTS = "departments";
    public static final String COLUMN_DEPT_ID = "_id";
    public static final String COLUMN_DEPT_NAME = "name";
    public static final String COLUMN_DEPT_POSITIONS = "positions";

    // User table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";

    // Role Table
    public static final String TABLE_ROLES = "Roles";
    public static final String COLUMN_ROLE_ID = "_id";
    public static final String COLUMN_ROLE_IDD = "role_id";
    public static final String COLUMN_ROLE_NAME = "name";

    // Permission
    public static final String TABLE_PERMISSIONS = "Permissions";
    public static final String COLUMN_PERMISSION_ID = "_id";
    public static final String COLUMN_PERMISSION_IDD = "permission_id";
    public static final String COLUMN_PERMISSION_NAME = "name";

    //
    public static final String TABLE_ROLE_PERMISSIONS = "RolePermissions";
    public static final String TABLE_USER_ROLES = "UserRoles";
}
