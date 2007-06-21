package com.wideplay.warp.persist;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 2:54:07 PM
 *
 * @author Dhanji R. Prasanna
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
    TransactionType type() default TransactionType.READ_WRITE;
    Class<? extends Exception> [] rollbackOn() default RuntimeException.class;
}
