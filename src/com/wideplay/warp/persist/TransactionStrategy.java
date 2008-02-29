package com.wideplay.warp.persist;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 31/05/2007
 * Time: 11:58:15
 * <p>
 * Enumerates various txn strategies supported by warp-ext.
 * </p>
 *
 * @author dprasanna
 * @since 1.0
 */
public enum TransactionStrategy {

    /**
     * Used to configure JTA as the transaction strategy for this app. UserTransactions will be looked up
     * via JNDI and joined or created as necessary. All transactions are assumed to be in isolation level
     * "REQUIRES"
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
