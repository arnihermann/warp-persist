package com.wideplay.warp.persist;

/**
 * WorkManager for internal code reuse.
 * This is not part of the public API; DO NOT USE.
 * 
 * @author Robbie Vanbrabant
 */
public interface InternalWorkManager<T> {
    T beginWork();
    T endWork();
}
