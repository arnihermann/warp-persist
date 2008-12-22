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

/**
 * <p>
 * Enumerates options for an experimental feature: read/write or read/only transactions. Currently
 * *not* supported in JPA and experimental in Hibernate. The underlying mechanism actually alters
 * the session-flush state to manual and silently ignores the flush on commit. The dirty state of
 * the same session in subsequent transactions may be affected. Use with caution.
 * </p>
 *
 * <p>
 *  Prefer driver-level read only transactions if your database supports it (using JTA global
 * transactions).
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public enum TransactionType {   
    /**
     * Used to instruct warp-persist in the {@code @Transactional} annotation of methods/classes
     * that this transaction is READ-ONLY. This feature is not supported for JPA and is experimental
     * for Hibernate.
     */
    READ_ONLY,


    /**
     * Used to instruct warp-persist in the {@code @Transactional} annotation of methods/classes
     * that this transaction is READ-WRITE. It is redundant to use this enum explicitly, since all
     * transactions are read-write by default. 
     */
    READ_WRITE,
}
