package com.example.employeemanagementapp.ui.role;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.RoleDAO;
import com.example.employeemanagementapp.db.dao.UserDAO;

import java.util.ArrayList;
import java.util.List;

public class AddRoleActivity extends AppCompatActivity {

    private EditText edtRoleName;
    private RoleDAO roleDAO;

    private List<String> selectedPermissions = new ArrayList<>();
    private String[] permissionList = {"READ", "WRITE", "UPDATE", "DELETE"};
    private boolean[] selectedStates = new boolean[permissionList.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_role);

        edtRoleName = findViewById(R.id.edtRoleName);

        roleDAO = new RoleDAO(this);

        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Hàm xử lý thêm người dùng mới
    public void addRole(View view) {
        String roleName = edtRoleName.getText().toString().trim();

        if (roleName.isEmpty()) {
            Toast.makeText(this, "Role name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPermissions.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 quyền", Toast.LENGTH_SHORT).show();
            return;
        }

        long newRoleId = roleDAO.insertRole(roleName);
        if (newRoleId != -1) {
            Toast.makeText(this, "Role added successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish(); // Quay lại trang trước
        } else {
            Toast.makeText(this, "Failed to add role", Toast.LENGTH_SHORT).show();
        }
    }

    public void showPermissionDialog(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_permission_for_role)
                .setMultiChoiceItems(permissionList, selectedStates, (dialog, which, isChecked) -> {
                    selectedStates[which] = isChecked;
                })
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    selectedPermissions.clear();
                    for (int i = 0; i < permissionList.length; i++) {
                        if (selectedStates[i]) {
                            selectedPermissions.add(permissionList[i]);
                        }
                    }
                    updatePermissionText();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updatePermissionText() {
        TextView tv = findViewById(R.id.tvSelectedPermissions);
        if (selectedPermissions.isEmpty()) {
            tv.setText("Chưa chọn quyền nào");
        } else {
            tv.setText("Đã chọn: " + android.text.TextUtils.join(", ", selectedPermissions));
        }
    }
}