package com.wideplay.warp.util;

public interface LifecycleAdapter<T> {
    Lifecycle asLifecycle(T instance);
}
