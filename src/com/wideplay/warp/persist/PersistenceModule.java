package com.wideplay.warp.persist;

import com.google.inject.AbstractModule;
import com.google.inject.cglib.proxy.Proxy;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.wideplay.warp.hibernate.HibernateBindingSupport;
import com.wideplay.warp.jpa.JpaBindingSupport;
import com.wideplay.warp.persist.dao.Finder;
import org.aopalliance.intercept.MethodInterceptor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
class PersistenceModule extends AbstractModule {

    private final PersistenceFlavor flavor;

    private UnitOfWork unitOfWork;
    private TransactionStrategy transactionStrategy;
    private Matcher<? super Class<?>> classMatcher;
    private Matcher<? super Method> methodMatcher;

    private final Set<Class<?>> accessors = new LinkedHashSet<Class<?>>();

    PersistenceModule(PersistenceFlavor flavor) {
        this.flavor = flavor;

        //set defaults (do *not* make these final!!)
        classMatcher = Matchers.any();
        methodMatcher = Matchers.annotatedWith(Transactional.class);
        transactionStrategy = TransactionStrategy.LOCAL;
        unitOfWork = UnitOfWork.TRANSACTION;
    }

    protected void configure() {
        MethodInterceptor txnInterceptor = null;
        MethodInterceptor finderInterceptor = null;
        switch (flavor) {
            case HIBERNATE:
                HibernateBindingSupport.addBindings(binder());
                txnInterceptor = HibernateBindingSupport.getInterceptor(transactionStrategy);
                HibernateBindingSupport.setUnitOfWork(unitOfWork);
                finderInterceptor = HibernateBindingSupport.getFinderInterceptor();
                break;
            case JPA:
                JpaBindingSupport.addBindings(binder());
                txnInterceptor = JpaBindingSupport.getInterceptor(transactionStrategy);
                JpaBindingSupport.setUnitOfWork(unitOfWork);
                finderInterceptor = JpaBindingSupport.getFinderInterceptor();
                break;
        }

        //bind the chosen txn interceptor
        bindInterceptor(classMatcher, methodMatcher,
                txnInterceptor);

        //bind dynamic finders
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Finder.class), finderInterceptor);

        //create & bind dynamic accessors
        bindDynamicAccessors(finderInterceptor);
    }
    
    @SuppressWarnings("unchecked")
    private void bindDynamicAccessors(MethodInterceptor finderInterceptor) {
        for (Class accessor : accessors) {

            if (accessor.isInterface()) {
                bind(accessor).toInstance(Proxy.newProxyInstance(accessor.getClassLoader(),
                        new Class<?>[] { accessor }, new AopAllianceAdapter(finderInterceptor)));
            } else
    
                //use cglib adapter to subclass the accessor (this lets us intercept both abstract classes as well as interfaces)
                bind(accessor).toInstance(com.google.inject.cglib.proxy.Enhancer.create(accessor,
                        new AopAllianceCglibAdapter(finderInterceptor)));
        }
    }

    public void setMethodMatcher(Matcher<? super Method> methodMatcher) {
        this.methodMatcher = methodMatcher;
    }

    static enum PersistenceFlavor { HIBERNATE, JPA }


    //builder config hooks  
    void addAccessor(Class<?> daoInterface) {
        accessors.add(daoInterface);
    }

    void setUnitOfWork(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    void setTransactionStrategy(TransactionStrategy transactionStrategy) {
        this.transactionStrategy = transactionStrategy;
    }

    void setClassMatcher(Matcher<? super Class<?>> classMatcher) {
        this.classMatcher = classMatcher;
    }
}
