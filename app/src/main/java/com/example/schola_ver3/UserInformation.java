package com.example.schola_ver3;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class UserInformation extends AppCompatActivity {
    private ListView resultsListView;
    private ArrayList<String> memberIds;
    private ArrayAdapter<String> adapter;
    private UserDatabase dbHelper;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);

        resultsListView = findViewById(R.id.resultsListView);
        backButton = findViewById(R.id.backButton);
        dbHelper = new UserDatabase(this);
        memberIds = new ArrayList<>();

        // データの読み込み処理を直接行う
        loadMemberIds();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, memberIds);
        resultsListView.setAdapter(adapter);

        // ListViewのアイテムをクリックしたときにUserCheckに遷移
        resultsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMemberId = memberIds.get(position);
            Intent intent = new Intent(UserInformation.this, UserCheck.class);
            intent.putExtra("MEMBER_ID", selectedMemberId); // memberIdを渡す
            startActivity(intent);
        });

        // 戻るボタン
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserInformation.this, ManagerHomepage.class);
            startActivity(intent);
            finish(); // 現在のアクティビティを終了
        });
    }

    // データベースから利用規約IDが0のメンバーIDのみを読み込む
    private void loadMemberIds() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // 利用規約IDが0のメンバーのみを選択
        Cursor cursor = db.rawQuery("SELECT id FROM Members WHERE terms_id = 0", null);

        if (cursor.moveToFirst()) {
            do {
                String memberId = cursor.getString(0);
                memberIds.add(memberId);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }
}
