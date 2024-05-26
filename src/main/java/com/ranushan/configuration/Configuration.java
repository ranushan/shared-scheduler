package com.ranushan.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

import static com.ranushan.constant.Constants.MAX_BATCH_HISTORY_SIZE;

/**
 * An object containing configuration data from a particular source.
 *
 */
@NoArgsConstructor
@Getter @Setter
public class Configuration {

    /**
     * Returns a list of {@link BatchConfiguration.BatchConfigurationBuilder} (candidates), as retrieved by the
     * configuration container.
     *
     * @see BatchConfiguration
     */
    private List<BatchConfiguration.BatchConfigurationBuilder> batches;

    /**
     * the statisticsConfiguration
     */
    private GlobalConfiguration globalConfiguration = new GlobalConfiguration(MAX_BATCH_HISTORY_SIZE);

    @Override
    public String toString() {
        return """
                    {
                        "batches": %s,
                        "globalConfiguration": %s
                    }
                    """.formatted(
                            batches.stream()
                                    .map(BatchConfiguration.BatchConfigurationBuilder::toString)
                                    .collect(Collectors.joining(", ")), globalConfiguration.toString());
    }
}
