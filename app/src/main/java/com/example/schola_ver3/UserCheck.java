package com.example.schola_ver3;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class UserCheck extends AppCompatActivity {
    private UserDatabase userDatabase;
    private SQLiteDatabase db;
    private String memberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_check);

        userDatabase = new UserDatabase(this);
        db = userDatabase.getWritableDatabase();

        // Get member ID from intent
        memberId = getIntent().getStringExtra("MEMBER_ID");

        // Initialize UI components
        ImageButton backButton = findViewById(R.id.backButton);
        Button rejectButton = findViewById(R.id.okButton);
        Button approveButton = findViewById(R.id.noButton);

        // Load user data and set to fields
        loadUserData();

        // Set back button functionality
        backButton.setOnClickListener(v -> {
            // UserInformation アクティビティにデータをリフレッシュして遷移
            Intent intent = new Intent(UserCheck.this, UserInformation.class);
            intent.putExtra("REFRESH", true); // リフレッシュフラグを送る
            startActivity(intent);
            finish();
        });

        // Reject button: Delete the member record
        rejectButton.setOnClickListener(v -> {
            db.delete(UserDatabase.TABLE_MEMBERS, UserDatabase.COLUMN_MEMBER_ID + "=?", new String[]{memberId});
            // ユーザー情報画面にリフレッシュして遷移
            Intent intent = new Intent(UserCheck.this, UserInformation.class);
            intent.putExtra("REFRESH", true);
            startActivity(intent);
            finish();
        });

        // Approve button: Create permissions table entry & update terms_id
        approveButton.setOnClickListener(v -> {
            createPermissionsEntry();
            updateTermsId();
            checkPermissionsTable();
            // ユーザー情報画面にリフレッシュして遷移
            Intent intent = new Intent(UserCheck.this, UserInformation.class);
            intent.putExtra("REFRESH", true);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        Cursor cursor = db.query(UserDatabase.TABLE_MEMBERS, null, UserDatabase.COLUMN_MEMBER_ID + "=?", new String[]{memberId}, null, null, null);
        if (cursor.moveToFirst()) {
            setEditText(R.id.userid, cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_MEMBER_ID)));
            setEditText(R.id.username, cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_NAME)));
            setEditText(R.id.kaiinname, cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_KAIINNAME)));
            setEditText(R.id.hurigana, cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_HURIGANA)));
            setEditText(R.id.birthday, cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_BIRTHDAY)));
            setEditText(R.id.school, cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_SCHOOL)));
            setEditText(R.id.email, cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_EMAIL)));
            setEditText(R.id.phone, cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_PHONE_NUMBER)));
            setEditText(R.id.profileMessage, cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_PROFILE_MESSAGE)));
        }
        cursor.close();
    }

    private void setEditText(int editTextId, String value) {
        EditText editText = findViewById(editTextId);
        editText.setText(value);
        editText.setEnabled(false);
    }

    private void createPermissionsEntry() {
        // 権限テーブルが存在しない場合は作成
        String createPermissionsTable = "CREATE TABLE IF NOT EXISTS Permissions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "member_id TEXT NOT NULL, " +
                "product_purchase INTEGER DEFAULT 1, " +
                "product_sale INTEGER DEFAULT 1, " +
                "chat INTEGER DEFAULT 1, " +
                "FOREIGN KEY(member_id) REFERENCES Members(id));";
        db.execSQL(createPermissionsTable);

        // 挿入する値の準備
        ContentValues values = new ContentValues();
        values.put("member_id", memberId); // 会員者ID
        values.put("product_purchase", 1); // 商品購入
        values.put("product_sale", 1); // 商品出品
        values.put("chat", 1); // チャット

        // データベースに挿入
        db.insert("Permissions", null, values);
    }

    private void updateTermsId() {
        ContentValues values = new ContentValues();
        values.put(UserDatabase.COLUMN_TERMS_ID, "1");
        db.update(UserDatabase.TABLE_MEMBERS, values, UserDatabase.COLUMN_MEMBER_ID + "=?", new String[]{memberId});
    }
    private void checkPermissionsTable() {
        Cursor cursor = db.query("Permissions", null, "member_id = ?", new String[]{memberId}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            // `Permissions` テーブルの内容をログに出力
            int idIndex = cursor.getColumnIndex("id");
            int memberIdIndex = cursor.getColumnIndex("member_id");
            int productPurchaseIndex = cursor.getColumnIndex("product_purchase");
            int productSaleIndex = cursor.getColumnIndex("product_sale");
            int chatIndex = cursor.getColumnIndex("chat");

            while (!cursor.isAfterLast()) {
                Log.d("PermissionsCheck", "ID: " + cursor.getInt(idIndex) +
                        ", Member ID: " + cursor.getString(memberIdIndex) +
                        ", Product Purchase: " + cursor.getInt(productPurchaseIndex) +
                        ", Product Sale: " + cursor.getInt(productSaleIndex) +
                        ", Chat: " + cursor.getInt(chatIndex));
                cursor.moveToNext();
            }
        } else {
            Log.d("PermissionsCheck", "No data found for member ID: " + memberId);
        }
        cursor.close();
    }

}
