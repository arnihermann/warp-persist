package com.wideplay.warp.persist;

import java.lang.annotation.Annotation;

/**
 * Formalizes naming conventions for {@link PersistenceStrategy} builders.
 * @author Robbie Vanbrabant
 */
public interface PersistenceStrategyBuilder<T> extends Builder<T> {
    PersistenceStrategyBuilder<T> annotatedWith(Class<? extends Annotation> annotation);
    T build();
}
