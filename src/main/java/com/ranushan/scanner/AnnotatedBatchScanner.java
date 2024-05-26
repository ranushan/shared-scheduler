package com.ranushan.scanner;

import com.ranushan.annotation.Batch;
import com.ranushan.configuration.BatchConfiguration;
import com.ranushan.exception.BatchConfigurationException;
import com.ranushan.util.AnnotationUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains methods for scanning package(s) to find annotated batches.
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotatedBatchScanner {

    /**
     * Scans the specified base package for batches.
     * <p>
     * <strong>NOTE: </strong> an empty {@code basePackage} string as a parameter may result
     * in a full class-path scan.
     *
     * @param basePackage the base package to search for annotated classes
     * @return a {@link Set} of {@link BatchConfiguration} objects from the objects found in
     *         the specified package, or an empty set; not null
     */
    public static Set<BatchConfiguration> scanPackage(String basePackage) {
        Set<String> classNames = findAnnotatedBatchClasses(basePackage);
        return classNames.stream()
                         .map(AnnotatedBatchScanner::toClass)
                         .map(BatchConfiguration::fromAnnotatedClass)
                         .collect(Collectors.toSet());
    }

    /**
     * Scans the specified base package for candidate batches.
     * <p>
     * <strong>NOTE: </strong> an empty {@code basePackage} string as a parameter may result
     * in a full class-path scan.
     *
     * @param basePackage the base package to search for annotated classes
     * @returns a list of candidate class names found in class path with the {@code @Batch}
     *          annotation, or an empty set; not null
     */
    protected static Set<String> findAnnotatedBatchClasses(String basePackage) {
        return AnnotationUtils.findClassesWithAnnotation(Batch.class, basePackage);
    }

    protected static Class<?> toClass(String className) {
        try {
            return Class.forName(className);
        }
        catch (ClassNotFoundException exception) {
            throw new BatchConfigurationException(exception);
        }
    }
}
