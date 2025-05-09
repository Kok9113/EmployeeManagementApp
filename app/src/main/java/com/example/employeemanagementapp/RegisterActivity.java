package com.example.employeemanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagementapp.db.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {

    EditText emailField, passwordField1, passwordField2;
    Button btnRegister, btnBack;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout); // layout XML chứa form đăng ký

        emailField = findViewById(R.id.txtdangky);
        passwordField1 = findViewById(R.id.txtpass1);
        passwordField2 = findViewById(R.id.txtpass2);
        btnRegister = findViewById(R.id.dang_ky2);
        btnBack = findViewById(R.id.quay_lai);

        dbHelper = new DatabaseHelper(this); // Khởi tạo SQLite helper

        btnRegister.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String pass1 = passwordField1.getText().toString();
            String pass2 = passwordField2.getText().toString();

            if (email.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else if (!pass1.equals(pass2)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            } else {
                // Thực hiện đăng ký với SQLite
                boolean isRegistered = dbHelper.registerUser(email, pass1);
                if (isRegistered) {
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    // Chuyển về LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Email đã tồn tại, vui lòng chọn email khác", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack.setOnClickListener(v -> {
            finish(); // quay lại LoginActivity
        });
    }
}
