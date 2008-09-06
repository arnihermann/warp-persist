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
package com.wideplay.warp.persist;

import com.wideplay.warp.util.Inferred;

import java.util.Map;

/**
 * <p>
 * Utility analogous to Hibernate's {@link org.hibernate.context.ManagedSessionContext},
 * but not limited to a single persistence engine.<br/> 
 * In {@link UnitOfWork#REQUEST}, this class is used by the
 * {@link com.wideplay.warp.persist.WorkManager}, in {@link UnitOfWork#TRANSACTION} by
 * the context artifact (like EntityManager, ObjectContainer, ...) provider and
 * local transaction interceptor.
 * </p>
 * <p>
 * This class implements the typesafe heterogeneous container pattern.
 * </p>
 * @author Robbie Vanbrabant
 */
public class ManagedContext {
    private ManagedContext() {}

    private static final ThreadLocal<Map<Class<?>, Map<Object,Object>>> globalContext =
            Inferred.threadLocal();

    public static <T> T bind(Class<T> type, Object key, T em) {
        return type.cast(getContext(type, Wish.EXISTING_OR_NEW).put(key, em));
    }

    public static <T> T unbind(Class<T> type, Object key) {
        Map<Object, Object> contextMap = getContext(type, Wish.EXISTING_OR_NULL);
        if (contextMap == null) return null;
        try {
            return type.cast(contextMap.remove(key));
        } finally {
            if (contextMap.isEmpty()) {
                Map<Class<?>, Map<Object,Object>> gctx = getGlobalContext(Wish.EXISTING_OR_NEW);
                gctx.remove(type);
                if (gctx.isEmpty()) {
                    globalContext.remove();
                }
            }
        }
    }

    public static <T> T getBind(Class<T> type, Object key) {
        Map<Object, Object> contextMap = getContext(type, Wish.EXISTING_OR_NULL);
        if (contextMap == null) return null;
        return type.cast(contextMap.get(key));
    }

    public static <T> boolean hasBind(Class<T> type, Object key) {
        Map<Object, Object> contextMap = getContext(type, Wish.EXISTING_OR_NULL);
        return contextMap != null && contextMap.get(key) != null;
    }

    private static Map<Class<?>, Map<Object,Object>> getGlobalContext(Wish wish) {
        Map<Class<?>, Map<Object,Object>> contextMap = globalContext.get();
        if (contextMap == null && Wish.EXISTING_OR_NEW == wish) {
            contextMap = Inferred.hashMap();
            globalContext.set(contextMap);
        }
        return contextMap;
    }

    private static Map<Object, Object> getContext(Class<?> key, Wish wish) {
        Map<Class<?>, Map<Object,Object>> gContext = getGlobalContext(Wish.EXISTING_OR_NEW);
        Map<Object,Object> contextMap = gContext.get(key);
        if (contextMap == null && Wish.EXISTING_OR_NEW == wish) {
            contextMap = Inferred.hashMap();
            gContext.put(key, contextMap);
        }
        return contextMap;
    }

    private enum Wish {
        EXISTING_OR_NEW, EXISTING_OR_NULL
    }
}