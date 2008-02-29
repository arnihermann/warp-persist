package com.wideplay.warp.jpa;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created with IntelliJ IDEA.
 * On: 3/06/2007
 *
 * <p>
 *  A guice binding annotation you should use to tell warp-persist the name of
 * the JPA persistence unit you wish to use. Bind a string with the name to this
 * annotation. For instance, if the name of your persistence unit is "myDb" you would
 * add the following in one of your guice modules:
 * </p>
 * <code>
 * bindConstant().annotatedWith(JpaUnit.class).to("myDb");
 * </code>
 *
 * <p>
 *  You <b>must</b> bind a string to this annotation if using JPA. And it must match
 * a jpa unit named in your JPA persistence.xml.
 * </p>
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface JpaUnit {
}
