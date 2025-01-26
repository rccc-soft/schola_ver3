package com.example.schola_ver3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ExhibitDetails extends AppCompatActivity implements View.OnClickListener {

    private ImageButton backButton;
    private Button exhibitEditButton;
    private Button chatListButton;
    private Button shippingButton;
    private Button buyerRatingButton;

    private DatabaseHelper dbHelper; // 購入テーブル（仮）
//    private int currentItemId = 4; // 該当する商品ID (仮の値)
    private boolean isShipped;
    private boolean itemExists;
    private String productId; // 商品ID
    private int purchaseId; // 購入ID

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exhibit_details);

        // DBヘルパーの初期化
        dbHelper = new DatabaseHelper(this);

        // Intent から productId を受け取る
        Intent intent = getIntent();
        productId = intent.getStringExtra("商品ID");

        if (productId != null) {
            // 購入IDの取得
            purchaseId = dbHelper.getPurchaseIdByItemId(productId);

            // 商品IDの存在確認と配送状況取得
            itemExists = dbHelper.doesItemExist(productId);
            if (itemExists) {
                isShipped = dbHelper.isItemShipped(productId);
            }
        } else {
            // productIdがnullの場合のエラー処理
            Toast.makeText(this, "商品IDが見つかりません", Toast.LENGTH_SHORT).show();
            finish(); // アクティビティを終了
            return;
        }

        // UIの初期化
        backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        exhibitEditButton = findViewById(R.id.exhibitEditButton);
        exhibitEditButton.setOnClickListener(this);

        chatListButton = findViewById(R.id.chatListButton);
        chatListButton.setOnClickListener(this);

        shippingButton = findViewById(R.id.shippingButton);
        shippingButton.setOnClickListener(this);

        buyerRatingButton = findViewById(R.id.buyerRatingButton);
        buyerRatingButton.setOnClickListener(this);

        // 配送状況に基づくボタンの有効化/無効化
//        updateButtonStates();
    }

    // 配送状況に基づくボタンの状態更新
    private void updateButtonStates() {
        if (!itemExists) {
            // 商品IDが存在しない場合、ボタンを無効化
            shippingButton.setEnabled(false);
            buyerRatingButton.setEnabled(false);
        } else if (isShipped) {
            // 配送済みの場合の状態
            shippingButton.setEnabled(false);
            buyerRatingButton.setEnabled(true);
        } else {
            // 未配送の場合の状態
            shippingButton.setEnabled(true);
            buyerRatingButton.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            // 出品一覧画面へ
            Intent nextIntent = new Intent(getApplication(), ExhibitList.class);
            startActivity(nextIntent);
        } else if (v.getId() == R.id.exhibitEditButton) {
                // 出品編集画面へ
                Intent nextIntent = new Intent(getApplication(), ExhibitEdit.class);
                ProductDatabaseHelper productDbHelper = new ProductDatabaseHelper(this);
                android.database.Cursor cursor = productDbHelper.getProductInfo(productId);

                if (cursor != null && cursor.moveToFirst()) {
                    nextIntent.putExtra("商品ID", productId);
                    nextIntent.putExtra("商品名", cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_NAME)));
                    nextIntent.putExtra("商品説明", cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_DESCRIPTION)));
                    nextIntent.putExtra("金額", cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_PRICE)));
                    nextIntent.putExtra("カテゴリ", cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_CATEGORY)));
                    nextIntent.putExtra("配送方法", cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_DELIVERY)));
                    nextIntent.putExtra("地域", cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_REGION)));
                    nextIntent.putExtra("商品画像", cursor.getBlob(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_IMAGE)));
                    cursor.close();
                } else {
                    Toast.makeText(this, "商品情報の取得に失敗しました", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(nextIntent);
        } else if (v.getId() == R.id.chatListButton) {
            // チャット一覧画面へ
//            Intent nextIntent = new Intent(getApplication(), ChatList.class);
//            nextIntent.putExtra("product_id", productId); // 商品ID
//            startActivity(nextIntent);
        } else if (v.getId() == R.id.shippingButton) {
            if (!itemExists) {
                Toast.makeText(this, "購入が完了していません", Toast.LENGTH_SHORT).show();
            } else if (!isShipped) {
                Intent nextIntent = new Intent(getApplication(), QuickResponse.class);
                nextIntent.putExtra("purchase_id", purchaseId); // 購入ID
                startActivity(nextIntent);
            } else {
                Toast.makeText(this, "すでに発送済みです", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.buyerRatingButton) {
            if (!itemExists) {
                Toast.makeText(this, "購入が完了していません", Toast.LENGTH_SHORT).show();
            } else if (isShipped) {
                // 出品者評価が済んでいるかを判定
                if (!dbHelper.isSellerRated(purchaseId)) {
                    Toast.makeText(this, "出品者評価が済んでいないため、購入者評価を行えません", Toast.LENGTH_SHORT).show();
                } else {
                    // 出品者IDを取得
                    ProductDatabaseHelper productDbHelper = new ProductDatabaseHelper(this);
                    String sellerId = productDbHelper.getSellerIdByProductId(productId);

                    // 購入者IDを取得
                    String buyerId = dbHelper.getBuyerIdByPurchaseId(purchaseId);

                    // 購入者評価画面へ
                    // 次のアクティビティにデータを渡す
                    Intent nextIntent = new Intent(getApplication(), EvaluationSell.class);
                    nextIntent.putExtra("product_id", productId); // 商品ID
                    nextIntent.putExtra("seller_member_id", sellerId);   // 出品者ID
                    nextIntent.putExtra("buyer_member_id", buyerId);     // 購入者ID
                    startActivity(nextIntent);
                }
            } else {
                Toast.makeText(this, "まだ発送されていません", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
