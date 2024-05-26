package com.ranushan.configuration;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * An object that holds multiple configuration objects for the several configuration
 * sources.
 *
 */
@Getter
public class ConfigurationHolder {

    protected Map<String, BatchConfiguration> batchesByClassName;

    /**
     * Builds a {@link ConfigurationHolder}, loaded with configuration data mapped from
     * all the supported configuration sources.
     */
    public ConfigurationHolder() {
        batchesByClassName = Collections.emptyMap();
    }

    /**
     * Returns the highest-precedence {@link BatchConfiguration} object associated with a
     * given class name.
     *
     * @param className the batch class name to be searched
     * @return the highest-precedence available {@link BatchConfiguration} object for the
     *         specified class name, or {@link Optional#empty()} if no associated
     *         {@link BatchConfiguration} found
     */
    public Optional<BatchConfiguration> getHighestPrecedenceConfigurationByBatchClassName(String className) {
        return Optional.ofNullable(batchesByClassName.get(className));
    }
}
