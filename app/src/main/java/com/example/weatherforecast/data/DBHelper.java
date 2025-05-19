package com.example.weatherforecast.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.OutfitRecommendation;
import com.example.weatherforecast.model.SavedOutfitEntry;
import com.example.weatherforecast.model.UserPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Helper class para la gestión de la base de datos SQLite de la aplicación.
 * Maneja las operaciones de CRUD para usuarios, preferencias y outfits guardados.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weatheroutfit.db";
    private static final int DATABASE_VERSION = 1;

    // Constantes para la tabla de usuarios
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_LAST_LOGIN = "last_login";

    // Constantes para la tabla de preferencias de usuario
    private static final String TABLE_PREFERENCES = "preferences";
    private static final String COLUMN_PREF_ID = "id";
    private static final String COLUMN_PREF_USERNAME = "username";
    private static final String COLUMN_SURNAME = "surname";
    private static final String COLUMN_GENDER = "gender";
    private static final String COLUMN_COLD_TOLERANCE = "cold_tolerance";
    private static final String COLUMN_HEAT_TOLERANCE = "heat_tolerance";

    // Constantes para la tabla de outfits guardados
    private static final String TABLE_SAVED_OUTFITS = "saved_outfits";
    private static final String COLUMN_OUTFIT_ID = "id";
    private static final String COLUMN_OUTFIT_USERNAME = "username";
    private static final String COLUMN_OUTFIT_DATE = "date";
    private static final String COLUMN_OUTFIT_DATA = "outfit_data";
    private static final String COLUMN_WEATHER_DATA = "weather_data";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final Gson gson = new GsonBuilder().create();

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla de usuarios
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL,"
                + COLUMN_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_LAST_LOGIN + " DATETIME"
                + ")";

        // Crear la tabla de preferencias
        String createPreferencesTable = "CREATE TABLE " + TABLE_PREFERENCES + "("
                + COLUMN_PREF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PREF_USERNAME + " TEXT UNIQUE NOT NULL,"
                + COLUMN_SURNAME + " TEXT,"
                + COLUMN_GENDER + " TEXT,"
                + COLUMN_COLD_TOLERANCE + " TEXT,"
                + COLUMN_HEAT_TOLERANCE + " TEXT,"
                + "FOREIGN KEY (" + COLUMN_PREF_USERNAME + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_USERNAME + ")"
                + ")";

        // Crear la tabla de outfits guardados
        String createSavedOutfitsTable = "CREATE TABLE " + TABLE_SAVED_OUTFITS + "("
                + COLUMN_OUTFIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_OUTFIT_USERNAME + " TEXT NOT NULL,"
                + COLUMN_OUTFIT_DATE + " TEXT NOT NULL,"
                + COLUMN_OUTFIT_DATA + " TEXT NOT NULL,"
                + COLUMN_WEATHER_DATA + " TEXT NOT NULL,"
                + "FOREIGN KEY (" + COLUMN_OUTFIT_USERNAME + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_USERNAME + "),"
                + "UNIQUE(" + COLUMN_OUTFIT_USERNAME + ", " + COLUMN_OUTFIT_DATE + ")"
                + ")";

        db.execSQL(createUsersTable);
        db.execSQL(createPreferencesTable);
        db.execSQL(createSavedOutfitsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // En caso de actualización de BD, eliminar tablas existentes y crear nuevas
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_OUTFITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFERENCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Registra un nuevo usuario en la base de datos
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return true si se registró correctamente, false si el usuario ya existe
     */
    public boolean registerUser(String username, String password) {
        // Verificar si el usuario ya existe
        if (usernameExists(username)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        // Insertar el nuevo usuario
        long result = db.insert(TABLE_USERS, null, values);

        // Crear un perfil de preferencias por defecto para el usuario
        if (result != -1) {
            createDefaultPreferences(username);
        }

        db.close();
        return result != -1;
    }

    /**
     * Crea preferencias por defecto para un nuevo usuario
     * @param username Nombre de usuario
     */
    private void createDefaultPreferences(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PREF_USERNAME, username);
        values.put(COLUMN_SURNAME, "¡Introduce tus apellidos!");
        values.put(COLUMN_GENDER, UserPreferences.Gender.OTHER.name());
        values.put(COLUMN_COLD_TOLERANCE, UserPreferences.Tolerance.NORMAL.name());
        values.put(COLUMN_HEAT_TOLERANCE, UserPreferences.Tolerance.NORMAL.name());

        db.insert(TABLE_PREFERENCES, null, values);
        db.close();
    }

    /**
     * Verifica si un nombre de usuario ya existe en la base de datos
     * @param username Nombre de usuario a verificar
     * @return true si el usuario existe, false en caso contrario
     */
    private boolean usernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /**
     * Verifica las credenciales de un usuario para el inicio de sesión
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return true si las credenciales son válidas, false en caso contrario
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE "
                + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isValid;
    }

    /**
     * Registra la fecha y hora del último inicio de sesión del usuario
     * @param username Nombre de usuario
     */
    public void recordLogin(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGIN, System.currentTimeMillis());

        db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{username});
        db.close();
    }

    /**
     * Guarda o actualiza las preferencias de un usuario
     * @param preferences Objeto con las preferencias del usuario
     * @return true si se guardaron correctamente
     */
    public boolean saveUserPreferences(UserPreferences preferences) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_SURNAME, preferences.getSurname());
        values.put(COLUMN_GENDER, preferences.getGender().name());
        values.put(COLUMN_COLD_TOLERANCE, preferences.getColdTolerance().name());
        values.put(COLUMN_HEAT_TOLERANCE, preferences.getHeatTolerance().name());

        // Verificar si el usuario ya tiene preferencias guardadas
        String query = "SELECT * FROM " + TABLE_PREFERENCES + " WHERE "
                + COLUMN_PREF_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{preferences.getName()});

        long result;
        if (cursor.getCount() > 0) {
            // Actualizar preferencias existentes
            result = db.update(TABLE_PREFERENCES, values,
                    COLUMN_PREF_USERNAME + " = ?", new String[]{preferences.getName()});
        } else {
            // Insertar nuevas preferencias
            values.put(COLUMN_PREF_USERNAME, preferences.getName());
            result = db.insert(TABLE_PREFERENCES, null, values);
        }

        cursor.close();
        db.close();
        return result != -1;
    }

    /**
     * Obtiene las preferencias de un usuario
     * @param username Nombre de usuario
     * @return Objeto UserPreferences con las preferencias del usuario
     */
    public UserPreferences getUserPreferences(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        UserPreferences preferences = new UserPreferences();
        preferences.setName(username);

        String query = "SELECT * FROM " + TABLE_PREFERENCES + " WHERE "
                + COLUMN_PREF_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor.moveToFirst()) {
            preferences.setSurname(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SURNAME)));

            // Convertir string a enum para gender
            String genderStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER));
            try {
                UserPreferences.Gender gender = UserPreferences.Gender.valueOf(genderStr);
                preferences.setGender(gender);
            } catch (IllegalArgumentException e) {
                preferences.setGender(UserPreferences.Gender.OTHER);
                Log.e("DBHelper", "Error al parsear género: " + e.getMessage());
            }

            // Convertir string a enum para cold tolerance
            String coldToleranceStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLD_TOLERANCE));
            try {
                UserPreferences.Tolerance coldTolerance = UserPreferences.Tolerance.valueOf(coldToleranceStr);
                preferences.setColdTolerance(coldTolerance);
            } catch (IllegalArgumentException e) {
                preferences.setColdTolerance(UserPreferences.Tolerance.NORMAL);
                Log.e("DBHelper", "Error al parsear tolerancia al frío: " + e.getMessage());
            }

            // Convertir string a enum para heat tolerance
            String heatToleranceStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HEAT_TOLERANCE));
            try {
                UserPreferences.Tolerance heatTolerance = UserPreferences.Tolerance.valueOf(heatToleranceStr);
                preferences.setHeatTolerance(heatTolerance);
            } catch (IllegalArgumentException e) {
                preferences.setHeatTolerance(UserPreferences.Tolerance.NORMAL);
                Log.e("DBHelper", "Error al parsear tolerancia al calor: " + e.getMessage());
            }
        }

        cursor.close();
        db.close();
        return preferences;
    }

    /**
     * Guarda un outfit con su información meteorológica y fecha
     * @param username Nombre del usuario
     * @param outfit Recomendación de outfit
     * @param weather Información meteorológica
     * @param date Fecha para la que se guarda el outfit
     * @return true si se guardó correctamente
     */
    public boolean saveOutfit(String username, OutfitRecommendation outfit,
                              CurrentWeather weather, Date date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_OUTFIT_USERNAME, username);
        values.put(COLUMN_OUTFIT_DATE, dateFormat.format(date));

        // Serializar objetos a JSON
        String outfitJson = gson.toJson(outfit);
        String weatherJson = gson.toJson(weather);

        values.put(COLUMN_OUTFIT_DATA, outfitJson);
        values.put(COLUMN_WEATHER_DATA, weatherJson);

        // Intentar insertar o actualizar si ya existe para esa fecha
        long result;
        try {
            result = db.insertWithOnConflict(TABLE_SAVED_OUTFITS, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            Log.e("DBHelper", "Error al guardar outfit: " + e.getMessage());
            result = -1;
        }

        db.close();
        return result != -1;
    }

    /**
     * Obtiene un outfit guardado para una fecha específica
     * @param username Nombre del usuario
     * @param date Fecha del outfit
     * @return SavedOutfitEntry con la información del outfit guardado, o null si no existe
     */
    public SavedOutfitEntry getOutfitForDate(String username, Date date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String dateStr = dateFormat.format(date);

        String query = "SELECT * FROM " + TABLE_SAVED_OUTFITS + " WHERE "
                + COLUMN_OUTFIT_USERNAME + " = ? AND " + COLUMN_OUTFIT_DATE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, dateStr});

        SavedOutfitEntry entry = null;

        if (cursor.moveToFirst()) {
            String outfitJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OUTFIT_DATA));
            String weatherJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEATHER_DATA));

            try {
                // Deserializar JSON a objetos
                OutfitRecommendation outfit = gson.fromJson(outfitJson, OutfitRecommendation.class);
                CurrentWeather weather = gson.fromJson(weatherJson, CurrentWeather.class);

                // Crear el objeto con los datos
                entry = new SavedOutfitEntry(outfit, weather, date);
            } catch (Exception e) {
                Log.e("DBHelper", "Error al deserializar datos: " + e.getMessage());
            }
        }

        cursor.close();
        db.close();
        return entry;
    }

    /**
     * Obtiene todos los outfits guardados para un usuario
     * @param username Nombre del usuario
     * @return Lista de outfits guardados
     */
    public List<SavedOutfitEntry> getAllSavedOutfits(String username) {
        List<SavedOutfitEntry> outfitList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_SAVED_OUTFITS + " WHERE "
                + COLUMN_OUTFIT_USERNAME + " = ? ORDER BY " + COLUMN_OUTFIT_DATE + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor.moveToFirst()) {
            do {
                String outfitJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OUTFIT_DATA));
                String weatherJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEATHER_DATA));
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OUTFIT_DATE));

                try {
                    Date savedDate = dateFormat.parse(dateStr);
                    OutfitRecommendation outfit = gson.fromJson(outfitJson, OutfitRecommendation.class);
                    CurrentWeather weather = gson.fromJson(weatherJson, CurrentWeather.class);

                    SavedOutfitEntry entry = new SavedOutfitEntry(outfit, weather, savedDate);
                    outfitList.add(entry);
                } catch (ParseException e) {
                    Log.e("DBHelper", "Error al parsear fecha: " + e.getMessage());
                } catch (Exception e) {
                    Log.e("DBHelper", "Error al deserializar datos: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return outfitList;
    }

    /**
     * Elimina un outfit guardado
     * @param username Nombre del usuario
     * @param date Fecha del outfit a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean deleteOutfit(String username, Date date) {
        SQLiteDatabase db = this.getWritableDatabase();
        String dateStr = dateFormat.format(date);

        int result = db.delete(TABLE_SAVED_OUTFITS,
                COLUMN_OUTFIT_USERNAME + " = ? AND " + COLUMN_OUTFIT_DATE + " = ?",
                new String[]{username, dateStr});

        db.close();
        return result > 0;
    }
}