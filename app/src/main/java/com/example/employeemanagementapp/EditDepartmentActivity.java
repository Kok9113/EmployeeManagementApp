package com.example.employeemanagementapp;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.db.DatabaseHelper;

import java.util.Locale;

public class EditDepartmentActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private long departmentId;
    private EditText editTextDeptName, editTextDeptPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_edit_department);

        dbHelper = new DatabaseHelper(this);

        departmentId = getIntent().getLongExtra("departmentId", -1);
        Log.d("EditDepartment", "Received departmentId: " + departmentId);
        if (departmentId == -1) {
            Toast.makeText(this, R.string.invalid_department_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextDeptName = findViewById(R.id.edittext_dept_name);
        editTextDeptPositions = findViewById(R.id.edittext_dept_positions);

        loadDepartmentDetails();

        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView validateIcon = findViewById(R.id.image_validate);
        validateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDepartment();
            }
        });
    }

    private void loadDepartmentDetails() {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getDepartmentById(departmentId);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DEPT_NAME);
                int positionsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DEPT_POSITIONS);

                if (nameIndex == -1 || positionsIndex == -1) {
                    Log.e("EditDepartment", "Column not found: nameIndex=" + nameIndex + ", positionsIndex=" + positionsIndex);
                    Toast.makeText(this, R.string.department_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String name = cursor.getString(nameIndex);
                String positions = cursor.getString(positionsIndex);
                Log.d("EditDepartment", "Loaded department: name=" + name + ", positions=" + positions);

                editTextDeptName.setText(name != null ? name : "");
                editTextDeptPositions.setText(positions != null ? positions : "");
            } else {
                Log.e("EditDepartment", "No department found with ID: " + departmentId);
                Toast.makeText(this, R.string.department_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e("EditDepartment", "Error loading department details: " + e.getMessage());
            Toast.makeText(this, R.string.department_not_found, Toast.LENGTH_SHORT).show();
            finish();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateDepartment() {
        String deptName = editTextDeptName.getText().toString().trim();
        String deptPositions = editTextDeptPositions.getText().toString().trim();

        if (deptName.isEmpty() || deptPositions.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        int rowsAffected = dbHelper.updateDepartment(departmentId, deptName, deptPositions);
        if (rowsAffected > 0) {
            Toast.makeText(this, R.string.department_updated_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.department_updated_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void applyLanguage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedLanguage = preferences.getString("selected_language", "");
        Locale newLocale;
        if (selectedLanguage.equals("Tiếng Việt")) {
            newLocale = new Locale("vi");
        } else {
            newLocale = Locale.ENGLISH;
        }
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.setLocale(newLocale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}