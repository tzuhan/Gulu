package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by kantei on 2014/01/05.
 */
public class DBHelper extends SQLiteOpenHelper {

    private final static String dbTag = DBHelper.class.getName();
    private final static int _DB_VERSION = 1;
    private final static String _DB_NAME = "drinkDifferentiation.db";
    public final static String _TABLE_NAME = "drinkRecords";

    //table columns
    public final static String _Identifer = "_id";
    public final static String _Drink = "drink";
    public final static String _WeekDay = "weekDay";
    public final static String _EndTime = "endTime";
    public final static String _Volume = "volume";
    public final static String[] allColumns = {
            _Identifer,
            _Drink,
            _WeekDay,
            _EndTime,
            _Volume
    };

    private final static String createTableSQL = "CREATE TABLE IF NOT EXISTS " + _TABLE_NAME +
                                              "(" +
                                              _Identifer + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                              _Drink + " INTEGER " +
                                              _WeekDay + " INTEGER " +
                                              _EndTime + " DATETIME NOT NULL " +
                                              _Volume + " FLOAT " +
                                              ");";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                    int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    public DBHelper(Context context) {
        super(context, _DB_NAME, null, _DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createTableSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d(dbTag,"Upgrading database from version " + oldVersion + " to " +
                    newVersion + ", which will destroy all old data");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + _TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
