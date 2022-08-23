package ib.edu.heart;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "inter.db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE interval(ID INTEGER PRIMARY KEY AUTOINCREMENT, array TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS interval");
    }
    @SuppressLint("Range")
    public String lastRow() {
        String last = "";
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM interval WHERE ID = (SELECT MAX(ID)  FROM interval)", null);
        query.moveToFirst();
        if (query.moveToFirst()) {
            last = query.getString(query.getColumnIndex("array"));
        }
        return last;
    }

    public boolean insert(String array){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("array", array);
        long result = sqLiteDatabase.insert("interval", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

}
