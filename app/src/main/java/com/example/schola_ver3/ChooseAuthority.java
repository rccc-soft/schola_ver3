package com.example.schola_ver3;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseAuthority extends AppCompatActivity {

    private SQLiteDatabase db;
    private String memberIdString;  // 選択された会員者ID（文字列型）
    private CheckBox buyButton, productButton, chatButton;
    private Button cancelButton, decideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_authority);

        // UI要素を取得
        buyButton = findViewById(R.id.buyButton);
        productButton = findViewById(R.id.productButton);
        chatButton = findViewById(R.id.chatButton);
        cancelButton = findViewById(R.id.cancelButton);
        decideButton = findViewById(R.id.decideButton);

        // データベースの取得
        UserDatabase dbHelper = new UserDatabase(this);
        db = dbHelper.getWritableDatabase();

        // 会員者IDを取得 (Intent から取得)
        memberIdString = getIntent().getStringExtra("memberId");
        Log.d("ChooseAuthority", "Selected memberId: " + memberIdString);

        // memberIdString をそのまま使用
        if (memberIdString != null) {
            // 権限テーブルからデータを取得
            loadPermissions(memberIdString);  // 修正: memberIdをString型で渡す
        } else {
            Log.e("ChooseAuthority", "Invalid memberId format.");
        }

        // ボタンのイベント処理
        cancelButton.setOnClickListener(view -> {
            // 戻るボタン処理
            Intent intent = new Intent(ChooseAuthority.this, UserSearch.class);
            startActivity(intent);
        });

        decideButton.setOnClickListener(view -> {
            // 確定ボタン処理
            updatePermissions();
        });
    }

    private void loadPermissions(String memberIdString) {
        // 権限テーブルからデータを取得
        Cursor cursor = null;
        try {
            cursor = db.query("Permissions", new String[]{"product_purchase", "product_sale", "chat"},
                    "member_id = ?", new String[]{memberIdString}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                // カラムインデックスを安全に取得
                int productPurchaseIndex = cursor.getColumnIndex("product_purchase");
                int productSaleIndex = cursor.getColumnIndex("product_sale");
                int chatIndex = cursor.getColumnIndex("chat");

                // 各カラムが存在するか確認
                if (productPurchaseIndex != -1 && productSaleIndex != -1 && chatIndex != -1) {
                    // 各カラムのデータを取得
                    int productPurchase = cursor.getInt(productPurchaseIndex);
                    int productSale = cursor.getInt(productSaleIndex);
                    int chat = cursor.getInt(chatIndex);

                    // チェックボックスの状態を設定
                    buyButton.setChecked(productPurchase == 1);  // 1の場合はチェック、0の場合は外す
                    productButton.setChecked(productSale == 1);  // 1の場合はチェック、0の場合は外す
                    chatButton.setChecked(chat == 1);            // 1の場合はチェック、0の場合は外す
                }
            } else {
                Log.e("ChooseAuthority", "データベースからのデータ取得に失敗しました。");
            }
        } finally {
            if (cursor != null) {
                cursor.close();  // クローズ処理はfinallyで行う
            }
        }
    }

    private void updatePermissions() {
        // 新しい権限状態を取得
        int newProductPurchase = buyButton.isChecked() ? 1 : 0;
        int newProductSale = productButton.isChecked() ? 1 : 0;
        int newChat = chatButton.isChecked() ? 1 : 0;

        // データベースを更新
        ContentValues values = new ContentValues();
        values.put("product_purchase", newProductPurchase);
        values.put("product_sale", newProductSale);
        values.put("chat", newChat);

        // 更新された行数をチェック
        int rowsUpdated = db.update("Permissions", values, "member_id = ?", new String[]{memberIdString});

        if (rowsUpdated > 0) {
            Toast.makeText(this, "権限が更新されました", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "更新に失敗しました", Toast.LENGTH_SHORT).show();
        }

        // `UserSearch.java` へ遷移
        Intent intent = new Intent(ChooseAuthority.this, UserSearch.class);
        startActivity(intent);
    }
}
