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

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class PurchasedList extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PurchasedList";
    private DatabaseHelper dbHelper;
    private ProductDatabaseHelper productDbHelper;

    private ImageView purchased_homebtn, purchased_searchbtn, purchased_exhibitbtn, purchased_favobtn, purchased_mypagebtn;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchased_list);

        initializeViews();
        setClickListeners();

        dbHelper = new DatabaseHelper(this);
        productDbHelper = new ProductDatabaseHelper(this);

        displayPurchasedProducts();
    }

    private void initializeViews() {
        purchased_homebtn = findViewById(R.id.purchased_homebtn);
        purchased_searchbtn = findViewById(R.id.purchased_searchbtn);
        purchased_exhibitbtn = findViewById(R.id.purchased_exhibitbtn);
        purchased_favobtn = findViewById(R.id.purchased_favobtn);
        purchased_mypagebtn = findViewById(R.id.purchased_mypagebtn);
        backButton = findViewById(R.id.backButton);
    }

    private void setClickListeners() {
        purchased_homebtn.setOnClickListener(this);
        purchased_searchbtn.setOnClickListener(this);
        purchased_exhibitbtn.setOnClickListener(this);
        purchased_favobtn.setOnClickListener(this);
        purchased_mypagebtn.setOnClickListener(this);
        backButton.setOnClickListener(this);
    }

    private void displayPurchasedProducts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String userId = getCurrentUserId();

        // 購入テーブルから商品IDを取得
        Cursor purchaseCursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_ITEM_ID +
                " FROM " + DatabaseHelper.TABLE_NAME +
                " WHERE " + DatabaseHelper.COLUMN_BUYER_ID + " = ?", new String[]{userId});

        ArrayList<String> purchasedProductIds = new ArrayList<>();
        while (purchaseCursor.moveToNext()) {
            purchasedProductIds.add(purchaseCursor.getString(purchaseCursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_ID)));
        }
        purchaseCursor.close();

        if (purchasedProductIds.isEmpty()) {
            Toast.makeText(this, "購入した商品がありません", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<HashMap<String, Object>> results = new ArrayList<>();

        for (String productId : purchasedProductIds) {
            Cursor productCursor = productDbHelper.getProductInfo(productId);
            if (productCursor != null && productCursor.moveToFirst()) {
                HashMap<String, Object> item = new HashMap<>();
                item.put("商品ID", productId);
                item.put("商品名", productCursor.getString(productCursor.getColumnIndex("商品名")));
                item.put("金額", productCursor.getString(productCursor.getColumnIndex("金額")));
                item.put("商品説明", productCursor.getString(productCursor.getColumnIndex("商品説明")));
                item.put("カテゴリ", productCursor.getString(productCursor.getColumnIndex("カテゴリ")));
                item.put("配送方法", productCursor.getString(productCursor.getColumnIndex("配送方法")));
                item.put("地域", productCursor.getString(productCursor.getColumnIndex("地域")));
                item.put("商品画像", productCursor.getBlob(productCursor.getColumnIndex("商品画像")));
                results.add(item);
                productCursor.close();
            }
        }

        if (results.isEmpty()) {
            Toast.makeText(this, "購入した商品情報を取得できませんでした", Toast.LENGTH_SHORT).show();
        } else {
            displayProductsInGrid(results);
        }
    }

    private void displayProductsInGrid(ArrayList<HashMap<String, Object>> products) {
        LinearLayout layoutContainer = findViewById(R.id.linearLayout2);
        layoutContainer.removeAllViews();

        LinearLayout currentRow = null;

        for (int i = 0; i < products.size(); i++) {
            if (i % 2 == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                layoutContainer.addView(currentRow);
            }

            HashMap<String, Object> product = products.get(i);
            View productView = getLayoutInflater().inflate(R.layout.purchased_list_item, null);

            ImageView imageView = productView.findViewById(R.id.productImageView);
            TextView nameTextView = productView.findViewById(R.id.productNameTextView);
            TextView priceTextView = productView.findViewById(R.id.productPriceTextView);

            nameTextView.setText((String) product.get("商品名"));
            priceTextView.setText("￥" + (String) product.get("金額"));

            byte[] imageData = (byte[]) product.get("商品画像");
            if (imageData != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                imageView.setImageBitmap(bitmap);
            }

            productView.setOnClickListener(v -> navigateToProductDetail(product));

            currentRow.addView(productView);
        }
    }

    private void navigateToProductDetail(HashMap<String, Object> product) {
        Intent intent = new Intent(this, ProductDetail.class);
        String productId = (String) product.get("商品ID");

        SQLiteDatabase db = productDbHelper.getReadableDatabase();
        String[] projection = {
                "商品ID", "商品名", "商品説明", "商品画像", "カテゴリ", "金額", "配送方法", "出品日時", "地域", "出品者ID"
        };
        String selection = "商品ID = ?";
        String[] selectionArgs = {productId};

        Cursor cursor = db.query(
                "商品テーブル",
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            for (String column : projection) {
                if (column.equals("商品画像")) {
                    intent.putExtra(column, cursor.getBlob(cursor.getColumnIndex(column)));
                } else {
                    intent.putExtra(column, cursor.getString(cursor.getColumnIndex(column)));
                }
            }
        }
        cursor.close();

        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.purchased_homebtn) {
            intent = new Intent(getApplication(), HomePage.class);
        } else if (v.getId() == R.id.purchased_searchbtn) {
            intent = new Intent(getApplication(), ProductSearch.class);
        } else if (v.getId() == R.id.purchased_exhibitbtn) {
            intent = new Intent(getApplication(), Exhibit.class);
        } else if (v.getId() == R.id.purchased_favobtn) {
            intent = new Intent(getApplication(), FavoriteList.class);
        } else if (v.getId() == R.id.purchased_mypagebtn) {
            intent = new Intent(getApplication(), MyPage.class);
        } else if (v.getId() == R.id.backButton) {
            finish();
            return;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("user_id", "");
    }
}