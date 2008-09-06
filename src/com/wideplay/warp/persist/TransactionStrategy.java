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
 * Defines a Transaction strategy, originally designed to choose
 * between LOCAL or JTA transactions. Only kept around for
 * legacy code; JPA was never officially supported and support
 * for it has now been removed. If you need JPA, consider creating
 * a custom {@link PersistenceStrategy} and transaction interceptor.
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 * @deprecated Don't specify a transaction strategy since you can only choose LOCAL.
 *             {@link PersistenceStrategy} implementations should roll their own
 *             mechanism for choosing between transaction strategies if they wish
 *             to make that choice available.
 */
@Deprecated
public enum TransactionStrategy {
    /**
     * Used to configure resource-local transactions. This is the only option and therefore also the default.
     *
     * Transaction isolation level emulates the "REQUIRES" semantic. See website documentation for details.
     * If using JTA you <b>must</b> remember to set the transaction type as {@code RESOURCE_LOCAL}
     */
    LOCAL
}
