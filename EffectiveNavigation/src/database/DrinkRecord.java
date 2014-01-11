package database;

import java.util.Date;

/**
 * Created by kantei on 2014/01/05.
 */
public class DrinkRecord {

    private long mId;
    private String mDrink;
    private int mWeekDay;
    private String mEndTime;
    private float mVolume;
    public final static String timeFormat = "HH:mm:ss";

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

}
