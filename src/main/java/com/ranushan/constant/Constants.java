package com.ranushan.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Grouping all constants field
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    /**
     * Set history size for batch event
     */
    public static final int MAX_BATCH_HISTORY_SIZE = 1440;
}
