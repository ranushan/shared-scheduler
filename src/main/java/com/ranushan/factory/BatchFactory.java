package com.ranushan.factory;

import com.ranushan.configuration.BatchConfiguration;
import com.ranushan.configuration.ConfigurationHolder;
import com.ranushan.domain.BatchType;
import com.ranushan.exception.InvalidClassException;
import com.ranushan.runner.AbstractBatch;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * A factory that creates {@link AbstractBatch} objects based on given
 * {@link BatchConfiguration}.
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchFactory {

    /**
     * Creates a new batch instance from the given {@link BatchConfiguration}.
     *
     * @param configuration the {@link BatchConfiguration} to be processed, not null
     * @return an {@link AbstractBatch} from the given {@link BatchConfiguration}, not null
     *
     * @throws NullPointerException  if a null {@link BatchConfiguration} is received
     * @throws InvalidClassException if any exception regarding a reflective operation (e.g.:
     *                               required class or method not found) occurs
     */
    public static AbstractBatch create(BatchConfiguration configuration, ConfigurationHolder configurationHolder) {
        Objects.requireNonNull(configuration, "The BatchConfiguration must not be null");
        Objects.requireNonNull(configurationHolder, "The ConfigurationHolder must not be null");
        BatchType type = configuration.getType();
        return type.getFactoryFunction().apply(configuration, configurationHolder);
    }
}
