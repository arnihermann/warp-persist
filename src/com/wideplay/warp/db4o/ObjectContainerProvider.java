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

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectServer;
import com.google.inject.Provider;
import com.wideplay.warp.persist.ManagedContext;

/**
 * @author Jeffrey Chung (jeffreymchung@gmail.com)
 * @author Robbie Vanbrabant
 */
class ObjectContainerProvider implements Provider<ObjectContainer> {
	private final ObjectServerProvider objectServer;
	
	public ObjectContainerProvider(ObjectServerProvider objectServer) {
		this.objectServer = objectServer;
	}
	
	public ObjectContainer get() {
		ObjectServer objectServer = this.objectServer.get();
        Db4oSettings settings = this.objectServer.getSettings();
        ObjectContainer objectContainer;
        // TODO this is a shameless copy paste from the WorkManager
        if (!ManagedContext.hasBind(ObjectContainer.class, objectServer)) {
            //open local server client
            if (settings.isLocal()) {
                objectContainer = Db4o.openClient(objectServer.ext().configure(),
                        settings.getHost(),
                        settings.getPort(),
                        settings.getUser(),
                        settings.getPassword());

            //open remote client
            } else if (settings.isRemote()) {
                objectContainer = Db4o.openClient(settings.getConfiguration(),
                        settings.getHost(),
                        settings.getPort(),
                        settings.getUser(),
                        settings.getPassword());

            //open file based client
            } else {
                objectContainer = objectServer.openClient();
            }
            ManagedContext.bind(ObjectContainer.class, objectServer, objectContainer);
        } else {
            objectContainer = ManagedContext.getBind(ObjectContainer.class, objectServer);
        }
        if (!objectContainer.ext().isClosed()) {
            return objectContainer;
        } else {
            // this one has been closed, try again
            ManagedContext.unbind(ObjectContainer.class, objectServer);
            return get();
        }
    }
}
