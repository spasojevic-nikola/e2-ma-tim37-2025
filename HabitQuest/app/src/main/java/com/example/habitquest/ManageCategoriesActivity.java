package com.example.habitquest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import yuku.ambilwarna.AmbilWarnaDialog;

public class ManageCategoriesActivity extends AppCompatActivity {

    private ListView listViewCategories;
    private EditText etCategoryName;
    private Button btnPickColor, btnAddCategory;
    private String pickedColor = "#43A047"; // podrazumevana boja
    private DatabaseHelper dbHelper;
    private List<TaskCategory> categoryList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        listViewCategories = findViewById(R.id.listViewCategories);
        etCategoryName = findViewById(R.id.etCategoryName);
        btnPickColor = findViewById(R.id.btnPickColor);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        dbHelper = new DatabaseHelper(this);

        refreshList();

        // Dodaj kategoriju
        btnAddCategory.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            if (name.isEmpty() || pickedColor.isEmpty()) {
                Toast.makeText(this, "Unesi naziv i izaberi boju!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Proveri da li već postoji kategorija sa tom bojom
            if (dbHelper.categoryColorExists(pickedColor)) {
                Toast.makeText(this, "Boja je već zauzeta!", Toast.LENGTH_SHORT).show();
                return;
            }
            dbHelper.insertCategory(name, pickedColor);
            refreshList();
            etCategoryName.setText("");
            pickedColor = "#43A047";
            btnPickColor.setBackgroundColor(Color.parseColor(pickedColor));
        });

// Izbor boje (demo, možeš koristiti biblioteku ili picker)
        btnPickColor.setOnClickListener(v -> {
            int initialColor = Color.parseColor(pickedColor); // ili neka podrazumevana boja
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    pickedColor = String.format("#%06X", (0xFFFFFF & color));
                    btnPickColor.setBackgroundColor(color);
                }
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // ništa, možeš dodati toast
                }
            });
            colorPicker.show();
        });

        
        // Izmena boje na klik na item iz liste
        listViewCategories.setOnItemClickListener((parent, view, position, id) -> {
            TaskCategory cat = categoryList.get(position);
            int initialColor = Color.parseColor(cat.getColor());

            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    String hexColor = String.format("#%06X", (0xFFFFFF & color));
                    boolean ok = dbHelper.updateCategoryColor(cat.getId(), hexColor);
                    if (ok) {
                        Toast.makeText(ManageCategoriesActivity.this, "Boja promenjena!", Toast.LENGTH_SHORT).show();
                        refreshList();
                    } else {
                        Toast.makeText(ManageCategoriesActivity.this, "Boja je već zauzeta!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {}
            });
            colorPicker.show();
        });


    }

    private void refreshList() {
        categoryList = dbHelper.getAllCategoriesFromDb();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                getNamesWithColor(categoryList));
        listViewCategories.setAdapter(adapter);
    }

    private String[] getNamesWithColor(List<TaskCategory> cats) {
        String[] arr = new String[cats.size()];
        for (int i = 0; i < cats.size(); i++) {
            arr[i] = cats.get(i).getName() + " (" + cats.get(i).getColor() + ")";
        }
        return arr;
    }
}
