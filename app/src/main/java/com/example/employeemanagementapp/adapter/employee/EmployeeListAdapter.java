package com.example.employeemanagementapp.adapter.employee;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.db.dao.DepartmentDAO;
import com.example.employeemanagementapp.db.model.Employee;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;

public class EmployeeListAdapter extends ArrayAdapter<Employee> {

    private DepartmentDAO departmentDAO;

    public EmployeeListAdapter(Context context, ArrayList<Employee> employees) {
        super(context, 0, employees);
        departmentDAO = new DepartmentDAO(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Employee employee = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
        }

        TextView firstLastNameTextView = convertView.findViewById(R.id.text_name);
        TextView jobTitleTextView = convertView.findViewById(R.id.text_job);

        firstLastNameTextView.setText(employee.getFirstName() + " " + employee.getLastName());

        String departmentName = "Unknown";
        Cursor deptCursor = departmentDAO.getDepartmentById(employee.getDepartmentId());
        if (deptCursor != null && deptCursor.moveToFirst()) {
            @SuppressLint("Range") String name = deptCursor.getString(deptCursor.getColumnIndex(Constants.COLUMN_DEPT_NAME));
            departmentName = name != null ? name : "Unknown";
            deptCursor.close();
        }

        jobTitleTextView.setText(departmentName + " - " + employee.getPosition());

        return convertView;
    }
}