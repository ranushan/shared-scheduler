package com.ranushan.util;

import com.ranushan.exception.InvalidClassException;
import com.ranushan.spring.SpringToolsUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationUtils {

    public enum MethodFilter {
        DEFAULT
                {
                    @Override
                    Method filter(Method method)
                    {
                        return method;
                    }
                },
        NO_PARAMETER
                {
                    @Override
                    Method filter(Method method)
                    {
                        Objects.requireNonNull(method, "The method must not be null");
                        int parameterCount = method.getParameterCount();
                        if (parameterCount != 0)
                        {
                            throw ExceptionUtils.invalidClass("The method \"%s\" has parameter(s).", method.getName());
                        }
                        return method;
                    }
                };
        abstract Method filter(Method method);
    }

    public static Method getSinglePublicMethodWithAnnotation(Class<? extends Annotation> annotationClass,
                                                             Class<?> sourceClass) {
        return getSinglePublicMethodWithAnnotation(annotationClass, sourceClass, MethodFilter.DEFAULT);
    }

    public static Method getSinglePublicMethodWithAnnotation(Class<? extends Annotation> annotationClass,
                                                             Class<?> sourceClass, MethodFilter methodFilter) {
        Objects.requireNonNull(annotationClass, "The annotation class must not be null");
        Objects.requireNonNull(sourceClass, "The source class must not be null");

        List<Method> batchTaskMethods = MethodUtils.getMethodsListWithAnnotation(sourceClass, annotationClass);

        if (batchTaskMethods.isEmpty()) {
            throw ExceptionUtils.invalidClass("No public method with the @%s annotation found in the class %s",
                    annotationClass.getSimpleName(), sourceClass.getName());
        }
        if (batchTaskMethods.size() > 1) {
            throw ExceptionUtils.invalidClass(
                    "%s methods with the @%s annotation found in the class %s. Only one is allowed.",
                    batchTaskMethods.size(), annotationClass.getSimpleName(), sourceClass.getName());
        }

        return applyFilter(methodFilter, batchTaskMethods.get(0), annotationClass);
    }

    private static Method applyFilter(MethodFilter methodFilter, Method candidateMethod,
                                      Class<? extends Annotation> annotationClass) {
        try {
            MethodFilter localMethodFilter = ObjectUtils.defaultIfNull(methodFilter, MethodFilter.DEFAULT);
            return localMethodFilter.filter(candidateMethod);
        }
        catch (InvalidClassException exception) {
            throw ExceptionUtils.invalidClass(exception, "The method contaning the @%s annotation is not valid.",
                    annotationClass.getSimpleName());
        }
    }

    public static Set<String> findClassesWithAnnotation(Class<? extends Annotation> annotationClass, String basePackage) {
        return SpringToolsUtils.findClassesWithAnnotation(annotationClass, basePackage);
    }
}
