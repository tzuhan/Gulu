package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.android.effectivenavigation.DrinksInformation;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by kantei on 2014/01/05.
 */
public class DrinkRecordDataSource {

    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private SimpleDateFormat dateFormatter;
    public static final String[] weekDayRecordsColumns = {DBHelper._WeekDay,
                                                          DBHelper._Drink,
                                                          DBHelper._EndTime,
                                                          DBHelper._Volume};
    private static final String dbTag = DrinkRecordDataSource.class.getName();
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

    public DrinkRecord createDrinkRecord(String drink, Calendar calendar, float volume) {
        ContentValues values = new ContentValues();
        values.put(dbHelper._Drink,drink);
        values.put(dbHelper._WeekDay,calendar.get(Calendar.DAY_OF_WEEK));
        values.put(dbHelper._EndTime,dateFormatter.format(calendar.getTime()));
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

    public void doSomeInsertionTest() {
        createDrinkRecord(DrinksInformation.drinks_list[0], Calendar.getInstance(), 100);
        createDrinkRecord(DrinksInformation.drinks_list[1], Calendar.getInstance(), 120);
        createDrinkRecord(DrinksInformation.drinks_list[2], Calendar.getInstance(), 200);
    }

    public List<DrinkRecord> getWeekDayRecords(int weekDay) {
        List<DrinkRecord> records = new ArrayList<DrinkRecord>();
        Cursor cursor = database.query(DBHelper._TABLE_NAME,
                                       weekDayRecordsColumns,
                                       DBHelper._WeekDay + " = " + weekDay,
                                       null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            records.add(cursorToDrinkRecord(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return records;
    }

    public Cursor getWeekDayCursor(int weekDay) {
        /*
        return database.query(DBHelper._TABLE_NAME,
                weekDayRecordsColumns,
                DBHelper._WeekDay + " = " + weekDay,
                null,null,null,null);
                */
        return database.query(DBHelper._TABLE_NAME,
                DBHelper.allColumns,
                DBHelper._WeekDay + " = " + weekDay,
                null,null,null,null);
    }

    private DrinkRecord cursorToDrinkRecord(Cursor cursor) {
        DrinkRecord newRecord = new DrinkRecord();
        newRecord.setId(cursor.getLong(0));
        newRecord.setDrink(cursor.getString(1));
        newRecord.setWeekDay(cursor.getInt(2));
        newRecord.setEndTime(cursor.getString(3));
        newRecord.setVolume(cursor.getFloat(4));
        return newRecord;
    }

    public void upgradeAndRemoveData(int oldVersion,int newVersion) {
        dbHelper.onUpgrade(database,oldVersion,newVersion);
    }
}
