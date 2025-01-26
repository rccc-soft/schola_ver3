package com.example.schola_ver3;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Exhibit extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE = 1;
    private ActivityResultLauncher<Intent> resultLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private Button decisionbtn;
    private LinearLayout addpicture;
    private EditText productNameEditText, productDescriptionEditText, productPriceEditText;
    private ImageView productImageView;
    private Spinner categorySpinner, deliveryMethodSpinner, regionSpinner;
    private ProductDatabaseHelper dbHelper;
    private byte[] imageByteArray; // 画像データを保持
    private ImageButton backButton;

    String fileName = "Image_" + System.currentTimeMillis() + ".jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exhibit);

        initializeViews();
        setupSpinners();
        setupResultLauncher();
        setupImagePickerLauncher();
        dbHelper = new ProductDatabaseHelper(this);
    }

    private void initializeViews() {
        decisionbtn = findViewById(R.id.decisionbtn);
        decisionbtn.setOnClickListener(this);
        addpicture = findViewById(R.id.addpicture);
        addpicture.setOnClickListener(this);
        productNameEditText = findViewById(R.id.productNameEditText);
        productDescriptionEditText = findViewById(R.id.productDescriptionEditText);
        productPriceEditText = findViewById(R.id.productPriceEditText);
        productImageView = findViewById(R.id.productImageView);
        categorySpinner = findViewById(R.id.categorySpinner);
        deliveryMethodSpinner = findViewById(R.id.deliveryMethodSpinner);
        regionSpinner = findViewById(R.id.regionSpinner);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> deliveryAdapter = ArrayAdapter.createFromResource(this,
                R.array.delivery_method_array, android.R.layout.simple_spinner_item);
        deliveryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deliveryMethodSpinner.setAdapter(deliveryAdapter);

        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(this,
                R.array.region_array, android.R.layout.simple_spinner_item);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);
    }

    private void setupResultLauncher() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {


                        saveProductToDatabase();
                        navigateToExhibitSuccess();
                    } else if (result.getResultCode() == RESULT_CANCELED) {
//                        navigateToHomePage();
                    }
                }
        );
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            productImageView.setImageBitmap(bitmap);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            imageByteArray = stream.toByteArray();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.decisionbtn) {
            if (validateInput()) {
                navigateToExhibitSave();
            }
        } else if (v.getId() == R.id.addpicture) {
            imagePickerLauncher.launch("image/*");
        } else if (v.getId() == R.id.backButton) {
//            Intent intent = new Intent(this, HomePage.class);
//            startActivity(intent);
            finish();
        }
    }

    private boolean validateInput() {
        boolean isValid = true;

        if (productNameEditText.getText().toString().trim().isEmpty()) {
            productNameEditText.setError("商品名を入力してください");
            isValid = false;
        } else if (productNameEditText.getText().toString().length() > 30) {
            productNameEditText.setError("商品名は30文字以内で入力してください");
            isValid = false;
        }

        if (productDescriptionEditText.getText().toString().length() > 1000) {
            productDescriptionEditText.setError("商品説明は1000文字以内で入力してください");
            isValid = false;
        }

        try {
            int price = Integer.parseInt(productPriceEditText.getText().toString());
            if (price < 10 || price > 99999) {
                productPriceEditText.setError("価格は10円から99999円の間で入力してください");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            productPriceEditText.setError("有効な価格を入力してください");
            isValid = false;
        }

        if (categorySpinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            ((TextView)categorySpinner.getSelectedView()).setError("カテゴリを選択してください");
            isValid = false;
        }

        if (imageByteArray == null) {
            // 画像が選択されていない場合のエラー処理
            return false;
        }
        // 特殊文字のチェック
        String specialChars = "\"'\\;:[]{}";
        String productName = productNameEditText.getText().toString();
        String productDescription = productDescriptionEditText.getText().toString();

        if (containsSpecialChars(productName, specialChars)) {
            productNameEditText.setError("商品名に特殊文字は使用できません");
            isValid = false;
        }

        if (containsSpecialChars(productDescription, specialChars)) {
            productDescriptionEditText.setError("商品説明に特殊文字は使用できません");
            isValid = false;
        }

        return isValid;
    }

    private boolean containsSpecialChars(String input, String specialChars) {
        for (char c : specialChars.toCharArray()) {
            if (input.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    private void navigateToExhibitSave() {
        Intent intent = new Intent(this, ExhibitSave.class);

        // 商品情報を Intent に渡す
        intent.putExtra("productName", productNameEditText.getText().toString());
        intent.putExtra("productDescription", productDescriptionEditText.getText().toString());
        intent.putExtra("productPrice", productPriceEditText.getText().toString());

        // カテゴリ、配送方法、地域を渡す
        intent.putExtra("category", categorySpinner.getSelectedItem().toString());
        intent.putExtra("deliveryMethod", deliveryMethodSpinner.getSelectedItem().toString());
        intent.putExtra("region", regionSpinner.getSelectedItem().toString());

        // 画像の URI を追加
        if (imageByteArray != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourAppName");

            Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                try (OutputStream os = getContentResolver().openOutputStream(imageUri)) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent.putExtra("productImageUri", imageUri.toString());
            } else {
                Log.e("Exhibit", "Failed to create new MediaStore record.");
            }
        }

        resultLauncher.launch(intent);
    }

    private void saveProductToDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String sql = "INSERT INTO 商品テーブル (商品ID, 商品名, 商品説明, 商品画像, 商品URL, カテゴリ, 金額, 配送方法, 出品日時, 地域, 出品者ID, 購入済み) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        SQLiteStatement statement = db.compileStatement(sql);

        statement.bindString(1, generateUniqueProductId());
        statement.bindString(2, productNameEditText.getText().toString());
        statement.bindString(3, productDescriptionEditText.getText().toString());
        statement.bindBlob(4, imageByteArray);
        statement.bindString(5, ""); // 商品URL
        statement.bindString(6, categorySpinner.getSelectedItem().toString());
        statement.bindLong(7, Long.parseLong(productPriceEditText.getText().toString()));
        statement.bindString(8, deliveryMethodSpinner.getSelectedItem().toString());
        statement.bindString(9, new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));
        statement.bindLong(10, regionSpinner.getSelectedItemPosition() + 1);
        statement.bindString(11, getCurrentUserId());
        statement.bindLong(12, 0); // 購入済みフラグ（false = 0）

        statement.executeInsert();
    }

    private String generateUniqueProductId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void navigateToExhibitSuccess() {
        Intent intent = new Intent(getApplication(), ExhibitSuccess.class);
        startActivity(intent);
    }

    private void navigateToHomePage() {
        Intent intent = new Intent(getApplication(), HomePage.class);
        startActivity(intent);
    }

    private String getCurrentUserId() {
        // 現在のユーザーIDを取得するロジックを実装
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");
        return userId;
    }
}