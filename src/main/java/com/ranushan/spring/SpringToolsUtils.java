package com.ranushan.spring;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpringToolsUtils {

    public static Set<String> findClassesWithAnnotation(Class<? extends Annotation> annotationClass, String basePackage) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));

        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
        return candidateComponents.stream().map(BeanDefinition::getBeanClassName).collect(Collectors.toSet());
    }
}
