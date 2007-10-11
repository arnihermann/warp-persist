package com.wideplay.warp.persist;

import java.lang.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 2:54:07 PM
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
    TransactionType type() default TransactionType.READ_WRITE;
    Class<? extends Exception> [] rollbackOn() default RuntimeException.class;
    Class<? extends Exception> [] exceptOn() default {};
}
