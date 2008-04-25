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

package com.wideplay.warp.db4o;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.google.inject.BindingAnnotation;

/**
 *
 * <p>
 * Use this annotation as a binding point for Db4o configuration. And its constants (HOST, PORT, etc.)
 * to bind specific values for those items when configuring for an {@code ObjectServer}. Example:
 * </p>
 *
 * <code>
 *
 *   bindConstant().annotatedWith(Db4Objects.class).to("TestDatabase.data");
 *
 *   bindConstant().annotatedWith(Names.named(Db4Objects.HOST)).to("localhost");
 *   bindConstant().annotatedWith(Names.named(Db4Objects.PORT)).to(4321);
 *   bindConstant().annotatedWith(Names.named(Db4Objects.USER)).to("autobot");
 *   bindConstant().annotatedWith(Names.named(Db4Objects.PASSWORD)).to("morethanmeetstheeye");
 *
 *
 *
 *   Configuration config = Db4o.newConfiguration();
 *   bind(Configuration.class).toInstance(config);
 * </code>
 *
 * <p>
 * You may also use the simple alternative (single file object server), for local file dbs:
 * </p>
 *
 * <code>
 *   bindConstant().annotatedWith(Db4Objects.class).to("TestDatabase.data");
 * </code>
 * 
 * @author Jeffrey Chung (jeffreymchung@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface Db4Objects {
	String HOST = "HOST";
	String PORT = "PORT";
	String USER = "USER";
	String PASSWORD = "PASSWORD";
}
