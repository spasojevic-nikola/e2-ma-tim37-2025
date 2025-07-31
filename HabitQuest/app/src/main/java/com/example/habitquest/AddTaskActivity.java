package com.example.habitquest;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etTaskDescription, etRepeatInterval;
    private Spinner spinnerCategory, spinnerDifficulty, spinnerImportance, spinnerRepeatUnit;
    private RadioGroup radioGroupFrequency;
    private LinearLayout layoutRepeatDetails, layoutRepeatDates;
    private Button btnStartDate, btnEndDate, btnExecutionTime, btnCreateTask;

    private View viewCategoryColor;
    private List<TaskCategory> categoryList;

    private String startDate = "", endDate = "", executionTime = "";
    private DatabaseHelper dbHelper;

    private void loadCategoriesToSpinner() {
        categoryList = dbHelper.getAllCategoriesFromDb(); // ažuriraš polje klase!
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                getCategoryNames(categoryList));
        spinnerCategory.setAdapter(categoryAdapter);

        // Listener za prikaz boje kategorije
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String color = categoryList.get(position).getColor();
                viewCategoryColor.setBackgroundColor(android.graphics.Color.parseColor(color));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Inicijalizacija
        etTaskName = findViewById(R.id.etTaskName);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerImportance = findViewById(R.id.spinnerImportance);
        spinnerRepeatUnit = findViewById(R.id.spinnerRepeatUnit);
        etRepeatInterval = findViewById(R.id.etRepeatInterval);
        radioGroupFrequency = findViewById(R.id.radioGroupFrequency);
        layoutRepeatDetails = findViewById(R.id.layoutRepeatDetails);
        layoutRepeatDates = findViewById(R.id.layoutRepeatDates);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnExecutionTime = findViewById(R.id.btnExecutionTime);
        btnCreateTask = findViewById(R.id.btnCreateTask);
        viewCategoryColor = findViewById(R.id.viewCategoryColor);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        dbHelper = new DatabaseHelper(this);

        // 1. Popuni kategorije iz baze
//        List<TaskCategory> categoryList = dbHelper.getAllCategoriesFromDb();
//        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
//                this, android.R.layout.simple_spinner_dropdown_item,
//                getCategoryNames(categoryList));
//        spinnerCategory.setAdapter(categoryAdapter);

        loadCategoriesToSpinner();


        // 2. Popuni težinu i bitnost iz predefinisanih listi
        spinnerDifficulty.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Veoma lak", "Lak", "Težak", "Ekstremno težak"}));
        spinnerImportance.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Normalan", "Važan", "Ekstremno važan", "Specijalan"}));
        spinnerRepeatUnit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"dan", "nedelja"}));

        // 3. RadioGroup za učestalost
        radioGroupFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOnetime) {
                layoutRepeatDetails.setVisibility(View.GONE);
                layoutRepeatDates.setVisibility(View.GONE);
            } else {
                layoutRepeatDetails.setVisibility(View.VISIBLE);
                layoutRepeatDates.setVisibility(View.VISIBLE);
            }
        });

        // 4. Date pickeri
        btnStartDate.setOnClickListener(v -> pickDate(true));
        btnEndDate.setOnClickListener(v -> pickDate(false));
        btnExecutionTime.setOnClickListener(v -> pickTime());

        // 5. Kreiranje zadatka
        btnCreateTask.setOnClickListener(v -> {
            String name = etTaskName.getText().toString().trim();
            String description = etTaskDescription.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            String difficulty = spinnerDifficulty.getSelectedItem().toString();
            String importance = spinnerImportance.getSelectedItem().toString();
            boolean isRepeating = radioGroupFrequency.getCheckedRadioButtonId() == R.id.rbRepeating;

            int repeatInterval = 0;
            String repeatUnit = "";
            if (isRepeating) {
                String strInterval = etRepeatInterval.getText().toString();
                if (!strInterval.isEmpty()) {
                    repeatInterval = Integer.parseInt(strInterval);
                }
                repeatUnit = spinnerRepeatUnit.getSelectedItem().toString();
            }

            if (name.isEmpty() || executionTime.isEmpty()) {
                Toast.makeText(this, "Popuni obavezna polja!", Toast.LENGTH_SHORT).show();
                return;
            }

            int xpValue = getXPForDifficulty(difficulty) + getXPForImportance(importance);

            int pos = spinnerCategory.getSelectedItemPosition();
            String categoryColor = categoryList.get(pos).getColor();

            Task task = new Task();
            task.setName(name);
            task.setDescription(description);
            task.setCategory(category);
            task.setDifficulty(difficulty);
            task.setImportance(importance);
            task.setRepeating(isRepeating);
            task.setRepeatInterval(repeatInterval);
            task.setRepeatUnit(repeatUnit);
            task.setStartDate(startDate);
            task.setEndDate(endDate);
            task.setExecutionTime(executionTime);
            task.setXpValue(xpValue);
            task.setStatus("aktivan");
            task.setCategoryColor(categoryColor);


            boolean success = dbHelper.insertTask(task);

            if (success) {
                Toast.makeText(this, "Zadatak sačuvan!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Greška pri čuvanju zadatka!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String[] getCategoryNames(List<TaskCategory> list) {
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
            arr[i] = list.get(i).getName();
        return arr;
    }

    private void pickDate(boolean isStart) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dateStr = dayOfMonth + "." + (month + 1) + "." + year;
            if (isStart) {
                startDate = dateStr;
                btnStartDate.setText(dateStr);
            } else {
                endDate = dateStr;
                btnEndDate.setText(dateStr);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void pickTime() {
        final Calendar c = Calendar.getInstance();
        TimePickerDialog tpd = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            executionTime = String.format("%02d:%02d", hourOfDay, minute);
            btnExecutionTime.setText(executionTime);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        tpd.show();
    }

    private int getXPForDifficulty(String diff) {
        switch (diff) {
            case "Veoma lak": return 1;
            case "Lak": return 3;
            case "Težak": return 7;
            case "Ekstremno težak": return 20;
            default: return 0;
        }
    }

    private int getXPForImportance(String imp) {
        switch (imp) {
            case "Normalan": return 1;
            case "Važan": return 3;
            case "Ekstremno važan": return 10;
            case "Specijalan": return 100;
            default: return 0;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategoriesToSpinner();
    }



}
