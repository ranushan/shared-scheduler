package com.ranushan.domain;

import com.ranushan.configuration.BatchConfiguration;
import com.ranushan.configuration.ConfigurationHolder;
import com.ranushan.runner.AbstractBatch;
import com.ranushan.runner.cron.DynamicCronBatch;
import com.ranushan.runner.timer.DynamicTimerBatch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Enumerates available {@link com.ranushan.annotation.Batch} types and associated facilities.
 *
 */
@Getter
@RequiredArgsConstructor
public enum BatchType {
    /**
     * An object that runs a particular task periodically, given a configurable interval in
     * seconds, minutes, or hours.
     */
    TIMER("1 minute", DynamicTimerBatch::new),

    /**
     * An object that runs a particular task at specified times and dates, similar to the Cron
     * service available in Unix/Linux systems.
     */
    CRON("* * * * *", DynamicCronBatch::new);

    /**
     * Returns the default interval for a batch type.
     *
     */
    private final String defaultInterval;

    /**
     * Returns the default factory {@link BiFunction} to be applied for instantiating new batches
     * of this type.
     *
     */
    private final BiFunction<BatchConfiguration, ConfigurationHolder, AbstractBatch> factoryFunction;
}
