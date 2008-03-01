package com.wideplay.warp.db4o;

import com.db4o.ObjectServer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import net.jcip.annotations.Immutable;

/**
 * 
 * @author Jeffrey Chung (lwbruce@gmail.com)
 */
@Immutable
class ObjectServerProvider implements Provider<ObjectServer> {

	private final ObjectServerHolder objectServerHolder;

	@Inject
	public ObjectServerProvider(ObjectServerHolder objectServerHolder) {
		this.objectServerHolder = objectServerHolder;
	}
	
	public ObjectServer get() {
		return this.objectServerHolder.getObjectServer();
	}
}
