package com.ranushan.runner.cron;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.ranushan.configuration.BatchConfiguration;
import com.ranushan.configuration.ConfigurationHolder;
import com.ranushan.domain.BatchType;
import com.ranushan.factory.BatchThreadFactory;
import com.ranushan.runner.AbstractBatch;
import com.ranushan.util.DateUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A batch that runs a particular task at specified times and dates, similar to the Cron
 * service available in Unix/Linux systems.
 *
 */
@Slf4j
@Getter
public abstract class CronBatch extends AbstractBatch {

    private final String cronExpression;
    private final String cronDescription;

    private final BatchThreadFactory threadFactory;
    private final ScheduledExecutorService schedule;
    private final Cron cron;

    private ZonedDateTime nextExecutionDate;

    /**
     * Builds a {@link CronBatch} from the given configuration.
     *
     * @param configuration the {@link BatchConfiguration} to be set
     */
    protected CronBatch(BatchConfiguration configuration, ConfigurationHolder configurationHolder) {
        super(configuration, configurationHolder);

        if (configuration.getType() != BatchType.CRON) {
            throw new IllegalArgumentException("Not a cron batch");
        }

        String originalExpression = configuration.getInterval();
        cron = parseCron(originalExpression);
        cronExpression = cron.asString();
        cronDescription = CronDescriptor.instance().describe(cron);

        threadFactory = new BatchThreadFactory(getName());
        schedule = Executors.newSingleThreadScheduledExecutor(threadFactory);

        setState(State.SET);
    }

    protected static Cron parseCron(String expression) {
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser cronParser = new CronParser(cronDefinition);
        return cronParser.parse(expression);
    }

    protected void scheduleFirstExecution() {
        scheduleNextExecution(true);
    }

    protected void scheduleNextExecution() {
        scheduleNextExecution(false);
    }

    private synchronized void scheduleNextExecution(boolean firstExecution) {
        nextExecutionDate = null;
        if (firstExecution || (isStarted() && !isStopRequested())) {
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            Optional<Duration> optional = executionTime.timeToNextExecution(DateUtils.now());

            if (optional.isPresent()) {
                Duration timeToNextExecution = optional.get();
                schedule.schedule(this, timeToNextExecution.toMillis(), TimeUnit.MILLISECONDS);
                nextExecutionDate = DateUtils.now().plus(timeToNextExecution);

                if (log.isInfoEnabled()) {
                    log.info("{} execution of {} will be at: {}", firstExecution ? "First" : "Next", getName(),
                            DateUtils.formatDate(nextExecutionDate));
                }
            }
            else {
                log.warn("No future execution for the Cron expression: \"{}\"", cronExpression);
            }
        }
    }

    /**
     * Starts this batch schedule considering the Cron expression.
     */
    @Override
    public final void onStart() {
        log.info("Starting batch: {}", getName());
        log.info("Batch {} scheduled to run {}.", getName(), cronDescription);
        scheduleFirstExecution();
    }

    @Override
    public final void onStop() {
        schedule.shutdown();
        nextExecutionDate = null;
    }

    @Override
    public final void afterRun() {
        scheduleNextExecution();
    }

    /**
     * @return A string with current batch status in JSON format
     */
    @Override
    public String getStatusJson() {
        return getPresetStatusJsonBuilder() +
                """
                {
                    "cronExpression": %s,
                    "cronDescription": %s,
                    "nextExecutionDate": %s
                }
                """.formatted(cronExpression, cronDescription, DateUtils.formatDate(nextExecutionDate));
    }

    public Optional<ZonedDateTime> getNextExecutionDate() {
        return Optional.ofNullable(nextExecutionDate);
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
