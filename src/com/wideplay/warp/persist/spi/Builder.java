package com.wideplay.warp.persist.spi;

/**
 * Formalizes the builder pattern.
 * @author Robbie Vanbrabant
 */
public interface Builder<T> {
    /**
     * Builds an instance of {@code T}.
     * @return T
     */
    T build();
}
