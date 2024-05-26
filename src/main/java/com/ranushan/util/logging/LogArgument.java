package com.ranushan.util.logging;

import java.util.function.Predicate;

public class LogArgument {
    protected static final String DEFAULT_REPLACEMENT = "<?>";

    private final Predicate<String> predicate;
    private final String original;
    private final String replacement;

    public LogArgument(String pattern, String original) {
        this(pattern, original, DEFAULT_REPLACEMENT);
    }

    public LogArgument(String pattern, String original, String replacement) {
        this(str -> str.matches(pattern), original, replacement);
    }

    private LogArgument(Predicate<String> predicate, String original, String replacement) {
        this.predicate = predicate;
        this.original = original;
        this.replacement = replacement;
    }

    public String getLoggableArgument() {
        return predicate.test(original) ? original : replacement;
    }
}
