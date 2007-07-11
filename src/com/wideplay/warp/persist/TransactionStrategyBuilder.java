package com.wideplay.warp.persist;

import com.google.inject.matcher.Matcher;

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
     *  Note that JTA requires configuring Hibernate to use the JEE container's transaction implementation.
     *  See warp-persist website for details.
     * @return Returns the next step in the builder chain
     */
    TransactionStrategyBuilder transactedWith(TransactionStrategy transactionStrategy);

    /**
     *
     * @param classMatcher A Matcher on classes to watch for @Transactional methods
     * @return Returns the next step in the builder chain
     */
    PersistenceModuleBuilder forAll(Matcher<? super Class<?>> classMatcher);

    /**
     *
     * @param daoInterface An interface with some methods (typically all) that are
     *  annotated with @Finder(..). Warp-persist will automagically create method bodies
     *  and provide an instance when you inject this interface anywhere in your setup.
     *  Note that you are *not* allowed to provide your own implementation/binding for such
     *  interfaces.
     *
     * @return Returns the next step in the builder chain
     */
    TransactionStrategyBuilder addAccessor(Class<?> daoInterface);
}
