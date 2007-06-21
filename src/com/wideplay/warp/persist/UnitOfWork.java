package com.wideplay.warp.persist;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 31/05/2007
 * Time: 11:55:04
 * <p/>
 * Enumerates all the supported units-of-work (i.e. atomic lifespan of a persistence session).
 *
 * @author dprasanna
 * @since 1.0
 */
public enum UnitOfWork {
    REQUEST, TRANSACTION, CONVERSATION,
}
