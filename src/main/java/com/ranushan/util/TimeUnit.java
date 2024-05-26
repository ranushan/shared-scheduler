package com.ranushan.util;

import lombok.Getter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public enum TimeUnit {

    SECONDS(java.util.concurrent.TimeUnit.SECONDS,
            Calendar.SECOND,
            Arrays.asList("second", "seconds", "second(s)", "s"),
            "second(s)"),

    MINUTES(java.util.concurrent.TimeUnit.MINUTES,
            Calendar.MINUTE,
            Arrays.asList("minute", "minutes", "minute(s)", "m"),
            "minute(s)"),

    HOURS(java.util.concurrent.TimeUnit.HOURS,
            Calendar.HOUR_OF_DAY,
            Arrays.asList("hour", "hours", "hour(s)", "h"),
            "hour(s)");

    public static final TimeUnit DEFAULT = TimeUnit.MINUTES;

    private final java.util.concurrent.TimeUnit javaTimeUnit;
    @Getter private final int calendarConstant;
    private final List<String> identifiers;
    private final String displayText;

    TimeUnit(java.util.concurrent.TimeUnit javaTimeUnit, int calendarConstant, List<String> identifiers, String displayText) {
        this.javaTimeUnit = javaTimeUnit;
        this.calendarConstant = calendarConstant;
        this.identifiers = identifiers;
        this.displayText = displayText;
    }

    public static TimeUnit findByIdentifier(String identifier) {
        return Arrays.stream(TimeUnit.values())
                .filter(timeUnit -> timeUnit.isIdentifiableBy(identifier))
                .findFirst()
                .orElseThrow(() -> ExceptionUtils.illegalArgument("Invalid time unit identifier: \"%s\"", identifier));
    }

    public boolean isIdentifiableBy(String identifier) {
        return StringUtils.isNotEmpty(identifier)
                && identifiers.stream()
                              .anyMatch(timeUnitIdentifier -> timeUnitIdentifier.equalsIgnoreCase(identifier));
    }

    @Override
    public String toString()
    {
        return displayText;
    }

    public long toMillis(long amount)
    {
        return javaTimeUnit.toMillis(amount);
    }

    public long convert(long amount, TimeUnit sourceTimeUnit) {
        return javaTimeUnit.convert(amount, sourceTimeUnit.javaTimeUnit);
    }
}
