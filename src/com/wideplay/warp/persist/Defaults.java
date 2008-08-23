package com.wideplay.warp.persist;

import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

/**
 * Configuration default values.
 * @author Robbie Vanbrabant
 */
public class Defaults {
    private Defaults() {}

    public static UnitOfWork UNIT_OF_WORK = UnitOfWork.TRANSACTION;
    public static TransactionStrategy TX_STRATEGY = TransactionStrategy.LOCAL;
    public static Matcher<? super Class<?>> TX_CLASS_MATCHER = Matchers.any();
    public static Matcher<? super Method> TX_METHOD_MATCHER = Matchers.annotatedWith(Transactional.class);    

    /**
     * Default persistence unit annotation.
     * @author Robbie Vanbrabant
     * @see com.wideplay.warp.persist.dao.Finder
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultUnit {}
}
