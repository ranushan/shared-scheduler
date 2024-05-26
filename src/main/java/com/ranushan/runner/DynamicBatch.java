package com.ranushan.runner;

import com.ranushan.annotation.Run;
import com.ranushan.configuration.BatchConfiguration;
import com.ranushan.exception.InvalidClassException;
import com.ranushan.util.AnnotationUtils;
import com.ranushan.util.ConstructorUtils;
import com.ranushan.util.ReflectionUtils;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * An object that prepares and holds the required metadata and infrastructure for the
 * execution of a dynamic batch, which may vary depending on the associated
 * {@link BatchConfiguration}.
 *
 */
@Getter
public class DynamicBatch {
    private final Class<?> batchClass;
    private final Method batchTaskMethod;
    private final Object batchInstance;

    /**
     * Validates annotations and prepares all objects for execution.
     *
     * @param configuration the {@link BatchConfiguration} to be parsed
     * @throws InvalidClassException if any exception regarding a reflective operation (e.g.:
     *                               class or method not found) occurs
     */
    public DynamicBatch(BatchConfiguration configuration) {
        try {
            String batchClassName = configuration.getClassName();
            batchClass = Class.forName(batchClassName);
            batchTaskMethod = AnnotationUtils.getSinglePublicMethodWithAnnotation(Run.class, batchClass,
                    AnnotationUtils.MethodFilter.NO_PARAMETER);
            batchInstance = ConstructorUtils.invokeConstructor(batchClass);
        }
        catch (ReflectiveOperationException cause) {
            throw new InvalidClassException(cause);
        }
    }

    /**
     * Invokes the method annotated as {@code @Run} for the batch.
     */
    public void runBatchTask() {
        ReflectionUtils.invokeMethod(batchTaskMethod, batchInstance);
    }
}
