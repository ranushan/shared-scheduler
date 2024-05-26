package com.ranushan.runner;

import com.google.common.collect.EvictingQueue;
import com.ranushan.configuration.BatchConfiguration;
import com.ranushan.configuration.ConfigurationHolder;
import com.ranushan.configuration.GlobalConfiguration;
import com.ranushan.domain.BatchType;
import com.ranushan.util.DateUtils;
import com.ranushan.util.DurationUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Queue;

/**
 * A common interface for all managed batches
 *
 */
@Slf4j
public abstract class AbstractBatch implements Runnable {
    public enum State {
        SET, STARTED, RUNNING, STOPPED, ERROR;
    }

    protected static final String MSG_BATCH_ALREADY_STARTED = "Batch already started";
    protected static final String MSG_BATCH_ALREADY_STOPPED = "Batch already stopped";
    protected static final String MSG_BATCH_ALREADY_RUNNING = "Batch task already in execution";

    @Getter final BatchConfiguration configuration;
    @Getter final ConfigurationHolder configurationHolder;

    private State previousState;
    private State currentState;

    /*
     * Stores the date & time this batch was started (schedule)
     */
    protected Date startDate;

    /*
     * The date & time when this batch task was last executed
     */
    protected Date lastRun;

    protected Duration lastRunDuration;

    /**
     * A history of most recent execution durations.
     */
    private final Queue<Duration> executionDurationHistory;

    /*
     * This object is used to control access to the task execution independently of other
     * operations.
     */
    private final Object runLock = new Object();
    private final Object changeLock = new Object();

    private boolean stopRequested = false;

    protected AbstractBatch(BatchConfiguration configuration, ConfigurationHolder configurationHolder) {
        this.configuration = configuration;
        this.configurationHolder = configurationHolder;
        this.executionDurationHistory = EvictingQueue.create(getMaxHistorySize());
    }

    /**
     * @return This batch's identifier name, as in {@link BatchConfiguration}.
     */
    public String getName() {
        return configuration.getName();
    }

    /**
     * @return This batch's type, as in {@link BatchConfiguration}.
     */
    public BatchType getType() {
        return configuration.getType();
    }

    protected void setState(State currentState) {
        previousState = this.currentState;
        this.currentState = currentState;
    }

    /**
     * @return This batch's current state
     */
    public State getState() {
        return currentState;
    }

    /**
     * @return {@code true} if this batch's timer (not its task) is currently started;
     *         otherwise {@code false}.
     */
    public boolean isStarted() {
        return currentState == State.STARTED || (currentState == State.RUNNING && previousState == State.STARTED);
    }

    /**
     * @return {@code true} if this batch's task is currently running; otherwise
     *         {@code false}.
     */
    public boolean isRunning() {
        return currentState == State.RUNNING;
    }

    /**
     * @return {@code true} if this batch's timer is currently stopped; otherwise
     *         {@code false}.
     */
    public boolean isStopped() {
        return currentState == State.STOPPED;
    }

    /**
     * @return The date and time when this batch was started (scheduled).
     */
    public Date getStartDate() {
        return DateUtils.getClonedDate(startDate);
    }

    /**
     * @return The date and time when this batch task was last executed.
     */
    public Date getLastRunDate() {
        return DateUtils.getClonedDate(lastRun);
    }

    /**
     * Starts this batch timer considering the interval settled in this object for execution.
     */
    public final void start() {
        switch (getState()) {
            case STARTED:
                throw new IllegalStateException(MSG_BATCH_ALREADY_STARTED);
            case STOPPED:
                throw new IllegalStateException("Batch was stopped. Please reset this batch before restarting");
            default:
                break;
        }
        synchronized (changeLock) {
            if (isStarted()) {
                throw new IllegalStateException(MSG_BATCH_ALREADY_STARTED);
            }
            onStart();
            setState(State.STARTED);
            startDate = new Date();
        }
    }

    public abstract void onStart();

    /**
     * Suspends this batch.
     */
    public final void stop() {
        stopRequested = true;
        if (isStopped()) {
            throw new IllegalStateException(MSG_BATCH_ALREADY_STOPPED);
        }
        synchronized (changeLock) {
            if (isStopped()) {
                throw new IllegalStateException(MSG_BATCH_ALREADY_STOPPED);
            }
            log.info("Stopping batch: {}...", getName());
            onStop();
            setState(State.STOPPED);
            startDate = null;
            log.info("Batch {} stopped successfully.", getName());
        }
    }

    public abstract void onStop();

    /**
     * The method called by the system to execute the batch task automatically.
     */
    @Override
    public void run() {
        run(false);
    }

    public void run(boolean manualFlag) {
        if (stopRequested && !manualFlag) return;
        if (isRunning()) {
            if (manualFlag) {
                throw new IllegalStateException(MSG_BATCH_ALREADY_RUNNING);
            }
            log.info(MSG_BATCH_ALREADY_RUNNING);
        } else {
            synchronized (runLock) {
                setState(State.RUNNING);
                lastRun = new Date();
                log.debug("Running batch...");
                try {
                    var start = Instant.now();
                    runTask();
                    updateStatistics(Duration.between(start, Instant.now()));
                    log.debug("Batch finished in {}", lastRunDuration);
                    log.debug("Batch finished in --");
                    afterRun();
                } catch (Exception exception) {
                    log.error("Batch finished with an exception", exception);
                } finally {
                    setState(previousState);
                }
            }
        }
    }

    private void updateStatistics(Duration duration) {
        this.lastRunDuration = duration;
        if (configuration.isEnableStatistics()) {
            executionDurationHistory.offer(lastRunDuration);
        }
    }

    /**
     * Calculates and formats the average of last execution durations. Returns {@code "null"}
     * if no execution is available in the local history.
     *
     * @return a string containing the average of execution durations, or {@code "null"}
     */
    protected String formatAverageRunDuration() {
        if (configuration.isEnableStatistics()) {
            Duration average = getAverageRunDuration();
            return formatDuration(average);
        }
        return "not enabled";
    }

    public Duration getAverageRunDuration() {
        return DurationUtils.average(new ArrayList<>(executionDurationHistory));
    }

    /**
     * Formats the last execution duration for reporting.
     *
     * @return the formatted duration, or {@code "null"}
     */
    protected String formatLastRunDuration() {
        return formatDuration(lastRunDuration);
    }

    /**
     * @param duration the {@link Duration} to be formatted
     * @return the formatted duration, or {@code "null"}
     */
    static String formatDuration(Duration duration) {
        return duration != null ? duration.toString() : "null";
    }

    /**
     * Implements the logic for concrete batches. This method cannot be accessed externally.
     * Its functionality will be available via the run() method.
     */
    protected abstract void runTask();

    /**
     * An event to be fired after batch task run.
     */
    protected abstract void afterRun();

    /**
     * @return {@code true} if a stop request has been sent for this batch
     */
    protected boolean isStopRequested() {
        return stopRequested;
    }

    public abstract String getStatusJson();

    protected String getPresetStatusJsonBuilder() {
        return """
                {
                    "name": %s,
                    "type": %s,
                    "status": %s,
                    "startDate": %s,
                    "lastExecutionStartDate": %s
                    "lastExecutionDuration": %s,
                     "averageExecutionDuration": %s
                }
                """.formatted(getName(), getType(), getState(), DateUtils.formatDate(startDate), DateUtils.formatDate(lastRun),
                formatLastRunDuration(), formatAverageRunDuration()
        );
    }

    /**
     * @return the configured maximum number of durations in the history for statistics, or
     *         zero if statistics not enabled for this batch
     */
    private int getMaxHistorySize() {
        if (configuration.isEnableStatistics()) {
            int maxBatchHistorySize = getGlobalConfiguration().getMaxBatchHistorySize();
            return Math.max(maxBatchHistorySize, 0);
        }
        return 0;
    }

    private GlobalConfiguration getGlobalConfiguration() {
        return configurationHolder.getGlobalConfiguration();
    }
}
