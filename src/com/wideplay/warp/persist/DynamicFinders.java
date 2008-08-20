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

import com.google.inject.Binder;
import com.google.inject.matcher.Matchers;
import com.wideplay.warp.persist.dao.Finder;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * Utilities for configuring Dynamic Finders.
 * @author Robbie Vanbrabant
 */
public class DynamicFinders {
    private DynamicFinders() {}

    // TODO This doesn't work properly with multiple persistence modules.
    public static void bindInterceptor(Binder binder, MethodInterceptor finderInterceptor) {
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(Finder.class), finderInterceptor);
    }
}
