package com.example.schola_ver3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProductSearch extends AppCompatActivity implements View.OnClickListener {
    private Button searchbtn;
    private Button schoolSearchBtn;
    private Button categorySearchBtn;
    private ImageButton backButton;
    private EditText searchEditText;
    private DatabaseHelper dbHelper;
    private ProductDatabaseHelper productDbHelper;
    private String userSchool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_search);


        initializeViews();
        setupClickListeners();
        dbHelper = new DatabaseHelper(this);
        productDbHelper = new ProductDatabaseHelper(this);
        getUserSchool();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        searchEditText = findViewById(R.id.searchEditText);
        searchbtn = findViewById(R.id.searchbtn);
        schoolSearchBtn = findViewById(R.id.schoolSearchBtn);
        categorySearchBtn = findViewById(R.id.categorySearchBtn);
    }

    private void setupClickListeners() {
        if (searchbtn != null) searchbtn.setOnClickListener(this);
        if (backButton != null) backButton.setOnClickListener(this);
        if (schoolSearchBtn != null) schoolSearchBtn.setOnClickListener(this);
        if (categorySearchBtn != null) categorySearchBtn.setOnClickListener(this);
    }

    private void getUserSchool() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", "");
        Cursor cursor = dbHelper.getMemberInfo(userId);
        if (cursor != null && cursor.moveToFirst()) {
            int schoolIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHOOL);
            if (schoolIndex != -1) {
                userSchool = cursor.getString(schoolIndex);
            }
            cursor.close();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.searchbtn) {
            performSearch(false);
        } else if (v.getId() == R.id.backButton) {
            finish();
        } else if (v.getId() == R.id.schoolSearchBtn) {
            performSearch(true);
        } else if (v.getId() == R.id.categorySearchBtn) {
            showCategoryDialog();
        }
    }

    private void performSearch(boolean schoolSearch) {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "検索キーワードを入力してください", Toast.LENGTH_SHORT).show();
            return;
        }

        if (query.length() > 30) {
            Toast.makeText(this, "検索キーワードは30文字以内で入力してください", Toast.LENGTH_SHORT).show();
            return;
        }

        if (containsSpecialChars(query)) {
            Toast.makeText(this, "検索キーワードに特殊文字は使用できません", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, SearchResult.class);
        intent.putExtra("SEARCH_QUERY", query);
        if (schoolSearch) {
            intent.putExtra("SEARCH_TYPE", "SCHOOL");
            intent.putExtra("USER_SCHOOL", userSchool);
        }
        startActivity(intent);
    }

    private void showCategoryDialog() {
        String[] category_array = getResources().getStringArray(R.array.category_array);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("カテゴリを選択");
        builder.setItems(category_array, (dialog, which) -> {
            String selectedCategory = category_array[which];
            performCategorySearch(selectedCategory);
        });
        builder.show();
    }

    private void performCategorySearch(String category) {
        Intent intent = new Intent(this, SearchResult.class);
        intent.putExtra("SEARCH_TYPE", "CATEGORY");
        intent.putExtra("SEARCH_QUERY", category);
        startActivity(intent);
    }

    private boolean containsSpecialChars(String input) {
        String specialChars = "\"'\\;:[]{}";
        for (char c : specialChars.toCharArray()) {
            if (input.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }
}