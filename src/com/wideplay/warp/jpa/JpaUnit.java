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

package com.wideplay.warp.jpa;

import com.google.inject.BindingAnnotation;
import com.wideplay.warp.persist.Defaults;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>
 *  A guice binding annotation you should use to tell warp-persist the name of
 * the JPA persistence unit you wish to use. Bind a string with the name to this
 * annotation. For instance, if the name of your persistence unit is "myDb" you would
 * add the following in one of your guice modules:
 * </p>
 * <code>
 * bindConstant().annotatedWith(JpaUnit.class).to("myDb");
 * </code>
 * If you are using multiple JPA configurations in the same project (multiple modules),
 * bind to an instance of the JpaUnit annotation instead, specifying the module-level
 * binding annotation used to create the persistence module:
 * <code>
 * bindConstant().annotatedWith(JpaUnitInstance.of(MyDB.class)).to("myDb");
 * </code>
 * <p>
 *  You <b>must</b> bind a string to this annotation if using JPA. And it must match
 * a jpa unit named in your JPA persistence.xml.
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface JpaUnit {
    Class<? extends Annotation> value() default Defaults.DefaultUnit.class;
}
