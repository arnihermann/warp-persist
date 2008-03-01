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
 * Created by IntelliJ IDEA.
 * User: Dhanji R. Prasanna (dhanji@gmail.com)
 * Date: 31/05/2007
 * Time: 11:58:15
 * <p>
 * Enumerates various txn strategies supported by warp-ext.
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
public enum TransactionStrategy {

    /**
     * Used to configure JTA as the transaction strategy for this app. UserTransactions will be looked up
     * via JNDI and joined or created as necessary. All transactions are assumed to be in isolation level
     * "REQUIRES". JTA support is currently <b>not available</b>
     */
    JTA,


    /**
     * Used to configure resource-local transactions. This is the default (and best) way to use warp-persist.
     *
     * Transaction isolation level emulates the "REQUIRES" semantic. See website documentation for details.
     * If using JTA you <b>must</b> remember to set the transaction type as {@code RESOURCE_LOCAL}
     */
    LOCAL,
}
