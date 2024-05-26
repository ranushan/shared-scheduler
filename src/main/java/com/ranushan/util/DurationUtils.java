package com.ranushan.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DurationUtils {
    public static Duration average(Collection<Duration> durations) {
        if (CollectionUtils.isCollectionEmptyOrNull(durations)) {
            return Duration.ZERO;
        }
        Duration sum = Duration.ZERO;
        int count = 0;
        for (Duration element : durations) {
            if (element != null) {
                sum = sum.plus(element);
                count++;
            }
        }
        return count == 0 ? Duration.ZERO : sum.dividedBy(count);
    }
}