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
    TransactionStrategyBuilder transactedWith(TransactionStrategy transactionStrategy);

    PersistenceModuleBuilder forAll(Matcher<? super Class<?>> classMatcher);

    TransactionStrategyBuilder addAccessor(Class<?> daoInterface);
}
