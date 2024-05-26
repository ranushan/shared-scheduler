package com.ranushan.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ToString
@EqualsAndHashCode
public class TimeInterval {
    private static final String EMPTY_STRING = "";
    private static final Pattern DIGITS_GROUP_PATTERN = Pattern.compile("\\d+");
    private static final Pattern LETTERS_GROUP_PATTERN = Pattern.compile("[a-zA-Z]+");

    private final int duration;
    private final TimeUnit timeUnit;

    public TimeInterval(int duration, TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    public TimeInterval(TimeInterval source) {
        this(source.getDuration(), source.getTimeUnit());
    }

    public static TimeInterval of(String input) {
        int digits = extractFirstDigitGroupFrom(input);
        String timeUnitDescription = extractFirstLetterGroupFrom(input);

        TimeUnit timeUnit = timeUnitDescription.isEmpty() ? TimeUnit.DEFAULT
                : TimeUnit.findByIdentifier(timeUnitDescription);

        return new TimeInterval(digits, timeUnit);
    }

    protected static int extractFirstDigitGroupFrom(String input) {
        Matcher matcher = DIGITS_GROUP_PATTERN.matcher(input);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        }
        throw ExceptionUtils.illegalArgument("No digit found in input string: \"%s\"", input);
    }

    protected static String extractFirstLetterGroupFrom(String input) {
        Matcher matcher = LETTERS_GROUP_PATTERN.matcher(input);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return EMPTY_STRING;
    }

    public long toMillis() {
        return timeUnit.toMillis(duration);
    }
}
