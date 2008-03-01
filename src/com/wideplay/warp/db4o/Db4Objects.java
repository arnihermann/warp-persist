package com.wideplay.warp.db4o;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.google.inject.BindingAnnotation;

/**
 *
 * <p>
 * Use this annotation as a binding point for Db4o configuration. And its constants (HOST, PORT, etc.)
 * to bind specific values for those items when configuring for an {@code ObjectServer}. 
 * </p>
 * 
 * @author Jeffrey Chung (lwbruce@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface Db4Objects {
	String HOST = "HOST";
	String PORT = "PORT";
	String USER = "USER";
	String PASSWORD = "PASSWORD";
}
