package com.wideplay.warp.db4o;

import com.db4o.ObjectContainer;
import com.wideplay.warp.persist.WorkManager;

/**
 * 
 * @author Jeffrey Chung (lwbruce@gmail.com)
 */
class Db4oWorkManager implements WorkManager {

	public void beginWork() {
		ObjectServerHolder.getCurrentObjectContainer();
	}

	public void endWork() {
		ObjectServerHolder.closeCurrentObjectContainer();
	}
}
