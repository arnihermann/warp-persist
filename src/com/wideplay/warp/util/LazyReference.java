/**
 * Copyright (C) 2008 Wideplay Interactive.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wideplay.warp.util;

import com.google.inject.Provider;
import net.jcip.annotations.ThreadSafe;

/**
 * Utility to lazily load an object reference.
 * It uses Double-Checked Locking under the covers
 * and thus can safely be accessed from multiple threads
 * concurrently.
 * 
 * @author Robbie Vanbrabant
 */
@ThreadSafe
public class LazyReference<T> {
    private final Object LOCK = new Object();
    // This field has to be volatile. Do not change!
    private volatile T instance;
    private final Provider<T> instanceProvider;

    private LazyReference(Provider<T> instanceProvider) {
        this.instanceProvider = instanceProvider;
    }

    /**
     * Get the existing T instance, or lazily initialize T using
     * {@link #instanceProvider}.
     * @return the T instance unique to this LazyReference
     */
    public T get() {
        // Double-Checked Locking as seen in
        // Effective Java, 2nd edition, page 283.
        T result = instance;
        if (result == null) {
            synchronized (LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = instanceProvider.get();
                }
            }
        }
        return result;
    }

    /**
     * Create a lazy reference with a provider of the
     * eventual instance.
     * @param instanceProvider a {@link com.google.inject.Provider}
     *                         that gives out the eventual instance,
     *                         usually an expensive operation
     * @return an uninitialized reference to T
     */
    public static <T> LazyReference<T> of(Provider<T> instanceProvider) {
        return new LazyReference<T>(instanceProvider);
    }
}
