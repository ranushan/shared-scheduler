package com.ranushan.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MethodUtils {

    public static List<Method> getMethodsListWithAnnotation(final Class<?> cls,
                                                            final Class<? extends Annotation> annotationCls) {
        return Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> m.getAnnotation(annotationCls) != null)
                .toList();
    }
}
