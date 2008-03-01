/**
 * Copyright (C) 2008 Wideplay Interactive.
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

package com.wideplay.warp.persist.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * On: 3/06/2007
 *
 * <p>
 * Marks a method stub as a dynamic finder. The method is intercepted and replaced with
 * the specified HQL or JPAQL query. Provides result auto-boxing and automatic parameter binding.
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Finder {
    /**
     * Specify a named query's name here, typically using the {@code @NamedQuery} annotation.
     *
     * @return Returns the configured named query's name.
     */
    String namedQuery() default "";


    /**
     * Directly specify a query here, hql or jpaql.
     *
     * @return Returns the configured query string.
     */
    String query() default "";


    /**
     * Use this clause to specify a collection impl to autobox result lists into. The impl *must*
     * have a default no-arg constructor and be a subclass of {@code java.util.Collection}.
     *  
     * @return Returns the configured autoboxing collection class.
     */
    Class<? extends Collection> returnAs() default ArrayList.class;
}
