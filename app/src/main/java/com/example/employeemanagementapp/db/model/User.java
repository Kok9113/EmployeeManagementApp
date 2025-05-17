package com.example.employeemanagementapp.db.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private long user_id; // Sửa từ int thành long để khớp với UserDAO (db.insert trả về long)
    private String username;
    private String password;
    private List<Long> roleIds;       // Danh sách ID vai trò (ví dụ: [1, 2])
    private List<Long> permissionIds; // Danh sách ID quyền (ví dụ: [1, 2])

    // Constructor đầy đủ
    public User(long user_id, String username, String password) {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
        this.roleIds = new ArrayList<>();
        this.permissionIds = new ArrayList<>();
    }

    // Constructor không có id (ví dụ dùng khi đăng ký mới)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.roleIds = new ArrayList<>();
        this.permissionIds = new ArrayList<>();
    }

    // Constructor với id và password
    public User(long userId, String password) {
        this.user_id = userId;
        this.password = password;
        this.roleIds = new ArrayList<>();
        this.permissionIds = new ArrayList<>();
    }

    // Getter & Setter
    public long getId() {
        return user_id;
    }

    public void setId(long id) {
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

    public List<Long> getRoleIds() {
        if (roleIds == null) roleIds = new ArrayList<>();
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds != null ? roleIds : new ArrayList<>();
    }

    public List<Long> getPermissionIds() {
        if (permissionIds == null) permissionIds = new ArrayList<>();
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds != null ? permissionIds : new ArrayList<>();
    }

    // Kiểm tra quyền (cập nhật để làm việc với ID, cần ánh xạ sang tên nếu cần)
    public boolean hasPermission(Long permissionId) {
        return getPermissionIds().contains(permissionId);
    }

    public boolean hasRole(Long roleId) {
        return getRoleIds().contains(roleId);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + user_id +
                ", username='" + username + '\'' +
                ", roleIds=" + roleIds +
                ", permissionIds=" + permissionIds +
                '}';
    }
}