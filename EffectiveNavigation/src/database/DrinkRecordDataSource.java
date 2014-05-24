package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android.effectivenavigation.DrinksInformation;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by kantei on 2014/01/05.
 */
public class DrinkRecordDataSource {

    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private SimpleDateFormat toTimeFormatter;
    private SimpleDateFormat toDateFormatter;
    public static final String[] drinksRecordsColumns = { DBHelper._Date,
                                                          DBHelper._WeekDay,
                                                          DBHelper._EndTime,
                                                          DBHelper._Drink,
                                                          DBHelper._Volume};

    private static final String dbTag = DrinkRecordDataSource.class.getName();
    public DrinkRecordDataSource(Context context) {
        dbHelper = new DBHelper(context);
        toTimeFormatter = new SimpleDateFormat(DrinkRecord.timeFormat);
        toDateFormatter = new SimpleDateFormat(DrinkRecord.DateFormat);
    }

    public void openDB() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void closeDB() {
        dbHelper.close();
    }


    public DrinkRecord createDrinkRecordAndReturn(String drink, Calendar calendar, float volume) {

        long insertId = insertNewDrinkRecord(drink, calendar, volume);

        Cursor cursor = database.query(DBHelper._TABLE_NAME,
                                       DBHelper.allColumns,
                                       DBHelper._Identifer + " = " + insertId,
                                       null,null,null,null);
        cursor.moveToFirst();
        DrinkRecord newRecord = cursorToDrinkRecord(cursor);
        cursor.close();
        return newRecord;
    }

    public long insertNewDrinkRecord(String drink, Calendar calendar, float volume) {
        ContentValues values = new ContentValues();
        Date dateInfo = calendar.getTime();

        values.put(DBHelper._Drink, drink);
        values.put(DBHelper._WeekDay, calendar.get(Calendar.DAY_OF_WEEK));
        values.put(DBHelper._Date, toDateFormatter.format(dateInfo));
        values.put(DBHelper._EndTime, toTimeFormatter.format(dateInfo));
        values.put(DBHelper._Volume, volume);

        return database.insert(DBHelper._TABLE_NAME, null, values);
    }

    public void doSomeInsertionTest() {
        createDrinkRecordAndReturn(DrinksInformation.drinks_list[0], Calendar.getInstance(), 100);
        createDrinkRecordAndReturn(DrinksInformation.drinks_list[1], Calendar.getInstance(), 120);
        createDrinkRecordAndReturn(DrinksInformation.drinks_list[2], Calendar.getInstance(), 200);
    }

    public List<DrinkRecord> getWeekDayRecords(int weekDay) {
        List<DrinkRecord> records = new ArrayList<DrinkRecord>();
        Cursor cursor = database.query(DBHelper._TABLE_NAME,
                                       drinksRecordsColumns,
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

    public List<DrinkRecord> getDateRecords(String dateStr) {
        List<DrinkRecord> records = new ArrayList<DrinkRecord>();
        Cursor cursor = database.query(DBHelper._TABLE_NAME,
                                       drinksRecordsColumns,
                                       DBHelper._Date + " = " + dateStr,
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
        return database.query(DBHelper._TABLE_NAME,
                DBHelper.allColumns,
                DBHelper._WeekDay + " = " + weekDay,
                null,null,null,null);
    }

    private DrinkRecord cursorToDrinkRecord(Cursor cursor) {
        DrinkRecord newRecord = new DrinkRecord();
        newRecord.setId(cursor.getLong(0)); //need to be beautify
        newRecord.setDrink(cursor.getString(cursor.getColumnIndex(DBHelper._Drink)));
        newRecord.setDate(cursor.getString(cursor.getColumnIndex(DBHelper._Date)));
        newRecord.setWeekDay(cursor.getInt(cursor.getColumnIndex(DBHelper._WeekDay)));
        newRecord.setEndTime(cursor.getString(cursor.getColumnIndex(DBHelper._EndTime)));
        newRecord.setVolume(cursor.getFloat(cursor.getColumnIndex(DBHelper._Volume)));
        return newRecord;
    }

    //remove old records
    public void upgradeAndRemoveData(int oldVersion,int newVersion) {
        dbHelper.onUpgrade(database,oldVersion,newVersion);
    }
}
