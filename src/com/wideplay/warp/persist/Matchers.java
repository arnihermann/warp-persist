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
package com.wideplay.warp.persist;

import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import static com.google.inject.matcher.Matchers.annotatedWith;
import com.wideplay.warp.persist.dao.Finder;

import java.lang.reflect.AnnotatedElement;

/**
 * Custom matchers for use with Warp Persist.
 * @author Robbie Vanbrabant
 */
public class Matchers {
    private Matchers() {}

    // TODO These matchers could log warnings if you omit unit=... in multimodules mode

    // Finders matcher is package private because it's useless to external users
    // without user-configurable matchers. We should consider adding that functionality
    // though.
    
    /**
     * Matches on all {@code @}{@link com.wideplay.warp.persist.dao.Finder} annotations
     * that have the specified unit.
     * <pre>
     * {@code finderWithUnit(Sales.class) => @Finder(unit=Sales.class)}
     * </pre>
     * @param annotation the unit annotation
     * @return a matcher that matches on {@code @Finder(unit=annotation)}
     */
    static Matcher<AnnotatedElement> finderWithUnit(final Class<?> annotation) {
        return new AbstractMatcher<AnnotatedElement>() {
            public boolean matches(AnnotatedElement annotatedElement) {
                return annotatedWith(Finder.class).matches(annotatedElement) &&
                       annotatedElement.getAnnotation(Finder.class).unit() == annotation;
            }
        };
    }

    /**
     * Matches on all {@code @}{@link com.wideplay.warp.persist.Transactional} annotations
     * that have the specified unit.
     * <pre>
     * {@code transactionalWithUnit(Sales.class) => @Transactional(unit=Sales.class)}
     * </pre>
     * @param annotation the unit annotation
     * @return a matcher that matches on {@code @Transactional(unit=annotation)}
     */
    public static Matcher<AnnotatedElement> transactionalWithUnit(final Class<?> annotation) {
        return new AbstractMatcher<AnnotatedElement>() {
            public boolean matches(AnnotatedElement annotatedElement) {
                return annotatedWith(Transactional.class).matches(annotatedElement) &&
                       annotatedElement.getAnnotation(Transactional.class).unit() == annotation;
            }
        };
    }
}
