package com.example.employeemanagementapp.db.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int user_id;
    private String username;
    private String password;
    private List<String> roles;       // Danh sách vai trò (ví dụ: "admin", "manager")
    private List<String> permissions; // Danh sách quyền (ví dụ: "VIEW_EMPLOYEE", "EDIT_USER")

    // Constructor đầy đủ
    public User(int user_id, String username, String password) {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
        this.roles = new ArrayList<>();
        this.permissions = new ArrayList<>();
    }

    // Constructor không có id (ví dụ dùng khi đăng ký mới)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = new ArrayList<>();
        this.permissions = new ArrayList<>();
    }

    public User(int userId, String password) {
    }


    // Getter & Setter
    public int getId() {
        return user_id;
    }

    public void setId(int id) {
        this.user_id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        if (roles == null) roles = new ArrayList<>();
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        if (permissions == null) permissions = new ArrayList<>();
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    // Kiểm tra quyền
    public boolean hasPermission(String permission) {
        return getPermissions().contains(permission);
    }

    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + user_id +
                ", username='" + username + '\'' +
                ", roles=" + roles +
                ", permissions=" + permissions +
                '}';
    }
}

