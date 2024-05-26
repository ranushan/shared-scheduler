package com.ranushan.runner.timer;

import com.ranushan.configuration.BatchConfiguration;
import com.ranushan.configuration.ConfigurationHolder;
import com.ranushan.domain.BatchType;
import com.ranushan.factory.BatchThreadFactory;
import com.ranushan.runner.AbstractBatch;
import com.ranushan.util.DateUtils;
import com.ranushan.util.TimeInterval;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A thread-safe extensible Batch for tasks that are scheduled in the system to run
 * repeatedly, given an interval that is particular to each task. Available operations
 * are: 'start', 'stop', 'run' and 'reset'
 *
 */
@Slf4j
@Getter
public abstract class TimerBatch extends AbstractBatch {
    private final TimeInterval interval;

    private final BatchThreadFactory threadFactory;
    private final ScheduledExecutorService schedule;

    /**
     * Builds a {@link TimerBatch} from the given configuration.
     *
     * @param configuration the {@link BatchConfiguration} to be set
     */
    protected TimerBatch(BatchConfiguration configuration, ConfigurationHolder configurationHolder) {
        super(configuration, configurationHolder);

        if (configuration.getType() != BatchType.TIMER) {
            throw new IllegalArgumentException("Not a timer batch");
        }

        this.interval = TimeInterval.of(configuration.getInterval());

        threadFactory = new BatchThreadFactory(getName());
        schedule = Executors.newSingleThreadScheduledExecutor(threadFactory);

        setState(State.SET);
    }

    /**
     * Starts this batch timer considering the interval settled in this object for execution.
     */
    @Override
    public final void onStart() {
        log.info("Starting batch: {}", getName());
        log.info("Batch {} scheduled to run every {}.", getName(), interval);

        schedule.scheduleAtFixedRate(this, getInitialDelay(), interval.toMillis(),
                java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    protected long getInitialDelay() {
        if (super.getConfiguration().isModulate()) {
            Date start = DateUtils.getNextExactDateEveryInterval(interval.getDuration(), interval.getTimeUnit());

            if (log.isInfoEnabled()) {
                log.info("First execution of {} will be at: {}", getName(), DateUtils.formatDate(start));
            }

            return start.getTime() - System.currentTimeMillis();
        }
        return 0L;
    }

    /**
     * Terminates this batch timer gracefully. Does not interfere with a currently executing
     * task, if it exists.
     */
    @Override
    public final void onStop() {
        schedule.shutdown();
    }

    @Override
    public void afterRun() {
    }

    /**
     * @return A string with current batch status in JSON format
     */
    @Override
    public String getStatusJson() {
        return getPresetStatusJsonBuilder() +
                """
                {
                    "interval": %s,
                    "modulate": %s
                }
                """.formatted(interval, getConfiguration().isModulate());
    }

    /**
     * Returns the {@link ExecutorService} associated with this batch instance, for testing
     * purposes.
     *
     * @return the {@link ExecutorService}
     */
    protected ScheduledExecutorService getExecutorService() {
        return schedule;
    }
}