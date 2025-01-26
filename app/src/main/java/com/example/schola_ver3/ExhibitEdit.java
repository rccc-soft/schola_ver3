package com.example.schola_ver3;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ExhibitEdit extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ExhibitEdit";
    private Button savebtn;
    private Button exhibitcancelbtn;
    private Button tradecancelbtn;
    private Button changeImageBtn;
    private ImageButton backButton;
    private EditText productNameEditText, productDescriptionEditText, productPriceEditText;
    private ImageView productImageView;
    private Spinner categorySpinner, deliveryMethodSpinner, regionSpinner;
    private ProductDatabaseHelper dbHelper;
    private String productId;
    private byte[] imageByteArray;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exhibit_edit);

        initializeViews();
        dbHelper = new ProductDatabaseHelper(this);
        setupImagePickerLauncher();

        // Intentから商品情報を取得
        Intent intent = getIntent();
        productId = intent.getStringExtra("商品ID");
        productNameEditText.setText(intent.getStringExtra("商品名"));
        productDescriptionEditText.setText(intent.getStringExtra("商品説明"));
        productPriceEditText.setText(intent.getStringExtra("金額"));

        setCategorySpinnerSelection(intent.getStringExtra("カテゴリ"));
        setDeliveryMethodSpinnerSelection(intent.getStringExtra("配送方法"));
        setRegionSpinnerSelection(intent.getStringExtra("地域"));

        imageByteArray = intent.getByteArrayExtra("商品画像");
        if (imageByteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            productImageView.setImageBitmap(bitmap);
        }

        Log.d(TAG, "Received Product ID: " + productId);
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        savebtn = findViewById(R.id.savebtn);
        savebtn.setOnClickListener(this);

        exhibitcancelbtn = findViewById(R.id.exhibitcancelbtn);
        exhibitcancelbtn.setOnClickListener(this);

        tradecancelbtn = findViewById(R.id.tradecancelbtn);
        tradecancelbtn.setOnClickListener(this);

        changeImageBtn = findViewById(R.id.changeImageBtn);
        changeImageBtn.setOnClickListener(this);

        productNameEditText = findViewById(R.id.productNameEditText);
        productDescriptionEditText = findViewById(R.id.productDescriptionEditText);
        productPriceEditText = findViewById(R.id.productPriceEditText);
        productImageView = findViewById(R.id.productImageView);

        categorySpinner = findViewById(R.id.categorySpinner);
        deliveryMethodSpinner = findViewById(R.id.deliveryMethodSpinner);
        regionSpinner = findViewById(R.id.regionSpinner);

        setupSpinners();
    }

    private void setupSpinners() {
        // カテゴリの設定
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // 配送方法の設定
        ArrayAdapter<CharSequence> deliveryAdapter = ArrayAdapter.createFromResource(this,
                R.array.delivery_method_array, android.R.layout.simple_spinner_item);
        deliveryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deliveryMethodSpinner.setAdapter(deliveryAdapter);

        // 地域の設定
        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(this,
                R.array.region_array, android.R.layout.simple_spinner_item);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);
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

    private void setCategorySpinnerSelection(String category) {
        if (category != null) {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) categorySpinner.getAdapter();
            int position = adapter.getPosition(category);
            if (position != -1) {
                categorySpinner.setSelection(position);
            }
        }
    }

    private void setDeliveryMethodSpinnerSelection(String deliveryMethod) {
        if (deliveryMethod != null) {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) deliveryMethodSpinner.getAdapter();
            int position = adapter.getPosition(deliveryMethod);
            if (position != -1) {
                deliveryMethodSpinner.setSelection(position);
            }
        }
    }

    private void setRegionSpinnerSelection(String region) {
        if (region != null) {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) regionSpinner.getAdapter();
            int position = adapter.getPosition(region);
            if (position != -1) {
                regionSpinner.setSelection(position);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.savebtn) {
            saveProductData();
        } else if (v.getId() == R.id.exhibitcancelbtn) {
            Intent intent = new Intent(this, ExhibitCancel.class);
            intent.putExtra("商品ID", productId);
            startActivity(intent);
        } else if (v.getId() == R.id.tradecancelbtn) {
            // トレードキャンセルの処理を実装
        } else if (v.getId() == R.id.changeImageBtn) {
            imagePickerLauncher.launch("image/*");
        } else if (v.getId() == R.id.backButton) {
            Intent intent = new Intent(this, ExhibitList.class);
            startActivity(intent);
        }
    }

    private void saveProductData() {
        if (productId == null || productId.isEmpty()) {
            Log.e(TAG, "Product ID is null or empty");
            Toast.makeText(this, "商品IDが無効です", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateInput()) {
            Toast.makeText(this, "入力内容を確認してください", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put("商品名", productNameEditText.getText().toString());
            values.put("商品説明", productDescriptionEditText.getText().toString());
            values.put("金額", productPriceEditText.getText().toString());
            values.put("カテゴリ", categorySpinner.getSelectedItem().toString());
            values.put("配送方法", deliveryMethodSpinner.getSelectedItem().toString());
            values.put("地域", regionSpinner.getSelectedItem().toString());
            values.put("商品画像", imageByteArray);

            Log.d(TAG, "Updating product with ID: " + productId);
            Log.d(TAG, "Update values: " + values.toString());

            int rowsAffected = db.update("商品テーブル", values, "商品ID = ?", new String[]{productId});
            Log.d(TAG, "Rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                db.setTransactionSuccessful();
                Toast.makeText(this, "商品情報が更新されました", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "商品情報の更新に失敗しました", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating product: ", e);
            Toast.makeText(this, "エラーが発生しました: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
            db.close();
        }
        finish();
    }

    private boolean containsSpecialChars(String input, String specialChars) {
        for (char c : specialChars.toCharArray()) {
            if (input.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    private boolean validateInput() {
        boolean isValid = true;
        String specialChars = "\"'\\;:[]{}";

        String productName = productNameEditText.getText().toString();
        String productDescription = productDescriptionEditText.getText().toString();
        String productPrice = productPriceEditText.getText().toString();

        if (productName.isEmpty() || productName.length() > 30) {
            productNameEditText.setError("商品名は1〜30文字で入力してください");
            isValid = false;
        } else if (containsSpecialChars(productName, specialChars)) {
            productNameEditText.setError("商品名に特殊文字は使用できません");
            isValid = false;
        }

        if (productDescription.length() > 1000) {
            productDescriptionEditText.setError("商品説明は1000文字以内で入力してください");
            isValid = false;
        } else if (containsSpecialChars(productDescription, specialChars)) {
            productDescriptionEditText.setError("商品説明に特殊文字は使用できません");
            isValid = false;
        }

        try {
            int price = Integer.parseInt(productPrice);
            if (price < 10 || price > 99999) {
                productPriceEditText.setError("価格は10円から99999円の間で入力してください");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            productPriceEditText.setError("有効な価格を入力してください");
            isValid = false;
        }

        return isValid;
    }
}