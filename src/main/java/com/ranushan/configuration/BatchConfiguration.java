package com.ranushan.configuration;

import com.ranushan.annotation.Batch;
import com.ranushan.domain.BatchType;
import com.ranushan.exception.BatchConfigurationException;
import com.ranushan.util.ExceptionUtils;
import com.ranushan.util.StringUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * An object that parses and stores the configuration data for a particular batch.
 *
 */
@Getter
@RequiredArgsConstructor
@Builder
@EqualsAndHashCode
public class BatchConfiguration {
    private final String name;
    private final BatchType type;
    private final String className;
    private final String interval;
    private final boolean modulate;
    private final boolean enableStatistics;

    protected BatchConfiguration(BatchConfiguration.BatchConfigurationBuilder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.className = builder.className;
        this.interval = builder.interval;
        this.modulate = builder.modulate;
        this.enableStatistics = builder.enableStatistics;
    }

    /**
     * A {@link BatchConfiguration} builder.
     *
     */
    public static class BatchConfigurationBuilder {
        protected static final String MSG_TYPE_CANNOT_BE_NULL = "the batch type cannot be null";
        protected static final String MSG_CLASS_NAME_CANNOT_BE_NULL = "the class name cannot be null";

        public BatchConfiguration build() {
            Objects.requireNonNull(type, MSG_TYPE_CANNOT_BE_NULL);

            if (StringUtils.isEmpty(className)) {
                throw new BatchConfigurationException(MSG_CLASS_NAME_CANNOT_BE_NULL);
            }
            if (StringUtils.isEmpty(name)) {
                name = StringUtils.defaultIfEmpty(name, className);
            }
            if (StringUtils.isEmpty(interval)) {
                interval = type.getDefaultInterval();
            }
            return new BatchConfiguration(this);
        }

        @Override
        public String toString() {
            return """
                    {
                        "name": %s,
                        "className": %s,
                        "type": %s,
                        "interval": %s,
                        "modulate": %b,
                        "enableStatistics": %b
                    }
                    """.formatted(name, className, type.name(), interval, modulate, enableStatistics);
        }
    }

    /**
     * Parses the batch configuration by checking the {@link Batch} annotation in the
     * specified class.
     *
     * @param batchClass the class to be mapped
     * @return a {@link BatchConfiguration} mapped from the specified source class
     * @throws BatchConfigurationException if the annotation is not present in the class
     */
    public static BatchConfiguration fromAnnotatedClass(Class<?> batchClass) {
        Batch annotation = batchClass.getAnnotation(Batch.class);
        if (annotation == null) {
            throw ExceptionUtils.batchConfiguration("@Batch annotation is not present in class %s", batchClass);
        }

        String name = StringUtils.defaultIfEmpty(annotation.name(), batchClass.getCanonicalName());

        BatchType type = annotation.type();
        String className = batchClass.getCanonicalName();
        String interval = annotation.interval();
        boolean modulate = annotation.modulate();
        boolean enableStatistics = annotation.enableStatistics();

        return new BatchConfiguration.BatchConfigurationBuilder()
                .type(type)
                .name(name)
                .className(className)
                .interval(interval)
                .modulate(modulate)
                .enableStatistics(enableStatistics)
                .build();
    }

    @Override
    public String toString() {
        return """
                    {
                        "name": %s,
                        "className": %s,
                        "type": %s,
                        "interval": %s,
                        "modulate": %b,
                        "enableStatistics": %b
                    }
                    """.formatted(name, className, type.name(), interval, modulate, enableStatistics);
    }
}