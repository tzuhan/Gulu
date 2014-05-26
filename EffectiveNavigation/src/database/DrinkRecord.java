package database;

import java.util.Date;

/**
 * Created by kantei on 2014/01/05.
 */
public class DrinkRecord {

    private long mId;
    private String mDrink;
    private int mWeekDay;
    private String mEndTime; //like 13:15:20 is 1PM 15:20
    private float mVolume;
    private String mDate; // like 4/21

    public final static String timeFormat = "HH:mm:ss";
    public final static String DateFormat = "yyyy/MM/dd";

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public void setDrink(String drink) {
        mDrink = drink;
    }

    public String getDrink() {
        return mDrink;
    }

    public void setWeekDay(int weekDay) {
        mWeekDay = weekDay;
    }

    public int getWeekDay() {
        return mWeekDay;
    }

    public void setEndTime(String endTime) {
        mEndTime = endTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public void setVolume(float volume) {
        mVolume = volume;
    }

    public float getVolume() {
        return mVolume;
    }

    public void setDate(String dateStr) { mDate = dateStr; }

    public String getDate() { return mDate; }

}
