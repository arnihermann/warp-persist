package com.wideplay.warp.hibernate;

import com.google.inject.name.Named;
import com.wideplay.warp.persist.dao.Finder;
import com.wideplay.warp.persist.dao.FirstResult;
import com.wideplay.warp.persist.dao.MaxResults;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Query;
import org.hibernate.Session;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 4/06/2007
 * Time: 14:54:52
 * <p/>
 *
 * @author dprasanna
 * @since 1.0
 */
class HibernateFinderInterceptor implements MethodInterceptor {
    private volatile Map<Method, FinderDescriptor> finderCache = new HashMap<Method, FinderDescriptor>();

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Session session = SessionFactoryHolder.getCurrentSessionFactory().getCurrentSession();

        //obtain a cached finder descriptor (or create a new one)
        FinderDescriptor finderDescriptor = getFinderDescriptor(methodInvocation);

        Object result = null;

        //execute as query (named params or otherwise)
        Query hibernateQuery = finderDescriptor.createQuery(session);
        if (finderDescriptor.isBindAsRawParameters)
            bindQueryRawParameters(hibernateQuery, finderDescriptor, methodInvocation.getArguments());
        else
            bindQueryNamedParameters(hibernateQuery, finderDescriptor, methodInvocation.getArguments());


        //depending upon return type, decorate or return the result as is
        if (ReturnType.PLAIN.equals(finderDescriptor.returnType)) {
            result = hibernateQuery.uniqueResult();
        } else if (ReturnType.COLLECTION.equals(finderDescriptor.returnType)) {
            result = getAsCollection(finderDescriptor, hibernateQuery.list());
        } else if (ReturnType.ARRAY.equals(finderDescriptor.returnType)) {
            result = hibernateQuery.list().toArray();
        }

        return result;
    }

    private Object getAsCollection(FinderDescriptor finderDescriptor, List results) {
        Collection<?> collection;
        try {
            collection = (Collection) finderDescriptor.returnCollectionTypeConstructor.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Specified collection class of Finder's returnAs could not be instantated: "
                + finderDescriptor.returnCollectionType, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Specified collection class of Finder's returnAs could not be instantated (do not have access privileges): "
                + finderDescriptor.returnCollectionType, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Specified collection class of Finder's returnAs could not be instantated (it threw an exception): "
                + finderDescriptor.returnCollectionType, e);
        }

        collection.addAll(results);
        return collection;
    }

    private void bindQueryNamedParameters(Query hibernateQuery, FinderDescriptor descriptor, Object[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            Object argument = arguments[i];
            Object annotation = descriptor.parameterAnnotations[i];

            if (null == annotation)
                continue;   //skip param as it's not bindable
            else if (annotation instanceof Named) {
                Named named = (Named)annotation;
                hibernateQuery.setParameter(named.value(), argument);
            } else if (annotation instanceof FirstResult)
                hibernateQuery.setFirstResult((Integer)argument);
            else if (annotation instanceof MaxResults)
                hibernateQuery.setMaxResults((Integer)argument);
        }
    }

    private void bindQueryRawParameters(Query hibernateQuery, FinderDescriptor descriptor, Object[] arguments) {
        for (int i = 0, index = 0; i < arguments.length; i++) {
            Object argument = arguments[i];
            Object annotation = descriptor.parameterAnnotations[i];

            if (null == annotation) {
                //bind it as a raw param (0-based index, yes I know its different from JDBC, blargh)
                hibernateQuery.setParameter(index, argument);
                index++;
            } else if (annotation instanceof FirstResult)
                hibernateQuery.setFirstResult((Integer)argument);
            else if (annotation instanceof MaxResults)
                hibernateQuery.setMaxResults((Integer)argument);
        }
    }

    private FinderDescriptor getFinderDescriptor(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        FinderDescriptor finderDescriptor = finderCache.get(method);
        if (null != finderDescriptor)
            return finderDescriptor;

        //otherwise reflect and cache finder info...
        finderDescriptor = new FinderDescriptor();

        //determine return type
        finderDescriptor.returnClass = invocation.getMethod().getReturnType();
        finderDescriptor.returnType = determineReturnType(finderDescriptor.returnClass);

        //determine finder query characteristics
        Finder finder = invocation.getMethod().getAnnotation(Finder.class);
        String query = finder.query();
        if (!"".equals(query.trim()))
            finderDescriptor.setQuery(query);
        else
            finderDescriptor.setNamedQuery(finder.namedQuery());

        //determine parameter annotations
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] discoveredAnnotations = new Object[parameterAnnotations.length];
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            //each annotation per param
            for (Annotation annotation : annotations) {
                //discover the named, first or max annotations then break out
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (Named.class.equals(annotationType)) {
                    discoveredAnnotations[i] = annotation;
                    finderDescriptor.isBindAsRawParameters = false;
                    break;
                } else if (FirstResult.class.equals(annotationType)) {
                    discoveredAnnotations[i] = annotation;
                    break;
                } else if (MaxResults.class.equals(annotationType)) {
                    discoveredAnnotations[i] = annotation;
                    break;
                }   //leave as null for no binding
            }
        }

        //set the discovered set to our finder cache object
        finderDescriptor.parameterAnnotations = discoveredAnnotations;

        //discover the returned collection implementation if this finder returns a collection
        if (ReturnType.COLLECTION.equals(finderDescriptor.returnType)) {
            finderDescriptor.returnCollectionType = finder.returnAs();
            try {
                finderDescriptor.returnCollectionTypeConstructor = finderDescriptor.returnCollectionType.getConstructor();
                finderDescriptor.returnCollectionTypeConstructor.setAccessible(true);   //UGH!
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Finder's collection return type specified has no default constructor! returnAs: "
                        + finderDescriptor.returnCollectionType, e);
            }
        }

        //cache it
        cacheFinderDescriptor(method, finderDescriptor);

        return finderDescriptor;
    }

    //provides copy-on-write semantics
    private synchronized void cacheFinderDescriptor(Method method, FinderDescriptor finderDescriptor) {
        Map<Method, FinderDescriptor> copy = new HashMap<Method, FinderDescriptor>();
        copy.putAll(finderCache);
        copy.put(method, finderDescriptor);

        //stash copy
        finderCache = copy;
    }

    private ReturnType determineReturnType(Class<?> returnClass) {
        if (Collection.class.isAssignableFrom(returnClass)) {
            return ReturnType.COLLECTION;
        } else if (returnClass.isArray()) {
            return ReturnType.ARRAY;
        }

        return ReturnType.PLAIN;
    }

    /**
     * A wrapper data class that caches information about a finder method.
     */
    private static class FinderDescriptor {
        private boolean isKeyedQuery = false;
        boolean isBindAsRawParameters = true;   //should we treat the query as having ? instead of :named params
        ReturnType returnType;
        Class<?> returnClass;
        Class<? extends Collection> returnCollectionType;
        Constructor returnCollectionTypeConstructor;
        Object[] parameterAnnotations;  //contract is: null = no bind, @Named = param, @FirstResult/@MaxResults for paging

        private String query;
        private String name;

        void setQuery(String query) {
            this.query = query;
        }

        void setNamedQuery(String name) {
            this.name = name;
            isKeyedQuery = true;
        }

        public boolean isKeyedQuery() {
            return isKeyedQuery;
        }

        Query createQuery(Session session) {
            return isKeyedQuery ? session.getNamedQuery(name) : session.createQuery(query);
        }
    }

    private static enum ReturnType { PLAIN, COLLECTION, ARRAY }
}
