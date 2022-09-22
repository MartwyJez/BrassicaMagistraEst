package ib.edu.heart;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseSensor extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "sensor.db";


    public DataBaseSensor(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE sensorID(ID INTEGER PRIMARY KEY AUTOINCREMENT, sensorID TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS sensorID");
    }
    @SuppressLint("Range")
    public String lastRow() {
        String last = "";
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM sensorID WHERE ID = (SELECT MAX(ID)  FROM sensorID)", null);
        query.moveToFirst();
        if (query.moveToFirst()) {
            last = query.getString(query.getColumnIndex("sensorID"));
        }
        return last;
    }

    public boolean insert(String sensorID){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("sensorID", sensorID);
        long result = sqLiteDatabase.insert("sensorID", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

}

