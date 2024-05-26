package com.ranushan.runner.timer;

import com.ranushan.configuration.BatchConfiguration;
import com.ranushan.configuration.ConfigurationHolder;
import com.ranushan.exception.InvalidClassException;
import com.ranushan.runner.DynamicBatch;

/**
 * A {@link TimerBatch} that runs a dynamic batch object.
 *
 */
public class DynamicTimerBatch extends TimerBatch {

    private final DynamicBatch annotatedBatch;

    /**
     * Creates a new DynamicTimerBatch for the given {@link BatchConfiguration}.
     *
     * @param configuration the {@link BatchConfiguration} to be parsed
     * @throws InvalidClassException if any exception regarding a reflective operation (e.g.:
     *                               class or method not found) occurs
     */
    public DynamicTimerBatch(BatchConfiguration configuration, ConfigurationHolder configurationHolder) {
        super(configuration, configurationHolder);
        annotatedBatch = new DynamicBatch(configuration);
    }

    /**
     * Executes the method annotated with {@code BatchTask} in the annotated batch instance.
     */
    @Override
    protected void runTask() {
        annotatedBatch.runBatchTask();
    }

    /**
     * @return the metadata associated with this AnnotatedTimerBatch
     */
    protected DynamicBatch getMetadata() {
        return annotatedBatch;
    }

    @Override
    public String toString() {
        return "AnnotatedTimerBatch$" + annotatedBatch.getBatchClass().getName();
    }
}