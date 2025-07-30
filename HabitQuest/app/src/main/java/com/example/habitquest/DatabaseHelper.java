package com.example.habitquest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // users tabela
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



    // --- TASK TABELA ---
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_TASK_ID = "id";
    public static final String COLUMN_TASK_NAME = "name";
    public static final String COLUMN_TASK_DESCRIPTION = "description";
    public static final String COLUMN_TASK_CATEGORY = "category";
    public static final String COLUMN_TASK_IS_REPEATING = "is_repeating";
    public static final String COLUMN_TASK_REPEAT_INTERVAL = "repeat_interval";
    public static final String COLUMN_TASK_REPEAT_UNIT = "repeat_unit";
    public static final String COLUMN_TASK_START_DATE = "start_date";
    public static final String COLUMN_TASK_END_DATE = "end_date";
    public static final String COLUMN_TASK_EXECUTION_TIME = "execution_time";
    public static final String COLUMN_TASK_IMPORTANCE = "importance";
    public static final String COLUMN_TASK_DIFFICULTY = "difficulty";
    public static final String COLUMN_TASK_XP = "xp_value";
    public static final String COLUMN_TASK_STATUS = "status";


    // SQL za kreiranje tabele users
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


    // sql za tabelu task

    private static final String TABLE_CREATE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + " (" +
                    COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TASK_NAME + " TEXT, " +
                    COLUMN_TASK_DESCRIPTION + " TEXT, " +
                    COLUMN_TASK_CATEGORY + " TEXT, " +
                    COLUMN_TASK_IS_REPEATING + " INTEGER, " +
                    COLUMN_TASK_REPEAT_INTERVAL + " INTEGER, " +
                    COLUMN_TASK_REPEAT_UNIT + " TEXT, " +
                    COLUMN_TASK_START_DATE + " TEXT, " +
                    COLUMN_TASK_END_DATE + " TEXT, " +
                    COLUMN_TASK_EXECUTION_TIME + " TEXT, " +
                    COLUMN_TASK_IMPORTANCE + " TEXT, " +
                    COLUMN_TASK_DIFFICULTY + " TEXT, " +
                    COLUMN_TASK_XP + " INTEGER, " +
                    COLUMN_TASK_STATUS + " TEXT" +
                    ");";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TABLE_CREATE);        // za users tabelu
        db.execSQL(TABLE_CREATE_TASKS);  // za tasks tabelu

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);

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

    public boolean insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, task.getName());
        values.put(COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(COLUMN_TASK_CATEGORY, task.getCategory());
        values.put(COLUMN_TASK_IS_REPEATING, task.isRepeating() ? 1 : 0);
        values.put(COLUMN_TASK_REPEAT_INTERVAL, task.getRepeatInterval());
        values.put(COLUMN_TASK_REPEAT_UNIT, task.getRepeatUnit());
        values.put(COLUMN_TASK_START_DATE, task.getStartDate());
        values.put(COLUMN_TASK_END_DATE, task.getEndDate());
        values.put(COLUMN_TASK_EXECUTION_TIME, task.getExecutionTime());
        values.put(COLUMN_TASK_IMPORTANCE, task.getImportance());
        values.put(COLUMN_TASK_DIFFICULTY, task.getDifficulty());
        values.put(COLUMN_TASK_XP, task.getXpValue());
        values.put(COLUMN_TASK_STATUS, task.getStatus());

        long result = db.insert(TABLE_TASKS, null, values);
        return result != -1;
    }


    // Za demo, vraća hardkodirane kategorije
    //treba uraditi select iz baze za kategorije
    public List<TaskCategory> getAllCategories() {
        List<TaskCategory> list = new ArrayList<>();
        list.add(new TaskCategory(1, "Zdravlje", "#43A047"));
        list.add(new TaskCategory(2, "Učenje", "#1E88E5"));
        list.add(new TaskCategory(3, "Zabava", "#F4511E"));
        list.add(new TaskCategory(4, "Sređivanje", "#FDD835"));
        return list;
    }



}
