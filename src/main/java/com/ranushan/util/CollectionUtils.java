package com.ranushan.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionUtils {

    public static <T> boolean isCollectionEmptyOrNull(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

}
