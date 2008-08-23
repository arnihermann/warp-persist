package com.wideplay.warp.persist;

import com.google.inject.matcher.Matcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Robbie Vanbrabant
 */
public interface PersistenceConfiguration {
    UnitOfWork getUnitOfWork();

    TransactionStrategy getTransactionStrategy();

    Matcher<? super Method> getTransactionMethodMatcher();

    Matcher<? super Class<?>> getTransactionClassMatcher();

    Set<Class<?>> getAccessors();

    Class<? extends Annotation> getBindingAnnotationClass();

    boolean hasBindingAnnotation();

    String getAnnotationDebugStringOrNull();
}
