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

    // --- TABELA KATEGORIJA ---
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_COLOR = "color";
    public static final String COLUMN_TASK_CATEGORY_COLOR = "category_color";



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
                    COLUMN_TASK_STATUS + " TEXT, " +
                    COLUMN_TASK_CATEGORY_COLOR + " TEXT " +
                    ");";


    // SQL za kreiranje tabele kategorija
    private static final String TABLE_CREATE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                    COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CATEGORY_NAME + " TEXT UNIQUE, " +
                    COLUMN_CATEGORY_COLOR + " TEXT" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TABLE_CREATE);        // za users tabelu
        db.execSQL(TABLE_CREATE_TASKS);  // za tasks tabelu
        db.execSQL(TABLE_CREATE_CATEGORIES);    //za categories tabelu


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);

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
        values.put(COLUMN_TASK_CATEGORY_COLOR, task.getCategoryColor());


        long result = db.insert(TABLE_TASKS, null, values);
        return result != -1;
    }


//    // Za demo, vraća hardkodirane kategorije
//    //treba uraditi select iz baze za kategorije
//    public List<TaskCategory> getAllCategories() {
//        List<TaskCategory> list = new ArrayList<>();
//        list.add(new TaskCategory(1, "Zdravlje", "#43A047"));
//        list.add(new TaskCategory(2, "Učenje", "#1E88E5"));
//        list.add(new TaskCategory(3, "Zabava", "#F4511E"));
//        list.add(new TaskCategory(4, "Sređivanje", "#FDD835"));
//        return list;
//    }

    // Dodavanje nove kategorije
    public boolean insertCategory(String name, String color) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Provera da li boja već postoji
        Cursor c = db.query(TABLE_CATEGORIES, null, COLUMN_CATEGORY_COLOR + "=?", new String[]{color}, null, null, null);
        if (c.moveToFirst()) {
            c.close();
            return false; // već postoji ta boja
        }
        c.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        values.put(COLUMN_CATEGORY_COLOR, color);
        long result = db.insert(TABLE_CATEGORIES, null, values);
        return result != -1;
    }

    // Lista svih kategorija
    public List<TaskCategory> getAllCategoriesFromDb() {
        List<TaskCategory> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME));
            String color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_COLOR));
            list.add(new TaskCategory(id, name, color));
        }
        cursor.close();
        return list;
    }

    // Menjanje boje kategorije (i boje svih zadataka te kategorije)
    public boolean updateCategoryColor(int id, String newColor) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Provera da li boja već postoji
        Cursor c = db.query(TABLE_CATEGORIES, null, COLUMN_CATEGORY_COLOR + "=?", new String[]{newColor}, null, null, null);
        if (c.moveToFirst()) {
            c.close();
            return false; // već postoji ta boja
        }
        c.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_COLOR, newColor);
        int rows = db.update(TABLE_CATEGORIES, values, COLUMN_CATEGORY_ID + "=?", new String[]{String.valueOf(id)});

        // 1. Pronađi ime kategorije kojoj menjaš boju
        Cursor catCursor = db.query(TABLE_CATEGORIES, new String[]{COLUMN_CATEGORY_NAME}, COLUMN_CATEGORY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        String categoryName = null;
        if (catCursor.moveToFirst()) {
            categoryName = catCursor.getString(catCursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME));
        }
        catCursor.close();
        if (categoryName != null) {
            ContentValues taskUpdate = new ContentValues();
            taskUpdate.put(COLUMN_TASK_CATEGORY_COLOR, newColor);
            db.update(TABLE_TASKS, taskUpdate, COLUMN_TASK_CATEGORY + "=?", new String[]{categoryName});
        }

        return rows > 0;
    }


    // Provera da li postoji kategorija sa tom bojom
    public boolean categoryColorExists(String color) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_CATEGORIES + " WHERE " + COLUMN_CATEGORY_COLOR + " = ? LIMIT 1",
                new String[]{color}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }







}
