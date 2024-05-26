package com.ranushan;

import com.ranushan.configuration.BatchConfiguration;
import com.ranushan.configuration.ConfigurationHolder;
import com.ranushan.factory.BatchFactory;
import com.ranushan.runner.AbstractBatch;
import com.ranushan.scanner.AnnotatedBatchScanner;
import com.ranushan.util.CommonRegEx;
import com.ranushan.util.ExceptionUtils;
import com.ranushan.util.ObjectUtils;
import com.ranushan.util.StringUtils;
import com.ranushan.util.logging.LogArgument;
import com.ranushan.util.logging.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
public class BatchManager {

    private static final String MSG_INVALID_BATCH = "Invalid batch: %s";
    private static final String MSG_BATCH_STARTED_PLEASE_STOP_FIRST = "'%s' is started. Please stop the batch before this operation.";

    private final Map<String, AbstractBatch> batchesByName = new TreeMap<>();
    private final Map<String, BatchConfiguration> batchesByClass = new TreeMap<>();

    private final ConfigurationHolder configurationHolder;

    private BatchManager(BatchRunner runner) {
        this.configurationHolder = runner.configurationHolder;
    }

    public static class BatchRunner {
        private ConfigurationHolder configurationHolder;
        private String scanPackage;
        private final BiFunction<BatchManager, String, BatchManager> fnScanPackage = (bm, sp) ->  {
            bm.scanPackage(sp);
            return bm;
        };

        public BatchRunner configurationHolder(ConfigurationHolder configurationHolder) {
            this.configurationHolder = configurationHolder;
            return this;
        }

        public BatchRunner scanPackage(String scanPackage) {
            this.scanPackage = scanPackage;
            return this;
        }

        public BatchManager build() {
            if(this.configurationHolder == null) {
                this.configurationHolder = new ConfigurationHolder();
            }
            if(StringUtils.isEmpty(this.scanPackage)) {
                this.scanPackage = "com.ranu";
            }
            return this.fnScanPackage.apply(new BatchManager(this), this.scanPackage);
        }
    }

    private void scanPackage(String basePackage) {
        LogArgument logArgument = new LogArgument(CommonRegEx.JAVA_PACKAGE_NAME, basePackage);
        LogUtils.logInfoSafely(log, "Scanning package: {}", logArgument);

        Collection<BatchConfiguration> batchCandidates = AnnotatedBatchScanner.scanPackage(basePackage);

        if (batchCandidates.isEmpty()) {
            LogUtils.logWarnSafely(log, "No batch found in base package \"{}\"", logArgument);
            return;
        }

        log.info("Instantiating batch(es)...");

        batchCandidates.stream()
                .map(this::findHighestPrecedenceConfiguration)
                .map(this::instantiateBatchQuietly)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addBatch);

        log.info("Instantiation complete. Now managing {} batches: {}", batchesByClass.size(), batchesByClass.values());
    }

    private BatchConfiguration findHighestPrecedenceConfiguration(BatchConfiguration batchConfiguration) {
        return configurationHolder.getHighestPrecedenceConfigurationByBatchClassName(batchConfiguration.getClassName())
                .orElse(batchConfiguration);
    }

    private Optional<AbstractBatch> instantiateBatchQuietly(BatchConfiguration batchConfiguration) {
        if (batchesByClass.containsKey(batchConfiguration.getClassName())) {
            log.debug("The batch {} was already instantiated", batchConfiguration.getClass());
            return Optional.empty();
        }
        return instantiateBatch(batchConfiguration);
    }

    private Optional<AbstractBatch> instantiateBatch(BatchConfiguration batchConfiguration) {
        log.debug("Instantiating batch {}...", batchConfiguration.getClassName());
        try {
            return Optional.of(BatchFactory.create(batchConfiguration, configurationHolder));
        }
        catch (Exception exception) {
            log.error("Error loading batch: {}", batchConfiguration.getClassName(), exception);
            return Optional.empty();
        }
    }

    private void addBatch(AbstractBatch batch) {
        BatchConfiguration configuration = batch.getConfiguration();
        String name = configuration.getName();
        String batchClass = configuration.getClassName();

        batchesByName.put(name, batch);
        batchesByClass.put(batchClass, configuration);
        log.debug("New batch added: {} (Object ID = {})", batchClass, ObjectUtils.getIdentityHexString(batch));
    }

    public AbstractBatch findBatchByName(String name) {
        if (StringUtils.isEmpty(name)) {
            throw ExceptionUtils.illegalArgument("The name cannot be null or empty");
        }
        if (batchesByName.containsKey(name)) {
            return batchesByName.get(name);
        }
        throw ExceptionUtils.illegalArgument(MSG_INVALID_BATCH, name);
    }

    public void removeBatch(String name) {
        AbstractBatch batch = findBatchByName(name);
        if (batch.isStarted() || batch.isRunning()) {
            throw ExceptionUtils.illegalState(MSG_BATCH_STARTED_PLEASE_STOP_FIRST, name);
        }
        batchesByName.remove(name);
    }

    public void resetBatch(String name) {
        AbstractBatch batch = findBatchByName(name);
        if (batch.isStarted() || batch.isRunning()) {
            throw ExceptionUtils.illegalState(MSG_BATCH_STARTED_PLEASE_STOP_FIRST, name);
        }

        log.info("Resetting batch: {}", batch.getConfiguration().getClassName());

        String batchClass = batch.getConfiguration().getClassName();
        BatchConfiguration batchConfig = batchesByClass.get(batchClass);
        AbstractBatch newBatch = BatchFactory.create(batchConfig, configurationHolder);

        addBatch(newBatch);
    }

    public void startBatch(String name) {
        startBatch(findBatchByName(name));
    }

    public void runNow(String name) {
        findBatchByName(name).run(true);
    }

    public void stopBatch(String name) {
        findBatchByName(name).stop();
    }

    public Collection<AbstractBatch> getBatches() {
        return batchesByName.values();
    }

    public boolean isBatchRunning(String name) {
        return findBatchByName(name).isRunning();
    }

    public boolean isBatchStarted(String name) {
        return findBatchByName(name).isStarted();
    }

    public String getBatchStatusJson(String name) {
        return findBatchByName(name).getStatusJson();
    }

    public void startAllBatches() {
        log.info("Starting batches...");
        getBatches().forEach(this::startBatch);
        log.info("All batches started successfully...");
    }

    private void startBatch(AbstractBatch batch) {
        batch.start();
    }
}
