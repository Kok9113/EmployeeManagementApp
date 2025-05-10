package com.example.employeemanagementapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.adapter.employee.EmployeeGridAdapter;
import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.DepartmentActivity;
import com.example.employeemanagementapp.db.dao.PermissionDAO;
import com.example.employeemanagementapp.db.dao.RoleDAO;
import com.example.employeemanagementapp.db.model.User;
import com.example.employeemanagementapp.ui.employee.AddEmployeeActivity;
import com.example.employeemanagementapp.ui.employee.EmployeeDetails;
import com.example.employeemanagementapp.ui.setting.SettingsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_EMPLOYEE_REQUEST_CODE = 1;

    private DatabaseHelper dbHelper;
    private SimpleCursorAdapter listAdapter;
    private EmployeeGridAdapter gridAdapter;
    private EditText searchInput;
    private ImageView settings;
    private ListView listView;
    private GridLayout gridLayout;
    private TextView noEmployeesText;
    private TextView headerTitle;
    private HashMap<Long, String> departmentMap;
    private LinearLayout menuPanel;
    private View overlay;
    private ImageView imageMenu;
    private ImageView imageSortDepartment;
    private Button filterDepartmentButton;
    private boolean isMenuOpen = false;
    private long selectedDeptId = -1; // -1 means all departments

    private BroadcastReceiver languageChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("LANGUAGE_CHANGED".equals(intent.getAction())) {
                String newLanguage = intent.getStringExtra("new_language");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putString("selected_language", newLanguage).apply();
                applyLanguage();
                recreate(); // Tái tạo MainActivity để áp dụng ngôn ngữ mới
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_main);

        // Đăng ký BroadcastReceiver với cờ RECEIVER_NOT_EXPORTED
        IntentFilter filter = new IntentFilter("LANGUAGE_CHANGED");
        registerReceiver(languageChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        searchInput = findViewById(R.id.search_input);
        settings = findViewById(R.id.settings);


        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            PermissionDAO permissionDAO = new PermissionDAO(this);
            List<String> permissions = permissionDAO.getUserPermissions(userId);
            RoleDAO roleDAO = new RoleDAO(this);
            List<String> roles = roleDAO.getUserRoles(userId); // tùy dùng hoặc bỏ

            User user = new User(userId, "");
            user.setPermissions(permissions);
            user.setRoles(roles);


            //Dùng quyền để điều khiển UI
            if (user.hasPermission("EDIT_EMPLOYEE")) {
                settings.setVisibility(View.GONE);
            }

            if (user.hasRole("admin")) {
                searchInput.setVisibility(View.GONE);
            }
        }


        dbHelper = new DatabaseHelper(this);
        departmentMap = new HashMap<>();
        loadDepartments();

        listView = findViewById(R.id.listview);
        gridLayout = findViewById(R.id.gridlayout);
        noEmployeesText = findViewById(R.id.text_no_employees);
        headerTitle = findViewById(R.id.text_employee_list);
        menuPanel = findViewById(R.id.menu_panel);

        if (listView == null || gridLayout == null) {
            Log.e("MainActivity", "ListView or GridLayout not found in layout");
            Toast.makeText(this, "Error: Layout components not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        displayEmployees();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                filterEmployeeList(query);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        imageMenu = findViewById(R.id.image_menu);
        imageSortDepartment = findViewById(R.id.image_sort_department);
        overlay = findViewById(R.id.overlay);

        imageMenu.setOnClickListener(v -> {
            if (isMenuOpen) {
                closeMenu();
            } else {
                openMenu();
            }
        });

        imageSortDepartment.setOnClickListener(v -> showDepartmentListDialog());

        overlay.setOnClickListener(v -> closeMenu());

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

        filterDepartmentButton = findViewById(R.id.button_add_department);
        if (filterDepartmentButton != null) {
            filterDepartmentButton.setOnClickListener(v -> showDepartmentListDialog());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(languageChangeReceiver); // Hủy đăng ký receiver khi hoạt động bị hủy
    }

    private void showDepartmentListDialog() {
        Cursor cursor = dbHelper.getAllDepartments();
        ArrayList<String> departmentNames = new ArrayList<>();
        ArrayList<Long> departmentIds = new ArrayList<>();

        // Add "All Departments" option
        departmentNames.add(getString(R.string.all_departments));
        departmentIds.add(-1L);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_DEPT_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DEPT_NAME));
                departmentNames.add(name != null ? name : "Unknown");
                departmentIds.add(id);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (departmentNames.isEmpty()) {
            Toast.makeText(this, R.string.no_departments_found, Toast.LENGTH_SHORT).show();
            return;
        }

        // Custom ArrayAdapter for department list
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.department_list_item, R.id.department_name, departmentNames) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.department_name);
                ImageView checkIcon = view.findViewById(R.id.check_icon);

                // Highlight selected department
                long deptId = departmentIds.get(position);
                if (deptId == selectedDeptId) {
                    view.setBackgroundColor(getResources().getColor(R.color.selected_item_background));
                    textView.setTextColor(getResources().getColor(R.color.selected_item_text));
                    checkIcon.setVisibility(View.VISIBLE);
                } else {
                    view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    textView.setTextColor(getResources().getColor(android.R.color.black));
                    checkIcon.setVisibility(View.GONE);
                }

                return view;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_department)
                .setAdapter(adapter, (dialog, which) -> {
                    long deptId = departmentIds.get(which);
                    selectedDeptId = deptId;
                    filterEmployeesByDepartment(deptId);
                    updateHeaderTitle();
                    searchInput.setText(""); // Clear search input
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateHeaderTitle() {
        if (selectedDeptId == -1) {
            headerTitle.setText(R.string.list);
        } else {
            String deptName = departmentMap.getOrDefault(selectedDeptId, "Unknown");
            headerTitle.setText(getString(R.string.employees_in_department, deptName));
        }
    }

    private void filterEmployeesByDepartment(long deptId) {
        Cursor cursor = null;
        try {
            if (deptId == -1) {
                cursor = dbHelper.getAllEmployees();
            } else {
                cursor = dbHelper.getEmployeesByDepartment(deptId);
            }
            if (cursor == null) {
                Log.e("MainActivity", "Cursor is null in filterEmployeesByDepartment for deptId: " + deptId);
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
            Log.e("MainActivity", "Error filtering employees by department: " + e.getMessage());
            listView.setAdapter(null);
            gridLayout.removeAllViews();
            if (noEmployeesText != null) {
                noEmployeesText.setVisibility(View.VISIBLE);
            }
        }


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        applyLanguage();
        departmentMap.clear();
        loadDepartments();
        displayEmployees();
        updateHeaderTitle();
    }

    private void openMenu() {
        menuPanel.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.VISIBLE);
        overlay.animate().alpha(0.5f).setDuration(200).start();
        menuPanel.setTranslationX(-menuPanel.getWidth());
        menuPanel.animate().translationX(0).setDuration(300).start();
        isMenuOpen = true;
        refreshMenu(); // Làm mới menu khi mở
    }

    private void closeMenu() {
        overlay.animate().alpha(0f).setDuration(200).withEndAction(() -> overlay.setVisibility(View.GONE)).start();
        menuPanel.animate().translationX(-menuPanel.getWidth()).setDuration(300).withEndAction(() -> menuPanel.setVisibility(View.GONE)).start();
        isMenuOpen = false;
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
            cursor = selectedDeptId == -1 ? dbHelper.getAllEmployees() : dbHelper.getEmployeesByDepartment(selectedDeptId);
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
        }
    }

    private void filterEmployeeList(String query) {
        Cursor cursor = null;
        try {
            if (selectedDeptId == -1) {
                cursor = dbHelper.getAllEmployeesFiltered(query);
            } else {
                cursor = dbHelper.getEmployeesByDepartmentFiltered(selectedDeptId, query);
            }
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

    public void GoToSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        closeMenu();
    }

    public void goToDepartments(View view) {
        Intent intent = new Intent(MainActivity.this, DepartmentActivity.class);
        startActivity(intent);
        closeMenu();
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
        if (selectedLanguage != null && selectedLanguage.equals("Tiếng Việt")) {
            newLocale = new Locale("vi");
        } else {
            newLocale = Locale.ENGLISH;
        }
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.setLocale(newLocale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        refreshMenu(); // Làm mới menu sau khi áp dụng ngôn ngữ
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyLanguage();
        updateHeaderTitle();
    }

    private void refreshMenu() {
        if (menuPanel != null) {
            // Tìm LinearLayout chứa TextView
            LinearLayout departmentsLayout = menuPanel.findViewById(R.id.menu_departments);
            LinearLayout settingsLayout = menuPanel.findViewById(R.id.menu_settings);

            // Tìm TextView bên trong LinearLayout
            if (departmentsLayout != null) {
                TextView departmentsText = departmentsLayout.findViewById(R.id.text_department);
                if (departmentsText != null) {
                    departmentsText.setText(getString(R.string.menu_departments));
                }
            }
            if (settingsLayout != null) {
                TextView settingsText = settingsLayout.findViewById(R.id.text_settings);
                if (settingsText != null) {
                    settingsText.setText(getString(R.string.menu_settings));
                }
            }
        }
    }
}