package com.wideplay.warp.persist;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 3:05:50 PM
 *
 * <p>
 * Enumerates options for an experimental feature: read/write or read/only transactions. Currently
 * *not* supported in JPA and experimental in Hibernate. The underlying mechanism actually alters
 * the session-flush state to manual and silently ignores the flush on commit. The dirty state of
 * the same session in subsequent transactions may be affected. Use with caution.
 * </p>
 *
 * <p>
 *   Prefer driver-level read only transactions if your database supports it (using JTA global
 * transactions).
 * </p>
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
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
