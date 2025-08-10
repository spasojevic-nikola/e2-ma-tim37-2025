package com.example.habitquest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_TOKEN = "activation_token";
    public static final String COLUMN_IS_ACTIVE = "is_active";

    public static final String TABLE_USER_PROFILES = "user_profiles";
    private static final String TABLE_USER_BADGES = "user_badges";
    private static final String TABLE_USER_EQUIPMENT = "user_equipment";

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

    private static final String TABLE_CREATE_USER_PROFILES =
            "CREATE TABLE " + TABLE_USER_PROFILES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "level INTEGER DEFAULT 1, " +
                    "title TEXT DEFAULT 'Početnik', " +
                    "power_points INTEGER DEFAULT 0, " +
                    "experience_points INTEGER DEFAULT 0, " +
                    "coins INTEGER DEFAULT 0, " +
                    "qr_code TEXT, " +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";

    private static final String TABLE_CREATE_USER_BADGES =
            "CREATE TABLE " + TABLE_USER_BADGES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "badge_name TEXT, " +
                    "badge_icon TEXT, " +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";

    private static final String TABLE_CREATE_USER_EQUIPMENT =
            "CREATE TABLE " + TABLE_USER_EQUIPMENT + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "equipment_name TEXT, " +
                    "equipment_icon TEXT, " +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, context.getExternalFilesDir("backup") + "/mydb.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        db.execSQL(TABLE_CREATE_USER_PROFILES);
        db.execSQL(TABLE_CREATE_USER_BADGES);
        db.execSQL(TABLE_CREATE_USER_EQUIPMENT);
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

    // Ubaci ili update user profile
    public boolean insertOrUpdateUserProfile(int userId, int level, String title, int powerPoints,
                                             int experiencePoints, int coins, String qrCode) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Provera da li već postoji profil za korisnika
        Cursor cursor = db.query(TABLE_USER_PROFILES,
                new String[]{"id"},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("level", level);
        values.put("title", title);
        values.put("power_points", powerPoints);
        values.put("experience_points", experiencePoints);
        values.put("coins", coins);
        values.put("qr_code", qrCode);

        boolean success;
        if (cursor.moveToFirst()) {
            // Update postojeći zapis
            int profileId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            int rows = db.update(TABLE_USER_PROFILES, values, "id = ?", new String[]{String.valueOf(profileId)});
            success = rows > 0;
        } else {
            // Insert novi zapis
            long result = db.insert(TABLE_USER_PROFILES, null, values);
            success = result != -1;
        }
        cursor.close();
        return success;
    }

    // Ubaci bedž za korisnika
    public boolean insertUserBadge(int userId, String badgeName, String badgeIcon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("badge_name", badgeName);
        values.put("badge_icon", badgeIcon);
        long result = db.insert(TABLE_USER_BADGES, null, values);
        return result != -1;
    }

    // Ubaci opremu za korisnika
    public boolean insertUserEquipment(int userId, String equipmentName, String equipmentIcon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("equipment_name", equipmentName);
        values.put("equipment_icon", equipmentIcon);
        long result = db.insert(TABLE_USER_EQUIPMENT, null, values);
        return result != -1;
    }

    // Učitaj profil korisnika (bez username i avatar koji su u users)
    public Cursor getUserProfile(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USER_PROFILES, null, "user_id = ?", new String[]{String.valueOf(userId)}, null, null, null);
    }


    // Učitaj bedževe korisnika
    public Cursor getUserBadges(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM user_badges WHERE user_id = ?", new String[]{String.valueOf(userId)});
    }

    // Učitaj opremu korisnika
    public Cursor getUserEquipment(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USER_EQUIPMENT, null, "user_id = ?", new String[]{String.valueOf(userId)}, null, null, null);
    }

    public void insertTestUserProfilesIfNotExists(int userId1, int userId2) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Proveri da li profil postoji za userId
        Cursor cursor = db.query(TABLE_USER_PROFILES, null, "user_id = ?", new String[]{String.valueOf(userId1)}, null, null, null);
        if (!cursor.moveToFirst()) {
            // Ubaci prvi red
            ContentValues values1 = new ContentValues();
            values1.put("user_id", userId1);
            values1.put("level", 5);
            values1.put("title", "Test korisnik 1");
            values1.put("power_points", 100);
            values1.put("experience_points", 200);
            values1.put("coins", 500);
            values1.put("qr_code", "QRkodTest1");
            long id1 = db.insert(TABLE_USER_PROFILES, null, values1);
            Log.d("DBHelper", "insertTestUserProfilesIfNotExists: inserted row id = " + id1);

            // Ubaci drugi red
            ContentValues values2 = new ContentValues();
            values2.put("user_id", userId2);
            values2.put("level", 10);
            values2.put("title", "Test korisnik 2");
            values2.put("power_points", 150);
            values2.put("experience_points", 300);
            values2.put("coins", 800);
            values2.put("qr_code", "QRkodTest2");
            long id2 = db.insert(TABLE_USER_PROFILES, null, values2);
            Log.d("DBHelper", "insertTestUserProfilesIfNotExists: inserted row id = " + id2);
        }
        cursor.close();
    }

    public void insertTestUsersIfNotExists(int userId1, int userId2) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Prvi user
        Cursor cursor1 = db.query(TABLE_USERS, null, COLUMN_ID + " = ?", new String[]{String.valueOf(userId1)}, null, null, null);
        if (!cursor1.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, userId1);
            values.put(COLUMN_USERNAME, "dzona123");
            values.put(COLUMN_EMAIL, "spasa007@gmail.com");
            values.put(COLUMN_PASSWORD, "nikolica");
            values.put(COLUMN_AVATAR, R.drawable.avatar2);
            values.put(COLUMN_TOKEN, "");
            values.put(COLUMN_IS_ACTIVE, 1);
            long id = db.insert(TABLE_USERS, null, values);
            Log.d("DBHelper", "insertTwoTestUsersIfNotExists: inserted user 1 with id = " + id);
        }
        cursor1.close();

        // Drugi user
        Cursor cursor2 = db.query(TABLE_USERS, null, COLUMN_ID + " = ?", new String[]{String.valueOf(userId2)}, null, null, null);
        if (!cursor2.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, userId2);
            values.put(COLUMN_USERNAME, "testuser2");
            values.put(COLUMN_EMAIL, "markovicsanja367@gmail.com");
            values.put(COLUMN_PASSWORD, "sanjica");
            values.put(COLUMN_AVATAR, R.drawable.avatar1);
            values.put(COLUMN_TOKEN, "");
            values.put(COLUMN_IS_ACTIVE, 1);
            long id = db.insert(TABLE_USERS, null, values);
            Log.d("DBHelper", "insertTwoTestUsersIfNotExists: inserted user 2 with id = " + id);
        }
        cursor2.close();
    }

    public void insertUserBadgesIfNotExists(int userId) {
        SQLiteDatabase db = getWritableDatabase();

        // Proveri da li već postoje bedževi za korisnika
        Cursor cursor = db.query("user_badges", null, "user_id = ?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            // Primer ubacivanja 2 test bedža za korisnika

            ContentValues values1 = new ContentValues();
            values1.put("user_id", userId);
            values1.put("badge_name", "Fitness Guru");
            values1.put("badge_icon", "ic_badge_fitness_guru");  // naziv drawable resursa

            ContentValues values2 = new ContentValues();
            values2.put("user_id", userId);
            values2.put("badge_name", "Marathon Master");
            values2.put("badge_icon", "ic_badge_marathon_master");

            long id1 = db.insert("user_badges", null, values1);
            long id2 = db.insert("user_badges", null, values2);

            Log.d("DBHelper", "insertUserBadgesIfNotExists: Inserted badges for userId " + userId + " with ids " + id1 + ", " + id2);
        } else {
            Log.d("DBHelper", "insertUserBadgesIfNotExists: Badges already exist for userId " + userId);
        }
        if(cursor != null) cursor.close();
    }

    public void insertUserEquipmentIfNotExists(int userId) {
        SQLiteDatabase db = getWritableDatabase();

        // Proveri da li već postoji oprema za korisnika
        Cursor cursor = db.query("user_equipment", null, "user_id = ?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            // Primer ubacivanja 2 komada opreme za korisnika

            ContentValues values1 = new ContentValues();
            values1.put("user_id", userId);
            values1.put("equipment_name", "Power Gloves");
            values1.put("equipment_icon", "ic_equipment_power_gloves");  // naziv drawable resursa

            ContentValues values2 = new ContentValues();
            values2.put("user_id", userId);
            values2.put("equipment_name", "Speed Boots");
            values2.put("equipment_icon", "ic_equipment_speed_boots");

            long id1 = db.insert("user_equipment", null, values1);
            long id2 = db.insert("user_equipment", null, values2);

            Log.d("DBHelper", "insertUserEquipmentIfNotExists: Inserted equipment for userId " + userId + " with ids " + id1 + ", " + id2);
        } else {
            Log.d("DBHelper", "insertUserEquipmentIfNotExists: Equipment already exists for userId " + userId);
        }
        if(cursor != null) cursor.close();
    }

}
