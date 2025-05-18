package com.example.employeemanagementapp.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.RoleDAO;
import com.example.employeemanagementapp.db.dao.UserDAO;
import com.example.employeemanagementapp.db.model.Role;
import com.example.employeemanagementapp.db.model.User;

import java.util.List;

public class EditUserActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Spinner spinnerRole;
    private UserDAO userDAO;
    private RoleDAO roleDAO;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        edtUsername = findViewById(R.id.editUsername);
        edtPassword = findViewById(R.id.editPassword);

        userDAO = new UserDAO(this);

        // Lấy userId từ Intent với key "userId" (khớp với UserDetailsActivity)
        userId = getIntent().getIntExtra("userId", -1);

        // Kiểm tra userId hợp lệ
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }



        spinnerRole = findViewById(R.id.spinnerRole);

        roleDAO = new RoleDAO(this);
        List<Role> roles = roleDAO.getAllRoles();

        // Adapter với dữ liệu từ SQLite
        ArrayAdapter<Role> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        // Tải dữ liệu người dùng và điền vào EditText
        loadUserData(userId);

        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDAO != null) {
            userDAO.close();
        }
    }
    // Hàm tải thông tin người dùng vào EditText
    private void loadUserData(int userId) {
        User user = userDAO.getUserById(userId);

        if (user != null) {
            edtUsername.setText(user.getUsername());
            edtPassword.setText(user.getPassword());
            int roleId = userDAO.getUserRole(userId); // hoặc user.getRoleId() nếu đã gán

            // Lấy Role từ roleId
            Role role = roleDAO.getRoleById(roleId);

            if (role != null) {
                String userRoleName = role.getName();

                // Tìm và set vị trí spinner theo tên role
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerRole.getAdapter();
                if (adapter != null) {
                    int spinnerPosition = adapter.getPosition(userRoleName);
                    if (spinnerPosition >= 0) {
                        spinnerRole.setSelection(spinnerPosition);
                    }
                }
            }
        } else {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Hàm xử lý chỉnh sửa thông tin người dùng
    public void editUser(View view) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Tên đăng nhập và mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        User updatedUser = new User(userId, username, password);
        int rows = userDAO.updateUser(updatedUser);
        boolean isUpdated = rows > 0;

        if (isUpdated) {
            Toast.makeText(this, "Cập nhật người dùng thành công", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK); // Thông báo cho UserDetailsActivity cập nhật giao diện
            finish(); // Quay lại UserDetailsActivity
        } else {
            Toast.makeText(this, "Cập nhật người dùng thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}