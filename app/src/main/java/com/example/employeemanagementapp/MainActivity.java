package com.example.employeemanagementapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.adapter.employee.EmployeeGridAdapter;
import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.DepartmentActivity;
import com.example.employeemanagementapp.ui.employee.AddEmployeeActivity;
import com.example.employeemanagementapp.ui.employee.EmployeeDetails;
import com.example.employeemanagementapp.ui.setting.SettingsActivity;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_EMPLOYEE_REQUEST_CODE = 1;

    private DatabaseHelper dbHelper;
    private SimpleCursorAdapter listAdapter;
    private EmployeeGridAdapter gridAdapter;
    private EditText searchInput;
    private ListView listView;
    private GridLayout gridLayout;
    private TextView noEmployeesText;
    private HashMap<Long, String> departmentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        departmentMap = new HashMap<>();
        loadDepartments(); // Tải danh sách phòng ban

        listView = findViewById(R.id.listview);
        gridLayout = findViewById(R.id.gridlayout);
        noEmployeesText = findViewById(R.id.text_no_employees); // Thêm TextView để hiển thị thông báo

        if (listView == null || gridLayout == null) {
            Log.e("MainActivity", "ListView or GridLayout not found in layout");
            Toast.makeText(this, "Error: Layout components not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        displayEmployees();

        searchInput = findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                filterEmployeeList(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageView menuIcon = findViewById(R.id.image_menu);
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleListViewGridLayout();
            }
        });
        ImageView listIcon = findViewById(R.id.list);
        listIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleListViewGridLayout();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                @SuppressLint("Range") long employeeId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                showEmployeeDetails(employeeId);
            }
        });

        findViewById(R.id.button_add_employee).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEmployeeActivity.class);
                startActivityForResult(intent, ADD_EMPLOYEE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        applyLanguage();
        departmentMap.clear();
        loadDepartments();
        displayEmployees();
    }

    private void loadDepartments() {
        Cursor cursor = dbHelper.getAllDepartments();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_DEPT_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DEPT_NAME));
                departmentMap.put(id, name != null ? name : "Unknown");
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void displayEmployees() {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllEmployees();
            if (cursor != null && cursor.moveToFirst()) {
                if (listAdapter == null) {
                    listAdapter = new SimpleCursorAdapter(
                            this,
                            R.layout.list_item_layout,
                            cursor,
                            new String[]{DatabaseHelper.COLUMN_FIRST_NAME, DatabaseHelper.COLUMN_LAST_NAME, DatabaseHelper.COLUMN_POSITION},
                            new int[]{R.id.text_name, R.id.text_lastname, R.id.text_job},
                            0) {
                        @Override
                        public void setViewText(TextView v, String text) {
                            if (v.getId() == R.id.text_job) {
                                Cursor cursor = getCursor();
                                if (cursor == null) {
                                    Log.e("MainActivity", "Cursor is null in setViewText");
                                    super.setViewText(v, "Unknown");
                                    return;
                                }
                                try {
                                    int positionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_POSITION);
                                    int deptIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DEPARTMENT_ID);
                                    String position = positionIndex != -1 && cursor.getString(positionIndex) != null ? cursor.getString(positionIndex) : "";
                                    long deptId = deptIdIndex != -1 ? cursor.getLong(deptIdIndex) : -1;
                                    String deptName = deptId != -1 ? departmentMap.getOrDefault(deptId, "Unknown") : "Unknown";
                                    text = deptName + " - " + position;
                                } catch (Exception e) {
                                    Log.e("MainActivity", "Error in setViewText: " + e.getMessage());
                                    text = "Unknown";
                                }
                            }
                            super.setViewText(v, text);
                        }
                    };
                    listView.setAdapter(listAdapter);
                } else {
                    listAdapter.changeCursor(cursor);
                }
                listView.setVisibility(View.VISIBLE);
                gridLayout.setVisibility(View.GONE);
                if (noEmployeesText != null) {
                    noEmployeesText.setVisibility(View.GONE);
                }

                if (gridAdapter == null) {
                    gridAdapter = new EmployeeGridAdapter(this, cursor, departmentMap);
                    updateGridLayout(gridLayout, cursor);
                } else {
                    gridAdapter.changeCursor(cursor);
                    updateGridLayout(gridLayout, cursor);
                }
            } else {
                Log.d("Employee Details", "No employees found in the database.");
                listView.setVisibility(View.GONE);
                gridLayout.setVisibility(View.GONE);
                listView.setAdapter(null);
                gridLayout.removeAllViews();
                if (noEmployeesText != null) {
                    noEmployeesText.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, R.string.no_employees_found, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("Employee Details", "Error accessing database: " + e.getMessage());
            Toast.makeText(this, "Error loading employees: " + e.getMessage(), Toast.LENGTH_LONG).show();
            listView.setVisibility(View.GONE);
            gridLayout.setVisibility(View.GONE);
            if (noEmployeesText != null) {
                noEmployeesText.setVisibility(View.VISIBLE);
            }
        } finally {
            // Đảm bảo không đóng cursor vì nó được quản lý bởi adapter
        }
    }

    private void filterEmployeeList(String query) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllEmployeesFiltered(query);
            if (cursor == null) {
                Log.e("MainActivity", "Cursor is null in filterEmployeeList for query: " + query);
                listView.setAdapter(null);
                gridLayout.removeAllViews();
                if (noEmployeesText != null) {
                    noEmployeesText.setVisibility(View.VISIBLE);
                }
                return;
            }
            listAdapter.changeCursor(cursor);
            updateGridLayout(gridLayout, cursor);
            if (noEmployeesText != null) {
                noEmployeesText.setVisibility(cursor.moveToFirst() ? View.GONE : View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error filtering employees: " + e.getMessage());
            listView.setAdapter(null);
            gridLayout.removeAllViews();
            if (noEmployeesText != null) {
                noEmployeesText.setVisibility(View.VISIBLE);
            }
        }
    }

    @SuppressLint("Range")
    private void updateGridLayout(GridLayout gridLayout, Cursor cursor) {
        gridLayout.removeAllViews();
        if (cursor == null || !cursor.moveToFirst()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        while (!cursor.isAfterLast()) {
            View itemView = inflater.inflate(R.layout.grid_item_layout, gridLayout, false);
            TextView nameTextView = itemView.findViewById(R.id.text_name);
            TextView lastNameTextView = itemView.findViewById(R.id.text_lastname);
            TextView jobTextView = itemView.findViewById(R.id.text_job);

            String firstName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LAST_NAME));
            long deptId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_DEPARTMENT_ID));
            String position = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_POSITION)) != null ?
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_POSITION)) : "";

            String deptName = departmentMap.getOrDefault(deptId, "Unknown");
            nameTextView.setText(firstName != null ? firstName : "");
            lastNameTextView.setText(lastName != null ? lastName : "");
            jobTextView.setText(deptName + " - " + position);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(8, 8, 8, 8);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f);
            itemView.setLayoutParams(params);

            final long employeeId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEmployeeDetails(employeeId);
                }
            });

            gridLayout.addView(itemView);
            cursor.moveToNext();
        }
    }

    private void showEmployeeDetails(long employeeId) {
        Intent intent = new Intent(MainActivity.this, EmployeeDetails.class);
        intent.putExtra("employeeId", employeeId);
        startActivity(intent);
    }

    private void toggleListViewGridLayout() {
        if (listView.getVisibility() == View.VISIBLE) {
            listView.setVisibility(View.GONE);
            gridLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.list).setVisibility(View.VISIBLE);
            findViewById(R.id.image_menu).setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.VISIBLE);
            gridLayout.setVisibility(View.GONE);
            findViewById(R.id.list).setVisibility(View.GONE);
            findViewById(R.id.image_menu).setVisibility(View.VISIBLE);
        }
    }

    public void GoToSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void goToDepartments(View view) {
        Intent intent = new Intent(MainActivity.this, DepartmentActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_EMPLOYEE_REQUEST_CODE && resultCode == RESULT_OK) {
            displayEmployees();
        }
    }

    private void applyLanguage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedLanguage = preferences.getString("selected_language", "");
        Log.d("selected language", selectedLanguage);
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