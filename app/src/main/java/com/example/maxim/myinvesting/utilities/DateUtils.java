package com.example.maxim.myinvesting.utilities;

import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by maxim on 08.04.17.
 */

public class DateUtils {

    public static long getTimeForMoscowInMillis(int yy, int mm, int dd) {

        GregorianCalendar calendar;

        TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");

        calendar = new GregorianCalendar(timeZone);

        calendar.set(yy, mm, dd);

        return calendar.getTimeInMillis();
    }


}
