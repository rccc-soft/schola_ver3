package com.example.schola_ver3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchResult extends AppCompatActivity implements View.OnClickListener {
    private ListView resultsListView;
    private ProductDatabaseHelper dbHelper;
    private DatabaseHelper databaseHelper;
    private EditText editTextText;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_result);

        editTextText = findViewById(R.id.editTextText);
        editTextText.setOnClickListener(this);

        dbHelper = new ProductDatabaseHelper(this);
        databaseHelper = new DatabaseHelper(this);
        resultsListView = findViewById(R.id.resultsListView);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        Intent intent = getIntent();
        String query = intent.getStringExtra("SEARCH_QUERY");

        displaySearchResults(query);

        resultsListView.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, Object> selectedItem = (HashMap<String, Object>) resultsListView.getItemAtPosition(position);
            navigateToProductDetail(selectedItem);
        });
    }

    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("user_id", "");
    }

    private void displaySearchResults(String query) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                "商品ID", "商品名", "商品説明", "商品画像", "カテゴリ", "金額", "配送方法", "出品日時", "地域", "出品者ID"
        };

        String currentUserId = getCurrentUserId();
        Log.d("SearchResult", "currentUserId: " + currentUserId);
        String selection;
        String[] selectionArgs;

        if (getIntent().hasExtra("SEARCH_TYPE")) {
            String searchType = getIntent().getStringExtra("SEARCH_TYPE");
            if (searchType.equals("SCHOOL")) {
                String userSchool = getIntent().getStringExtra("USER_SCHOOL");
                // DatabaseHelperからユーザーIDのリストを取得
                List<String> userIds = databaseHelper.getUserIdsBySchool(userSchool);
                // ProductDatabaseHelperで商品を検索
                selection = "商品名 LIKE ? AND 出品者ID != ? AND 出品者ID IN (" + makePlaceholders(userIds.size()) + ")";
                selectionArgs = new String[userIds.size() + 2];
                selectionArgs[0] = "%" + query + "%";
                selectionArgs[1] = currentUserId;
                for (int i = 0; i < userIds.size(); i++) {
                    selectionArgs[i + 2] = userIds.get(i);
                }
            } else if (searchType.equals("CATEGORY")) {
                selection = "カテゴリ = ? AND 出品者ID != ?";
                selectionArgs = new String[]{query, currentUserId};
            } else {
                selection = "商品名 LIKE ? AND 出品者ID != ?";
                selectionArgs = new String[]{"%" + query + "%", currentUserId};
            }
        } else {
            selection = "商品名 LIKE ? AND 出品者ID != ?";
            selectionArgs = new String[]{"%" + query + "%", currentUserId};
        }

        Cursor cursor = db.query(
                ProductDatabaseHelper.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "10" // ここで最大10件を指定
        );

        ArrayList<HashMap<String, Object>> results = new ArrayList<>();
        while (cursor.moveToNext()) {
            String productId = cursor.getString(cursor.getColumnIndex("商品ID"));
            if (!dbHelper.isProductSold(productId)) {
                HashMap<String, Object> item = new HashMap<>();
                for (String column : projection) {
                    if (column.equals("商品画像")) {
                        item.put(column, cursor.getBlob(cursor.getColumnIndex(column)));
                    } else if (column.equals("金額")) {
                        String price = cursor.getString(cursor.getColumnIndex(column));
                        item.put(column, price); // 元の価格を保存
                        item.put("表示用金額", "￥" + price); // 表示用に￥を追加
                    } else {
                        item.put(column, cursor.getString(cursor.getColumnIndex(column)));
                    }
                }
                results.add(item);
            }
        }
        cursor.close();

        if (results.isEmpty()) {
            Toast.makeText(this, "該当する商品が見つかりませんでした", Toast.LENGTH_SHORT).show();
        } else {
            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    results,
                    R.layout.search_result_item,
                    new String[]{"商品名", "表示用金額", "商品画像"},
                    new int[]{R.id.productNameTextView, R.id.productPriceTextView, R.id.productImageView}
            );

            adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view instanceof ImageView && data instanceof byte[]) {
                        ImageView imageView = (ImageView) view;
                        byte[] imageData = (byte[]) data;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                        imageView.setImageBitmap(bitmap);
                        return true;
                    }
                    return false;
                }
            });

            resultsListView.setAdapter(adapter);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.editTextText) {
            intent = new Intent(getApplication(), ProductSearch.class);
        } else if(v.getId() == R.id.backButton) {
            finish();
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void navigateToProductDetail(HashMap<String, Object> item) {
        Intent detailIntent = new Intent(SearchResult.this, ProductDetail.class);
        for (String key : item.keySet()) {
            if (key.equals("表示用金額")) {
                continue; // 表示用金額はスキップ
            }
            if (item.get(key) instanceof String) {
                detailIntent.putExtra(key, (String) item.get(key));
            } else if (item.get(key) instanceof byte[]) {
                detailIntent.putExtra(key, (byte[]) item.get(key));
            }
        }
        startActivity(detailIntent);
    }

    private String makePlaceholders(int len) {
        if (len < 1) {
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }
}