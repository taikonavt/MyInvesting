package com.example.maxim.myinvesting.utilities;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by maxim on 08.04.17.
 */

public class DateUtils {

    // возвращает время в миллисекундах для указанной даты
    public static long getTimeForMoscowInMillis(int yy, int mm, int dd) {

        GregorianCalendar calendar;

        TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");

        calendar = new GregorianCalendar(timeZone);

        calendar.clear();

        calendar.set(yy, mm, dd, 0, 0, 0);

        return calendar.getTimeInMillis();
    }

    // переводит вермя из миллисекунд в вид дд/мм/гг
    public static String getNormalTimeForMoscow(long time) {

        GregorianCalendar calendar;

        TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");

        calendar= new GregorianCalendar(timeZone);

        calendar.setTimeInMillis(time);

        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);

        return ( Integer.toString(dd) + "/" +
                Integer.toString(mm + 1) + "/" + // перевожу значение константы MONTH на человеческий
                Integer.toString(yy).substring(2)); // убираю первый две цифры от года
    }

    public static long getCurrentDateForMoscowInMillis() {

        GregorianCalendar calendar;

        TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");

        calendar= new GregorianCalendar(timeZone);

        return calendar.getTimeInMillis();
    }
}
