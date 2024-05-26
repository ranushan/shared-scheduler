package com.ranushan.factory;

import com.ranushan.util.ExceptionUtils;
import com.ranushan.util.StringUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An object that creates new threads for SMART batches.
 * <p>
 * The threads created by this factory are identified with the runnable batch name and
 * unique, sequential number.
 * <p>
 * The threads are also set up as non-daemon, to secure Process finalization before system
 * shutdown.
 *
 */
public class BatchThreadFactory implements ThreadFactory {
    private static final String THREAD_NAME_FORMAT = "Batch-%s-thread%d";
    private final AtomicInteger nextSequenceNumber = new AtomicInteger(1);
    private final String batchName;

    /**
     * Creates a new thread factory for the batch identified by the given name.
     *
     * @param batchName the batch name to compose new thread names; not null
     * @throws IllegalArgumentException if the specified name is either null or empty
     */
    public BatchThreadFactory(String batchName) {
        if (StringUtils.isEmpty(batchName)) {
            throw ExceptionUtils.illegalArgument("The batch name is mandatory");
        }
        this.batchName = batchName;
    }

    /**
     * Constructs a new {@link Thread}.
     *
     * @param runnable a {@link Runnable} to be executed by new thread instance
     * @return constructed thread, or null if the request to create a thread is rejected
     */
    @Override
    public Thread newThread(final Runnable runnable) {
        Thread thread = new Thread(runnable, newThreadName());
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setDaemon(false);
        return thread;
    }

    private String newThreadName() {
        return String.format(THREAD_NAME_FORMAT, batchName, nextSequenceNumber.getAndIncrement());
    }
}