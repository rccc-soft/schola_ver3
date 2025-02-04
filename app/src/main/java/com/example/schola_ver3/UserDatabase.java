package com.example.schola_ver3;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 2; // バージョンを更新

    // Membersテーブル
    public static final String TABLE_MEMBERS = "Members";
    public static final String COLUMN_MEMBER_ID = "id";
    public static final String COLUMN_NAME = "user_name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_KAIINNAME = "kaiinname";
    public static final String COLUMN_HURIGANA = "hurigana";
    public static final String COLUMN_BIRTHDAY = "birthday";
    public static final String COLUMN_SCHOOL = "school";
    public static final String COLUMN_PROFILE_MESSAGE = "profile_message";
    public static final String COLUMN_PROFILE_IMAGE_PATH = "profile_image_path";
    public static final String COLUMN_TERMS_ID = "terms_id";

    // Permissionsテーブル
    public static final String TABLE_PERMISSIONS = "Permissions";
    public static final String COLUMN_PERMISSION_MEMBER_ID = "member_id";
    public static final String COLUMN_PRODUCT_PURCHASE = "product_purchase";
    public static final String COLUMN_PRODUCT_SALE = "product_sale";
    public static final String COLUMN_CHAT = "chat";

    // Membersテーブル作成SQL
    private static final String CREATE_TABLE_MEMBERS =
            "CREATE TABLE " + TABLE_MEMBERS + " (" +
                    COLUMN_MEMBER_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_NAME + " TEXT NOT NULL CHECK(LENGTH(" + COLUMN_NAME + ") BETWEEN 2 AND 10), " +
                    COLUMN_PASSWORD + " TEXT NOT NULL CHECK(LENGTH(" + COLUMN_PASSWORD + ") BETWEEN 8 AND 16), " +
                    COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PHONE_NUMBER + " TEXT NOT NULL, " +
                    COLUMN_KAIINNAME + " TEXT NOT NULL, " +
                    COLUMN_HURIGANA + " TEXT NOT NULL, " +
                    COLUMN_BIRTHDAY + " TEXT NOT NULL, " +
                    COLUMN_SCHOOL + " TEXT NOT NULL, " +
                    COLUMN_PROFILE_MESSAGE + " TEXT, " +
                    COLUMN_PROFILE_IMAGE_PATH + " TEXT, " +
                    COLUMN_TERMS_ID + " TEXT NOT NULL" +
                    ");";

    // Permissionsテーブル作成SQL
    private static final String CREATE_TABLE_PERMISSIONS =
            "CREATE TABLE " + TABLE_PERMISSIONS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PERMISSION_MEMBER_ID + " TEXT NOT NULL, " +
                    COLUMN_PRODUCT_PURCHASE + " INTEGER DEFAULT 1, " +
                    COLUMN_PRODUCT_SALE + " INTEGER DEFAULT 1, " +
                    COLUMN_CHAT + " INTEGER DEFAULT 1, " +
                    "FOREIGN KEY(" + COLUMN_PERMISSION_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(" + COLUMN_MEMBER_ID + ")" +
                    ");";

    public UserDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEMBERS);
        db.execSQL(CREATE_TABLE_PERMISSIONS); // Permissionsテーブルの作成

        insertTestData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the existing Permissions table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERMISSIONS);

        // Create a new Permissions table
        db.execSQL(CREATE_TABLE_PERMISSIONS);
    }


    private void insertTestData(SQLiteDatabase db) {
        // Membersテーブルにテストデータを挿入
        db.execSQL("DELETE FROM " + TABLE_MEMBERS);

        for (int i = 1; i <= 9; i++) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_MEMBER_ID, "user00" + i);
            values.put(COLUMN_NAME, "山田太郎");
            values.put(COLUMN_EMAIL, "user" + i + "@example.com");
            values.put(COLUMN_PHONE_NUMBER, "000-0000-000" + i);
            values.put(COLUMN_PASSWORD, "password123");
            values.put(COLUMN_KAIINNAME, "山田太郎");
            values.put(COLUMN_HURIGANA, "ヤマダタロウ");
            values.put(COLUMN_BIRTHDAY, "2000010" + i);
            values.put(COLUMN_SCHOOL, "東京大学");
            values.put(COLUMN_PROFILE_MESSAGE, "こんにちは！");
            values.put(COLUMN_PROFILE_IMAGE_PATH, "profile" + i + ".jpg");
            values.put(COLUMN_TERMS_ID, "0");

            db.insert(TABLE_MEMBERS, null, values);
        }
        for (int i = 1; i <= 9; i++) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_MEMBER_ID, "user11" + i);
            values.put(COLUMN_NAME, "山田太郎");
            values.put(COLUMN_EMAIL, "user" + i + "@example.com");
            values.put(COLUMN_PHONE_NUMBER, "000-0000-000" + i);
            values.put(COLUMN_PASSWORD, "password123");
            values.put(COLUMN_KAIINNAME, "山田太郎");
            values.put(COLUMN_HURIGANA, "ヤマダタロウ");
            values.put(COLUMN_BIRTHDAY, "2000010" + i);
            values.put(COLUMN_SCHOOL, "東京大学");
            values.put(COLUMN_PROFILE_MESSAGE, "こんにちは！");
            values.put(COLUMN_PROFILE_IMAGE_PATH, "profile" + i + ".jpg");
            values.put(COLUMN_TERMS_ID, "1");

            db.insert(TABLE_MEMBERS, null, values);
        }

    }
}
