package com.ranushan.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    protected static final String NULL_STRING = "null";

    public static ZonedDateTime now() {
        return ZonedDateTime.now();
    }

    public static String formatDate(Calendar calendar) {
        return calendar != null ? formatDate(calendar.getTime()) : NULL_STRING;
    }

    public static String formatDate(ZonedDateTime zonedDateTime) {
        return zonedDateTime != null ? formatDate(Date.from(zonedDateTime.toInstant())) : NULL_STRING;
    }

    public static String formatDate(Date date) {
        return date != null ? FORMATTER.format(date.toInstant()) : NULL_STRING;
    }

    public static Date getNextExactDateEveryInterval(int interval, TimeUnit timeUnit) {
        return getNextExactDateEveryInterval(interval, timeUnit, Calendar.getInstance());
    }

    public static Date getNextExactDateEveryInterval(int interval, TimeUnit timeUnit, Date date) {
        Objects.requireNonNull(date, "The source date must not be null");
        Calendar calendar = toCalendar(date);
        return getNextExactDateEveryInterval(interval, timeUnit, calendar);
    }

    static Date getNextExactDateEveryInterval(int interval, TimeUnit timeUnit, Calendar baseCalendar) {
        Objects.requireNonNull(baseCalendar, "The base calendar must not be null");
        Calendar nextCalendar = (Calendar) baseCalendar.clone();

        int time = baseCalendar.get(timeUnit.getCalendarConstant());
        int timeDiff = (time % interval == 0) ? 0 : interval - time % interval;

        nextCalendar.add(timeUnit.getCalendarConstant(), timeDiff);

        if (nextCalendar.before(baseCalendar) || nextCalendar.equals(baseCalendar)) {
            nextCalendar.add(timeUnit.getCalendarConstant(), interval);
        }

        switch (timeUnit) {
            case HOURS   -> nextCalendar.set(Calendar.MINUTE, 0);
            case MINUTES -> nextCalendar.set(Calendar.SECOND, 0);
            case SECONDS -> nextCalendar.set(Calendar.MILLISECOND, 0);
        }
        return nextCalendar.getTime();
    }

    public static Date getClonedDate(Date date) {
        return date != null ? (Date) date.clone() : null;
    }

    private static Calendar toCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}