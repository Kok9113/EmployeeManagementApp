package com.example.employeemanagementapp.ui.user; // Cập nhật package

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.UserDAO;
import com.example.employeemanagementapp.utils.Constants;

public class AddUserActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);

        userDAO = new UserDAO(this);
    }

    // Hàm xử lý thêm người dùng mới
    public void addUser(View view) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isRegistered = userDAO.registerUser(username, password);
        if (isRegistered) {
            Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show();
            finish(); // Quay lại trang trước
        } else {
            Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show();
        }
    }
}