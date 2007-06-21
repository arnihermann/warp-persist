package com.wideplay.warp.persist;

import com.google.inject.cglib.proxy.InvocationHandler;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 4/06/2007
 * Time: 14:40:15
 * <p/>
 *
 * This is a simple adaptor to convert a JDK dynamic proxy invocation into an aopalliance invocation.
 *
 * @author dprasanna
 * @since 1.0
 */
class AopAllianceAdapter implements InvocationHandler {
    private final MethodInterceptor interceptor;

    public AopAllianceAdapter(MethodInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public Object invoke(final Object object, final Method method, final Object[] objects) throws Throwable {
        return interceptor.invoke(new MethodInvocation() {

            public Method getMethod() {
                return method;
            }

            public Object[] getArguments() {
                return objects;
            }

            public Object proceed() throws Throwable {
                return method.invoke(object,  objects);
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
