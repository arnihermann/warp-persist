package com.wideplay.warp.persist;

import com.google.inject.matcher.Matcher;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public interface TransactionStrategyBuilder extends PersistenceModuleBuilder {
    /**
     *
     * @param transactionStrategy One of the enum TransactionStrategy (typically LOCAL or JTA).
     *  Note that JTA requires configuring Hibernate/JPA to use the Java EE container's transaction implementation.
     *  See warp-persist website for details. By default LOCAL is set.
     * 
     * @return Returns the next step in the builder chain
     */
    TransactionStrategyBuilder transactedWith(TransactionStrategy transactionStrategy);

    /**
     * This is a convenience method, and defaults to using a method matcher of @Transactional.
     *
     * @param classMatcher A Matcher on classes to watch for @Transactional methods
     * @return Returns the next step in the builder chain
     */
    PersistenceModuleBuilder forAll(Matcher<? super Class<?>> classMatcher);

    /**
     *
     * Note that if you do not have an @Transactional annotation present on transactional methods, Warp
     *  will assume default rollbackOn and exceptOn clauses (as though @Transactional were present but
     *  with no attributes specified).
     *
     * @param classMatcher A Matcher on classes to watch for transactional methods
     * @param methodMatcher A Matcher on methods
     * @return Returns the next step in the builder chain
     */
    PersistenceModuleBuilder forAll(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher);

    /**
     *
     * @param daoInterface An interface with some methods (typically all) that are
     *  annotated with @Finder(..). Warp-persist will automagically create method bodies
     *  and provide an instance when you inject this interface anywhere in your setup.
     *  Note that you are *not* allowed to provide your own implementation/binding for such
     *  interfaces. Binding errors will result if you try.
     *
     * @return Returns the next step in the builder chain
     */
    TransactionStrategyBuilder addAccessor(Class<?> daoInterface);
}
