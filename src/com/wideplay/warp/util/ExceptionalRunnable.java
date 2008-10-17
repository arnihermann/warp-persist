package com.wideplay.warp.util;

public interface ExceptionalRunnable<E extends Exception> {
    void run() throws E;
}
