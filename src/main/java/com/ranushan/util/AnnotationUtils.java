package com.ranushan.util;

import com.ranushan.exception.BatchConfigurationException;
import com.ranushan.exception.InvalidClassException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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
        try {
            return getClasses(basePackage)
                    .stream()
                    .filter(c -> c.isAnnotationPresent(annotationClass))
                    .map(Class::getName)
                    .collect(Collectors.toSet());
        } catch (ClassNotFoundException | IOException | URISyntaxException exception) {
            throw new BatchConfigurationException(exception);
        }
    }

    /**
     * Scans all classes accessible from the context class loader which belong
     * to the given package and subpackages.
     *
     * @param packageName
     *            The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static List<Class> getClasses(String packageName) throws ClassNotFoundException, IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            URI uri = new URI(resource.toString());
            dirs.add(new File(uri.getPath()));
        }
        List<Class> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and
     * subdirs.
     *
     * @param directory
     *            The base directory
     * @param packageName
     *            The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
