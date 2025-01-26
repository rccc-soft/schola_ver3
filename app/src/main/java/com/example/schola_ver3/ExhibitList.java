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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.HashMap;

public class ExhibitList extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ExhibitList";
    private ProductDatabaseHelper dbHelper;

    private ImageView exlist_homebtn;
    private ImageView exlist_searchbtn;
    private ImageView exlist_exhibitbtn;
    private ImageView exlist_favobtn;
    private ImageView exlist_mypagebtn;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exhibit_list);

        exlist_homebtn = findViewById(R.id.exlist_homebtn);
        exlist_homebtn.setOnClickListener(this);

        exlist_searchbtn = findViewById(R.id.exlist_searchbtn);
        exlist_searchbtn.setOnClickListener(this);

        exlist_exhibitbtn = findViewById(R.id.exlist_exhibitbtn);
        exlist_exhibitbtn.setOnClickListener(this);

        exlist_favobtn = findViewById(R.id.exlist_favobtn);
        exlist_favobtn.setOnClickListener(this);

        exlist_mypagebtn = findViewById(R.id.exlist_mypagebtn);
        exlist_mypagebtn.setOnClickListener(this);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);


        dbHelper = new ProductDatabaseHelper(this);

        displayUserProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayUserProducts();
    }

    private void displayUserProducts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String userId = getCurrentUserId();


        // 出品者IDが一致する商品の取得
        Cursor cursor = db.rawQuery("SELECT * FROM 商品テーブル WHERE 出品者ID = ? ORDER BY 出品日時 ASC", new String[]{userId});

        ArrayList<HashMap<String, Object>> results = new ArrayList<>();

        while (cursor.moveToNext()) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("商品ID", cursor.getString(cursor.getColumnIndex("商品ID")));
            item.put("商品名", cursor.getString(cursor.getColumnIndex("商品名")));
            item.put("金額", cursor.getString(cursor.getColumnIndex("金額")));
            item.put("商品説明", cursor.getString(cursor.getColumnIndex("商品説明")));
            item.put("カテゴリ", cursor.getString(cursor.getColumnIndex("カテゴリ")));
            item.put("配送方法", cursor.getString(cursor.getColumnIndex("配送方法")));
            item.put("地域", cursor.getString(cursor.getColumnIndex("地域")));
            item.put("商品画像", cursor.getBlob(cursor.getColumnIndex("商品画像")));
            results.add(item);
            Log.d(TAG, "Product loaded: " + item);
        }

        cursor.close();

        if (results.isEmpty()) {
            Toast.makeText(this, "表示する商品がありません", Toast.LENGTH_SHORT).show();
        } else {
            displayProductsInGrid(results);
        }
    }

    private void displayProductsInGrid(ArrayList<HashMap<String, Object>> products) {
        LinearLayout layoutContainer = findViewById(R.id.linearLayout2);

        // 既存の行をクリア
        layoutContainer.removeAllViews();

        LinearLayout currentRow = null;

        for (int i = 0; i < products.size(); i++) {
            if (i % 2 == 0) {
                // 新しい行を作成
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                layoutContainer.addView(currentRow);
            }

            HashMap<String, Object> product = products.get(i);
            View productView = getLayoutInflater().inflate(R.layout.exhibit_list_item, null);

            ImageView imageView = productView.findViewById(R.id.productImageView);
            TextView nameTextView = productView.findViewById(R.id.productNameTextView);
            TextView priceTextView = productView.findViewById(R.id.productPriceTextView);

            nameTextView.setText((String) product.get("商品名"));
            priceTextView.setText("￥" + (String) product.get("金額"));

            byte[] imageData = (byte[]) product.get("商品画像");
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            imageView.setImageBitmap(bitmap);

            // 商品がクリックされたときの処理
            String productId = (String) product.get("商品ID");
            productView.setOnClickListener(v -> navigateToExhibitDetails(product));

            // 現在の行に商品ビューを追加
            currentRow.addView(productView);
        }
    }

    private void navigateToExhibitDetails(HashMap<String, Object> item) {
        Intent editIntent = new Intent(ExhibitList.this, ExhibitDetails.class);

        editIntent.putExtra("商品ID", (String) item.get("商品ID"));
        editIntent.putExtra("商品名", (String) item.get("商品名"));
        editIntent.putExtra("商品説明", (String) item.get("商品説明"));
        editIntent.putExtra("金額", (String) item.get("金額"));
        editIntent.putExtra("カテゴリ", (String) item.get("カテゴリ"));
        editIntent.putExtra("配送方法", (String) item.get("配送方法"));
        editIntent.putExtra("地域", (String) item.get("地域"));

        editIntent.putExtra("商品画像", (byte[]) item.get("商品画像"));

        Log.d(TAG, "Navigating to ExhibitDetail with Product ID: " + item.get("商品ID"));

        startActivity(editIntent);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null; // intentを初期化
        if (v.getId() == R.id.exlist_homebtn) {
            intent = new Intent(getApplication(), HomePage.class);
        } else if (v.getId() == R.id.exlist_searchbtn) {
            intent = new Intent(getApplication(), ProductSearch.class);
        } else if (v.getId() == R.id.exlist_exhibitbtn) {
            intent = new Intent(getApplication(), Exhibit.class);
        } else if (v.getId() == R.id.exlist_favobtn) {
            // intentは未設定なので、必要なクラスを設定してください
//          intent = new Intent(getApplication(), Favorite.class); // 例：お気に入り画面への遷移
        } else if (v.getId() == R.id.exlist_mypagebtn) {
            intent = new Intent(getApplication(), MyPage.class);
        } else if (v.getId() == R.id.backButton) {
            intent = new Intent(getApplication(), MyPage.class);
        }

        // intentがnullでない場合にstartActivityを呼び出す
        if (intent != null) {
            startActivity(intent);
        }
    }

    private String getCurrentUserId() {
        // 現在のユーザーIDを取得するロジックを実装
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");
//        String userId = "user_id";
        return userId;
    }
}