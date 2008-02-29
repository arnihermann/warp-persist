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
     * @param transactionStrategy One of the enum {@code TransactionStrategy}.
     *  See warp-persist website for details. By default {@code TransactionStrategy.LOCAL} is set.
     * 
     * @return Returns the next step in the builder chain
     */
    TransactionStrategyBuilder transactedWith(TransactionStrategy transactionStrategy);

    /**
     * This is a convenience method, and defaults to using the method matcher:
     * {@code Matchers.annotatedWith(Transactional.class)}.
     *
     * @param classMatcher A {@code Matcher} on classes to watch for transactional methods
     * @return Returns the next step in the builder chain
     */
    PersistenceModuleBuilder forAll(Matcher<? super Class<?>> classMatcher);

    /**
     *
     * Note that if you do not have an {@code @Transactional} annotation present on transactional methods, and
     * use a {@code Matcher.any()} for methods, warp-persist
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
     *  annotated with {@code @Finder}. Warp-persist will automagically create method bodies
     *  and provide an instance when you inject this interface anywhere in your setup.
     *  Note that you are *not* allowed to provide your own implementation/binding for such
     *  interfaces. Binding errors will result if you try.
     *
     *  Also accepts abstract classes, with one or more abstract methods marked {@code @Finder}.
     *
     * @return Returns the next step in the builder chain
     */
    TransactionStrategyBuilder addAccessor(Class<?> daoInterface);
}
