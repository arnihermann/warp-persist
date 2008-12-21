/**
 * Copyright (C) 2008 Robbie Vanbrabant.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wideplay.warp.jpa;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.wideplay.warp.persist.PersistenceServiceSessionFilter;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Struts 2 equivalent for {@link com.wideplay.warp.jpa.SessionPerRequestFilter}.
 * Needed because Guice's current (1.0.1) Struts 2 plugin creates the Guice Injector
 * internally, which means that that injector instance is not available to other filters
 * before the Struts filter executes. Also see
 * <a href="http://groups.google.com/group/warp-core/browse_thread/thread/738a8ce3c7275602/">here</a>.
 * <p>
 * Do <em>not</em> use this interceptor in combination with the
 * {@link com.wideplay.warp.jpa.SessionPerRequestFilter}.
 * <p>
 * This Interceptor starts the {@link com.wideplay.warp.persist.PersistenceService} at creation time.
 * <p>
 * It is vital that only one instance of this interceptor exists in an application.
 * Currently Guice can't scope Struts 2 interceptors, so you have to rely on Struts'
 * behaviour in this regard. Struts 2 interceptors are not true singletons; one instance
 * exists per {@code <interceptor-ref>} in the XML. To make sure only one instance exists,
 * create an {@code <interceptor-stack>} with this single interceptor, and then use the resulting
 * stack in your application. Do <em>not</em> use the interceptor directly.
 * <p>
 * Example configuration:
 * <pre>{@code
 * <interceptors>
 *     <interceptor name="sessionPerRequestInterceptor"
 *                  class="com.wideplay.warp.jpa.SessionPerRequestInterceptor"/>
 *     <!-- Stack with single interceptor because we only want one instance -->
 *     <!-- Interceptors = one instance per interceptor-ref -->
 *     <interceptor-stack name="spriStack">
 *        <interceptor-ref name="sessionPerRequestInterceptor" />
 *     </interceptor-stack>
 * </interceptors>
 * }</pre>
 * Example usage:
 * <pre>{@code
 * <interceptor-stack name="securedStack">
 *     <interceptor-ref name="spriStack" />
 *     <interceptor-ref name="authenticationInterceptor" />
 *     <interceptor-ref name="defaultStack" />
 * </interceptor-stack>
 * }</pre>
 * <p>
 * It is likely that this class will be obsolete when Guice 2.0 is released.
 * The hierarchical injectors feature would enable a redesign of Guice's current Struts 2 plugin;
 * you would be able to provide one part of the injector ahead of time and then let the plugin
 * merge your injector with its internal one.
 * <p>
 * This code originated from the book
 * <a href="http://tinyurl.com/578n9s">Google Guice (Apress, 2008, ISBN 978-1590599976)</a>.
 *
 * @see com.wideplay.warp.jpa.SessionPerRequestFilter
 * @author Robbie Vanbrabant
 */
@Immutable
@ThreadSafe
public class SessionPerRequestInterceptor extends PersistenceServiceSessionFilter implements Interceptor {
    /**
     * Makes sure an {@link javax.persistence.EntityManager} instance is available while
     * the current request is being processed.
     * @see com.opensymphony.xwork2.interceptor.Interceptor#intercept(com.opensymphony.xwork2.ActionInvocation)
     */
    public String intercept(final ActionInvocation ai) throws Exception {
        // Ugly hack to reuse code. We should probably pull some code out of SessionFilter
        // into a class that we can reuse.
        final String[] result = new String[1];
        final Exception[] exception = new Exception[1];
        super.doFilter(null, null, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                try {
                    result[0] = ai.invoke();
                } catch (Exception e) {
                    exception[0] = e;
                }
            }
        });
        if (exception[0] != null)
            throw exception[0];
        return result[0];
    }

    /**
     * Does nothing.
     * @see com.opensymphony.xwork2.interceptor.Interceptor#init()
     */
    public void init() {
        try {
            super.init(null);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tries to close the {@link javax.persistence.EntityManagerFactory}.
     * @see com.opensymphony.xwork2.interceptor.Interceptor#destroy()
     */
    public void destroy() {
        super.destroy();
    }
}
