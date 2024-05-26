package com.ranushan.util;

import com.ranushan.exception.BatchConfigurationException;
import com.ranushan.exception.BatchRuntimeException;
import com.ranushan.exception.InvalidClassException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionUtils {

    public static IllegalArgumentException illegalArgument(final String format, final Object... args) {
        return new IllegalArgumentException(String.format(format, args));
    }

    public static IllegalArgumentException illegalArgument(final Throwable cause, final String format,
                                                           final Object... args) {
        return new IllegalArgumentException(String.format(format, args), cause);
    }

    public static IllegalStateException illegalState(final String format, final Object... args) {
        return new IllegalStateException(String.format(format, args));
    }

    public static IllegalStateException illegalState(final Throwable cause, final String format, final Object... args) {
        return new IllegalStateException(String.format(format, args), cause);
    }

    public static InvalidClassException invalidClass(final String format, final Object... args) {
        return new InvalidClassException(String.format(format, args));
    }

    public static InvalidClassException invalidClass(final Throwable cause, final String format, final Object... args) {
        return new InvalidClassException(String.format(format, args), cause);
    }

    public static BatchConfigurationException batchConfiguration(final String format, final Object... args) {
        return new BatchConfigurationException(String.format(format, args));
    }

    public static BatchConfigurationException batchConfiguration(final Throwable cause, final String format,
                                                                 final Object... args) {
        return new BatchConfigurationException(String.format(format, args), cause);
    }

    public static BatchRuntimeException batchRuntime(final String format, final Object... args) {
        return new BatchRuntimeException(String.format(format, args));
    }

    public static BatchRuntimeException batchRuntime(final Throwable cause, final String format, final Object... args) {
        return new BatchRuntimeException(String.format(format, args), cause);
    }
}