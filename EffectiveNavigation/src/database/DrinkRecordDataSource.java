package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kantei on 2014/01/05.
 */
public class DrinkRecordDataSource {

    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private SimpleDateFormat dateFormatter;

    public DrinkRecordDataSource(Context context) {
        dbHelper = new DBHelper(context);
        dateFormatter = new SimpleDateFormat(DrinkRecord.timeFormat);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public DrinkRecord createDrinkRecord(int drink, int weekDay, Date endTime, float volume) {
        ContentValues values = new ContentValues();
        values.put(dbHelper._Drink,drink);
        values.put(dbHelper._WeekDay,weekDay);
        values.put(dbHelper._EndTime,dateFormatter.format(endTime));
        values.put(dbHelper._Volume,volume);
        long insertId = database.insert(DBHelper._TABLE_NAME, null, values);
        Cursor cursor = database.query(DBHelper._TABLE_NAME,
                                       DBHelper.allColumns,
                                       DBHelper._Identifer + " = " + insertId,
                                       null,null,null,null);
        cursor.moveToFirst();
        DrinkRecord newRecord = cursorToDrinkRecord(cursor);
        cursor.close();
        return newRecord;
    }
    /*
    public DrinkRecord[] getWeekDayRecords(int weekDay) {



    }
    */
    private DrinkRecord cursorToDrinkRecord(Cursor cursor) {
        DrinkRecord newRecord = new DrinkRecord();
        newRecord.setId(cursor.getLong(0));
        newRecord.setDrink(cursor.getInt(1));
        newRecord.setWeekDay(cursor.getInt(2));
        newRecord.setEndTime(cursor.getString(3));
        newRecord.setVolume(cursor.getFloat(4));
        return newRecord;
    }

}
