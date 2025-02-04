package com.example.schola_ver3;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class UserSearch extends AppCompatActivity {
    private UserDatabase userDatabase;
    private SQLiteDatabase db;
    private EditText searchEditText;
    private ListView resultsListView;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usersearch);

        userDatabase = new UserDatabase(this);
        db = userDatabase.getReadableDatabase();

        // UI components
        searchEditText = findViewById(R.id.editTextText);
        resultsListView = findViewById(R.id.resultsListView);
        backButton = findViewById(R.id.backButton);

        // Set back button functionality to navigate to ManagerHomepage
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserSearch.this, ManagerHomepage.class);
            startActivity(intent);
            finish();
        });

        // Set up real-time search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                performSearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Initially load all data
        performSearch("");

        // Set item click listener to navigate to ChooseAuthority with selected member ID
        resultsListView.setOnItemClickListener((parent, view, position, id) -> {
            // Get the selected member ID
            HashMap<String, String> selectedItem = (HashMap<String, String>) parent.getItemAtPosition(position);
            String selectedMemberId = selectedItem.get("member_id");


            // Pass the member ID to ChooseAuthority activity
            // UserSearch.java の例
            Intent intent = new Intent(UserSearch.this, ChooseAuthority.class);
            intent.putExtra("memberId", selectedMemberId);  // selectedMemberIdは選択された会員ID
            startActivity(intent);

        });
    }

    private void performSearch(String query) {
        // Perform database query with the search term (matching member_id)
        Cursor cursor = db.query(
                UserDatabase.TABLE_PERMISSIONS,  // テーブル名（permissionsテーブル）
                new String[]{UserDatabase.COLUMN_PERMISSION_MEMBER_ID},  // 表示するカラム（member_id）
                UserDatabase.COLUMN_PERMISSION_MEMBER_ID + " LIKE ?",  // 検索条件（member_idがqueryに一致する）
                new String[]{"%" + query + "%"},  // クエリ文字列を検索（部分一致検索）
                null, null, null
        );

        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        while (cursor.moveToNext()) {
            String memberId = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_PERMISSION_MEMBER_ID));
            HashMap<String, String> map = new HashMap<>();
            map.put("member_id", memberId);  // member_idをリストに追加
            results.add(map);
        }
        cursor.close();

        // Set up adapter to display the results
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                results,
                android.R.layout.simple_list_item_1,  // シンプルな1行リストアイテム
                new String[]{"member_id"},  // 表示するデータのキー（member_id）
                new int[]{android.R.id.text1}  // リストアイテムに表示するテキストビュー
        );
        resultsListView.setAdapter(adapter);
    }

}
