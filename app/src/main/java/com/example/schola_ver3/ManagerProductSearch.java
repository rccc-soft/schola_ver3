package com.example.schola_ver3;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ManagerProductSearch extends AppCompatActivity {
    private EditText searchEditText;
    private ImageButton backButton;
    private ListView resultsListView;
    private ProductDatabaseHelper productDbHelper;
    private ArrayAdapter<String> adapter;
    private List<String> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_search_manager);

        // UIの初期化
        searchEditText = findViewById(R.id.editTextText);
        backButton = findViewById(R.id.backButton);
        resultsListView = findViewById(R.id.resultsListView);

        productDbHelper = new ProductDatabaseHelper(this);
        productList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productList);
        resultsListView.setAdapter(adapter);

        // 戻るボタンの処理（ManagerHomepageへ遷移）
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerProductSearch.this, ManagerHomepage.class);
            startActivity(intent);
            finish();
        });

        // 検索入力時にリアルタイムで検索
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // リストビューのアイテムクリックリスナー（選択すると遷移）
        resultsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedProduct = productList.get(position);
            Intent intent = new Intent(this, ManagerExhibitCancel.class);
            intent.putExtra("SELECTED_PRODUCT", selectedProduct);
            startActivity(intent);
        });
    }

    private void performSearch(String query) {
        productList.clear();

        if (!query.isEmpty()) {
            Cursor cursor = productDbHelper.searchProducts(query);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String productName = cursor.getString(cursor.getColumnIndex("product_name"));
                    productList.add(productName);
                }
                cursor.close();
            }
        }

        adapter.notifyDataSetChanged();
    }
}
