package com.wideplay.warp.persist;

import com.google.inject.cglib.proxy.Callback;
import com.google.inject.cglib.proxy.MethodProxy;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;

/**
 * Created with IntelliJ IDEA.
 * User: dhanji
 * Date: Oct 9, 2007
 * Time: 2:00:03 PM
 *
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
class AopAllianceCglibAdapter implements com.google.inject.cglib.proxy.MethodInterceptor {
    private final MethodInterceptor finderInterceptor;

    public AopAllianceCglibAdapter(MethodInterceptor finderInterceptor) {
        this.finderInterceptor = finderInterceptor;
    }


    public Object intercept(final Object object, final Method method, final Object[] args,
                            final MethodProxy methodProxy) throws Throwable {
        return finderInterceptor.invoke(new MethodInvocation() {

            public Method getMethod() {
                return method;
            }

            public Object[] getArguments() {
                return args;
            }

            public Object proceed() throws Throwable {
                return methodProxy.invoke(object, args);
            }

            public Object getThis() {
                return object;
            }

            public AccessibleObject getStaticPart() {
                return method;
            }
        });
    }
}
