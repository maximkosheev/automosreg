package net.monsterdev.automosreg.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtils {

    public static Date toMoscow(long mills) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        Calendar ret = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        ret.setTimeInMillis(calendar.getTimeInMillis());
        return ret.getTime();
    }

    public static Date toMoscow(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar ret = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        ret.setTimeInMillis(calendar.getTimeInMillis());
        return ret.getTime();
    }
}
