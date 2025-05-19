package com.example.employeemanagementapp.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagementapp.MainActivity;
import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.UserDAO;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginBtn, registerBtn;

    UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout); // layout của form đăng nhập

        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextNumberPassword);
        loginBtn = findViewById(R.id.dang_nhap);
        registerBtn = findViewById(R.id.dang_ky);

        userDAO = new UserDAO(this); // Khởi tạo SQLite helper

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();
                Log.e("a","a");
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isValid = userDAO.checkLogin(username, password);
                    if (isValid) {
                        int userId = userDAO.checkLoginAndGetUserId(username, password);
                        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("authUserId", userId); // ví dụ userId = 123
                        editor.apply();

                        // Đăng nhập thành công
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // không quay lại login
                    } else {
                        // Sai tài khoản
                        Toast.makeText(LoginActivity.this, "Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        registerBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
