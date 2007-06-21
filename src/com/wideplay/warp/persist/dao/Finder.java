package com.wideplay.warp.persist.dao;

import java.util.Collection;
import java.util.ArrayList;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created with IntelliJ IDEA.
 * On: 3/06/2007
 *
 * @author Dhanji R. Prasanna
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Finder {
    String namedQuery() default "";
    String query() default "";

    Class<? extends Collection> returnAs() default ArrayList.class;
}
