package com.wideplay.warp.db4o;

import com.db4o.ObjectContainer;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * 
 * @author Jeffrey Chung (lwbruce@gmail.com)
 */
class ObjectContainerProvider implements Provider<ObjectContainer> {

	private final ObjectServerHolder objectServerHolder;
	
	@Inject
	public ObjectContainerProvider(ObjectServerHolder objectServerHolder) {
		this.objectServerHolder = objectServerHolder;
	}
	 
	public ObjectContainer get() {
		return this.objectServerHolder.getObjectContainer();
	}
}
