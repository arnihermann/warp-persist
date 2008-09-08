package com.wideplay.warp.persist;

import java.lang.annotation.Annotation;

/**
 * Formalizes naming conventions for {@link PersistenceStrategy} builders.
 * @author Robbie Vanbrabant
 */
public interface PersistenceStrategyBuilder<T> extends Builder<T> {
    /**
     * Configure this strategy to build modules bound to the specified Guice
     * Binding Annotation.
     * 
     * @param annotation a valid Guice Binding Annotation to which all persistence
     *        artifacts will be bound, including interceptors
     * @return this
     */
    PersistenceStrategyBuilder<T> annotatedWith(Class<? extends Annotation> annotation);
}
