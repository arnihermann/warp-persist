package com.wideplay.warp.persist.spi;

/**
 * @author Robbie Vanbrabant
 */
public interface Builder<T> {
    /**
     * Builds an instance of {@code T}.
     * @return T
     */
    T build();
}
