package com.ranushan.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * An object that parses and stores global configuration data.
 *
 */
@Getter
@RequiredArgsConstructor
public class GlobalConfiguration {
    /**
     * the maxBatchHistorySize for storing statistics
     */
    private final int maxBatchHistorySize;

    @Override
    public String toString() {
        return """
                {
                    "maxBatchHistorySize": %d
                }
                """.formatted(maxBatchHistorySize);
    }
}
