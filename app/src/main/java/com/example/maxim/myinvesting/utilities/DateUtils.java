package com.example.maxim.myinvesting.utilities;

import java.sql.Time;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by maxim on 08.04.17.
 */

public class DateUtils {
    // TODO: 17.04.17 Проверить дату, пишет какую-то хрень
    public static long getTimeForMoscowInMillis(int yy, int mm, int dd) {

        GregorianCalendar calendar;

        TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");

        calendar = new GregorianCalendar(timeZone);

        calendar.set(yy, mm, dd);

        return calendar.getTimeInMillis();
    }

    public static String getNormalTimeForMoscow(long time) {

        GregorianCalendar calendar;

        TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");

        calendar= new GregorianCalendar(timeZone);

        calendar.setTimeInMillis(time);

        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);

        return ( Integer.toString(dd) + "/" +
                Integer.toString(mm + 1) + "/" +
                Integer.toString(yy).substring(2));
    }


}
