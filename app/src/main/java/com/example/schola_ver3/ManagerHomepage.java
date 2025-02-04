package com.example.schola_ver3; // あなたのアプリのパッケージ名に置き換えてください

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ManagerHomepage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_homepage); // activity_manager_homepage.xml のレイアウトを設定

        // ボタンのIDを取得
        Button userBtn = findViewById(R.id.userbtn);
        Button productBtn = findViewById(R.id.productbtn);
        Button applicationBtn = findViewById(R.id.applicationbtn);
        Button searchBtn = findViewById(R.id.search);

        // 会員者検索ボタンのクリックイベント
        userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerHomepage.this, UserSearch.class);
                startActivity(intent);
            }
        });

        // 商品検索ボタンのクリックイベント
        productBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerHomepage.this, UserInformation.class);
                startActivity(intent);
            }
        });

        // 申請一覧ボタンのクリックイベント
        applicationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerHomepage.this, UserInformation.class);
                startActivity(intent);
            }
        });


    }
}