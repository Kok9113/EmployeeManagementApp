package com.example.employeemanagementapp.ui.employee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.db.dao.DepartmentDAO;
import com.example.employeemanagementapp.db.dao.EmployeeDAO;
import com.example.employeemanagementapp.db.model.Employee;
import com.example.employeemanagementapp.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddEmployeeActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextEmail, editTextResidence;
    private Spinner spinnerDepartment, spinnerPosition;
    private ImageView imageViewValidate, imageViewBack, imageView;
    private EmployeeDAO employeeDAO;
    private DepartmentDAO departmentDAO;
    private List<Department> departments;
    private long selectedDepartmentId;

    private static class Department {
        long id;
        String name;
        String[] positions;

        Department(long id, String name, String positions) {
            this.id = id;
            this.name = name;
            this.positions = positions != null ? positions.split(",\\s*") : new String[]{};
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_add_employee);

        editTextFirstName = findViewById(R.id.edittext_first_name);
        editTextLastName = findViewById(R.id.edittext_last_name);
        editTextPhoneNumber = findViewById(R.id.edittext_phone_number);
        editTextEmail = findViewById(R.id.edittext_email);
        editTextResidence = findViewById(R.id.edittext_residence);
        spinnerDepartment = findViewById(R.id.spinner_department);
        spinnerPosition = findViewById(R.id.spinner_position);
        imageViewValidate = findViewById(R.id.image_validate);
        imageViewBack = findViewById(R.id.image_back);
        imageView = findViewById(R.id.image_profile);
        imageView.setImageResource(R.drawable.ic_launcher_background);

        employeeDAO = new EmployeeDAO(this);
        departmentDAO = new DepartmentDAO(this);

        loadDepartments();

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageViewValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEmployee();
            }
        });

        spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartmentId = departments.get(position).id;
                updatePositionSpinner(departments.get(position).positions);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDepartmentId = -1;
                updatePositionSpinner(new String[]{});
            }
        });
    }

    private void loadDepartments() {
        departments = new ArrayList<>();
        Cursor cursor = departmentDAO.getAllDepartments();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DEPT_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DEPT_NAME));
                @SuppressLint("Range") String positions = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DEPT_POSITIONS));
                departments.add(new Department(id, name, positions));
            } while (cursor.moveToNext());
            cursor.close();
        }

        List<String> departmentNames = new ArrayList<>();
        for (Department dept : departments) {
            departmentNames.add(dept.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapter);

        if (!departments.isEmpty()) {
            selectedDepartmentId = departments.get(0).id;
            updatePositionSpinner(departments.get(0).positions);
        } else {
            spinnerPosition.setEnabled(false);
        }
    }

    private void updatePositionSpinner(String[] positions) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, positions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPosition.setAdapter(adapter);
        spinnerPosition.setEnabled(positions.length > 0);
    }

    public byte[] convertImageToByteArray() {
        try {
            Bitmap bitmap = null;
            if (imageView.getDrawable() instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            }
            if (bitmap == null) {
                throw new IllegalStateException("Drawable is not a BitmapDrawable or is null");
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        } catch (Exception e) {
            Log.e("AddEmployeeActivity", "Error converting image to byte array: " + e.getMessage());
            return null;
        }
    }

    public void openCamera(View view) {
        Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, 0);
    }

    public void openGallery(View view) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        Bitmap bmp = (Bitmap) bundle.get("data");
                        if (bmp != null) {
                            Bitmap resized = Bitmap.createScaledBitmap(bmp, 100, 100, true);
                            imageView.setImageBitmap(resized);
                        }
                    }
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        imageView.setImageURI(selectedImage);
                    }
                }
                break;
        }
    }

    private void addEmployee() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String residence = editTextResidence.getText().toString().trim();
        String position = spinnerPosition.getSelectedItem() != null ? spinnerPosition.getSelectedItem().toString() : "";
        byte[] imageBytes = convertImageToByteArray();

        if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || residence.isEmpty() || selectedDepartmentId == -1 || position.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageBytes == null) {
            Toast.makeText(this, "Failed to process profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        Employee employee = new Employee(firstName, lastName, phoneNumber, email, selectedDepartmentId, position, residence);
        long result = employeeDAO.insertEmployee(employee, imageBytes);

        if (result != -1) {
            Toast.makeText(this, "Employee added successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add employee", Toast.LENGTH_SHORT).show();
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