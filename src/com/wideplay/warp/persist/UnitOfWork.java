package com.wideplay.warp.persist;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 31/05/2007
 * Time: 11:55:04
 * <p>
 * Enumerates all the supported units-of-work (i.e. atomic lifespan of a persistence session).
 * </p>
 *
 * @author dprasanna
 * @since 1.0
 */
public enum UnitOfWork {
    /**
     * Logical unit of work (Session, ObjectContainer or JPA EntityManager) that spans an HTTP request.
     */
    REQUEST,


    /**
     * Logical unit of work (Session, ObjectContainer or JPA EntityManager) that spans a transaction demarcated
     *  with {@code @Transactional}.
     */
    TRANSACTION,


    /**
     * Unsupported in this version. Do not use. 
     */
    CONVERSATION,
}
