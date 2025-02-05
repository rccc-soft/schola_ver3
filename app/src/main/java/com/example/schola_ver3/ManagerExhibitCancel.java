package com.example.schola_ver3;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class ManagerExhibitCancel extends AppCompatActivity {

    private TextView confirmationText;
    private Button yesButton;
    private Button noButton;
    private String productId;
    private ProductDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exhibit_cancel_manager);

        dbHelper = new ProductDatabaseHelper(this);

        confirmationText = findViewById(R.id.confirmationText);
        yesButton = findViewById(R.id.yesButton);
        noButton = findViewById(R.id.noButton);

        // ExhibitEditから渡された商品IDを取得
        productId = getIntent().getStringExtra("商品ID");

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProduct();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToExhibitList();
            }
        });
    }

    private void deleteProduct() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = db.delete("商品テーブル", "商品ID = ?", new String[]{productId});
        db.close();

        if (deletedRows > 0) {
            Toast.makeText(this, "商品が削除されました", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "商品の削除に失敗しました", Toast.LENGTH_SHORT).show();
        }

        navigateToExhibitList();
    }

    private void navigateToExhibitList() {
        Intent intent = new Intent(this, ManagerHomepage.class);
        startActivity(intent);
        finish();
    }
}