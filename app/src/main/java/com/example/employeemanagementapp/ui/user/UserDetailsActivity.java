package com.example.employeemanagementapp.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.UserDAO;
import com.example.employeemanagementapp.db.model.User;

public class UserDetailsActivity extends AppCompatActivity {

    private static final int EDIT_USER_REQUEST_CODE = 2;

    private TextView textUsername, textPassword;
    private Button buttonDelete, buttonEdit, buttonBack;
    private UserDAO userDAO;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        textUsername = findViewById(R.id.text_username);
        textPassword = findViewById(R.id.text_password);
        buttonDelete = findViewById(R.id.button_delete_user);
        buttonEdit = findViewById(R.id.button_edit_user);

        userDAO = new UserDAO(this);

        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayUserDetails();

        buttonDelete.setOnClickListener(v -> {
            userDAO.deleteUser(userId);
            Toast.makeText(this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });

        buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(UserDetailsActivity.this, EditUserActivity.class);
            intent.putExtra("userId", userId);
            startActivityForResult(intent, EDIT_USER_REQUEST_CODE);
        });

        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void displayUserDetails() {
        User user = userDAO.getUserById(userId);
        if (user != null) {
            textUsername.setText("Tên đăng nhập: " + user.getUsername());
            textPassword.setText("Mật khẩu: " + user.getPassword());
        } else {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_USER_REQUEST_CODE && resultCode == RESULT_OK) {
            displayUserDetails(); // Cập nhật thông tin sau khi sửa
            setResult(RESULT_OK); // Thông báo cho UserActivity cập nhật danh sách
        }
    }
}