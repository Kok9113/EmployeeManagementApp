package com.example.employeemanagementapp.ui.employee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.MainActivity;
import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.db.dao.EmployeeDAO;
import com.example.employeemanagementapp.db.model.Employee;
import com.example.employeemanagementapp.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class EmployeeDetails extends AppCompatActivity {

    private EmployeeDAO employeeDAO;
    private DatabaseHelper dbHelper;
    private boolean isEditMode = false;
    private ImageView profileImageView;
    private EditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextEmail, editTextResidence;
    private Spinner spinnerDepartment, spinnerPosition;
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
        setContentView(R.layout.activity_employee_details);

        employeeDAO = new EmployeeDAO(this);
        dbHelper = new DatabaseHelper(this);

        editTextFirstName = findViewById(R.id.edittext_first_name);
        editTextLastName = findViewById(R.id.edittext_last_name);
        editTextPhoneNumber = findViewById(R.id.edittext_phone_number);
        editTextEmail = findViewById(R.id.edittext_email);
        editTextResidence = findViewById(R.id.edittext_residence);
        spinnerDepartment = findViewById(R.id.spinner_department);
        spinnerPosition = findViewById(R.id.spinner_position);
        profileImageView = findViewById(R.id.image_profile2);

        long employeeId = getIntent().getLongExtra("employeeId", -1);
        loadDepartments();

        if (employeeId != -1) {
            Cursor cursor = employeeDAO.getEmployeeById(employeeId);
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String firstName = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_FIRST_NAME));
                @SuppressLint("Range") String lastName = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAST_NAME));
                @SuppressLint("Range") String phoneNumber = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PHONE_NUMBER));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_EMAIL));
                @SuppressLint("Range") String residence = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_RESIDENCE));
                @SuppressLint("Range") long departmentId = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DEPARTMENT_ID));
                @SuppressLint("Range") String position = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_POSITION));

                editTextFirstName.setText(firstName);
                editTextLastName.setText(lastName);
                editTextPhoneNumber.setText(phoneNumber);
                editTextEmail.setText(email);
                editTextResidence.setText(residence);

                setEditTextReadonly(editTextFirstName);
                setEditTextReadonly(editTextLastName);
                setEditTextReadonly(editTextPhoneNumber);
                setEditTextReadonly(editTextEmail);
                setEditTextReadonly(editTextResidence);
                spinnerDepartment.setEnabled(false);
                spinnerPosition.setEnabled(false);

                // Set department spinner
                for (int i = 0; i < departments.size(); i++) {
                    if (departments.get(i).id == departmentId) {
                        spinnerDepartment.setSelection(i);
                        selectedDepartmentId = departmentId;
                        updatePositionSpinner(departments.get(i).positions);
                        // Set position spinner
                        for (int j = 0; j < departments.get(i).positions.length; j++) {
                            if (departments.get(i).positions[j].equals(position)) {
                                spinnerPosition.setSelection(j);
                                break;
                            }
                        }
                        break;
                    }
                }

                profileImageView.setImageResource(R.drawable.ic_launcher_background);
                byte[] imageData = employeeDAO.getEmployeeProfileImage(employeeId);
                if (imageData != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    profileImageView.setImageBitmap(bitmap);
                } else {
                    profileImageView.setImageResource(R.drawable.rounded_button_background);
                }

                cursor.close();
            } else {
                Log.d("Employee Details", "No employee found with ID: " + employeeId);
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            Log.d("Employee Details", "Invalid employee ID");
        }

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
        Cursor cursor = dbHelper.getAllDepartments();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_DEPT_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DEPT_NAME));
                @SuppressLint("Range") String positions = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DEPT_POSITIONS));
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
        spinnerPosition.setEnabled(isEditMode && positions.length > 0);
    }

    private void setEditTextReadonly(EditText editText) {
        editText.setFocusable(false);
        editText.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    public void toggleEditMode(View view) {
        isEditMode = !isEditMode;

        if (isEditMode) {
            setEditTextEditable(editTextFirstName);
            setEditTextEditable(editTextLastName);
            setEditTextEditable(editTextPhoneNumber);
            setEditTextEditable(editTextEmail);
            setEditTextEditable(editTextResidence);
            spinnerDepartment.setEnabled(true);
            spinnerPosition.setEnabled(spinnerPosition.getAdapter().getCount() > 0);
        } else {
            setEditTextReadonly(editTextFirstName);
            setEditTextReadonly(editTextLastName);
            setEditTextReadonly(editTextPhoneNumber);
            setEditTextReadonly(editTextEmail);
            setEditTextReadonly(editTextResidence);
            spinnerDepartment.setEnabled(false);
            spinnerPosition.setEnabled(false);
        }
    }

    private void setEditTextEditable(EditText editText) {
        editText.setFocusableInTouchMode(true);
        editText.setTextColor(getResources().getColor(android.R.color.black));
    }

    public byte[] convertImageToByteArray() {
        try {
            Bitmap bitmap = null;
            if (profileImageView.getDrawable() instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();
            }
            if (bitmap == null) {
                throw new IllegalStateException("Drawable is not a BitmapDrawable or is null");
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        } catch (Exception e) {
            Log.e("EmployeeDetails", "Error converting image to byte array: " + e.getMessage());
            return null;
        }
    }

    public void openCamera2(View view) {
        Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, 0);
    }

    public void openGallery2(View view) {
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
                            profileImageView.setImageBitmap(resized);
                        }
                    }
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        profileImageView.setImageURI(selectedImage);
                    }
                }
                break;
        }
    }

    public void goBack(View view) {
        finish();
    }

    public void SendMail(View view) {
        String recipientEmail = editTextEmail.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + Uri.encode(recipientEmail)));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "No email app found. Please install an email app.", Toast.LENGTH_SHORT).show();
        }
    }

    public void MakeCall(View view) {
        String recipientPhone = editTextPhoneNumber.getText().toString();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + recipientPhone));
        startActivity(intent);
    }

    public void MakeSMS(View view) {
        String recipientPhone = editTextPhoneNumber.getText().toString();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + recipientPhone));
        startActivity(intent);
    }

    public void deleteEmployee(View view) {
        long employeeId = getIntent().getLongExtra("employeeId", -1);
        if (employeeId != -1) {
            int rowsDeleted = employeeDAO.deleteEmployee(employeeId);
            if (rowsDeleted > 0) {
                Toast.makeText(this, "Employee deleted successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Failed to delete employee", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("Delete Employee", "Invalid employee ID");
        }
    }

    public void updateEmployee(View view) {
        long employeeId = getIntent().getLongExtra("employeeId", -1);
        if (employeeId != -1) {
            String firstName = editTextFirstName.getText().toString();
            String lastName = editTextLastName.getText().toString();
            String phoneNumber = editTextPhoneNumber.getText().toString();
            String email = editTextEmail.getText().toString();
            String residence = editTextResidence.getText().toString();
            String position = spinnerPosition.getSelectedItem() != null ? spinnerPosition.getSelectedItem().toString() : "";
            byte[] imageBytes = convertImageToByteArray();

            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                    TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(residence) || selectedDepartmentId == -1 || TextUtils.isEmpty(position)) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            if (imageBytes == null) {
                Toast.makeText(this, "Failed to process profile image", Toast.LENGTH_SHORT).show();
                return;
            }
            Employee employee = new Employee(firstName, lastName, phoneNumber, email, selectedDepartmentId, position, residence);
            int rowsAffected = employeeDAO.updateEmployee(employeeId, employee, imageBytes);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Employee updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update employee", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("Update Employee", "Invalid employee ID");
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