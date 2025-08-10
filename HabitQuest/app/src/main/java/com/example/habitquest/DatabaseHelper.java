package com.example.habitquest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "habitquest.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_TOKEN = "activation_token";
    public static final String COLUMN_IS_ACTIVE = "is_active";

    // SQL za kreiranje tabele
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE, " +
                    COLUMN_EMAIL + " TEXT UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_AVATAR + " INTEGER, " +
                    COLUMN_TOKEN + " TEXT, " +
                    COLUMN_IS_ACTIVE + " INTEGER DEFAULT 0" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Upis korisnika sa avatarom i aktivacionim tokenom
    public boolean insertUser(String username, String email, String password, int avatar, String token) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Proveri da li korisnik već postoji po emailu ili username-u
        Cursor cursor = db.query(TABLE_USERS,
                null,
                COLUMN_EMAIL + "=? OR " + COLUMN_USERNAME + "=?",
                new String[]{email, username},
                null, null, null);

        if (cursor.getCount() > 0) {
            cursor.close();
            return false; // korisnik postoji
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_AVATAR, avatar);
        values.put(COLUMN_TOKEN, token);
        values.put(COLUMN_IS_ACTIVE, 0);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Aktiviraj korisnika (koristi se kod ručne aktivacije ili iz backend-a)
    public void activateUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_ACTIVE, 1);
        db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{email});
    }

    // Provera korisnika pri loginu (i da je aktiviran)
    public boolean checkUserActive(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                null,
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=? AND " + COLUMN_IS_ACTIVE + "=1",
                new String[]{email, password},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Provera korisnika po email + token (za backend aktivaciju)
    public boolean checkUserToken(String email, String token) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                null,
                COLUMN_EMAIL + "=? AND " + COLUMN_TOKEN + "=?",
                new String[]{email, token},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Dohvati avatar po emailu (opciono)
    public int getUserAvatar(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_AVATAR},
                COLUMN_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        int avatar = -1;
        if (cursor.moveToFirst()) {
            avatar = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AVATAR));
        }
        cursor.close();
        return avatar;
    }
}
