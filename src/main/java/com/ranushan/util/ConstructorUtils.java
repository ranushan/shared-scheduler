package com.ranushan.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstructorUtils {

    public static <T> T invokeConstructor(Class<T> cls, Object... args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return org.apache.commons.lang3.reflect.ConstructorUtils.invokeConstructor(cls, args);
    }
}
