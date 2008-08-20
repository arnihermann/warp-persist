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
import com.db4o.Db4oIOException;
import com.db4o.ObjectContainer;
import com.db4o.ObjectServer;
import com.db4o.config.Configuration;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * 
 * @author Jeffrey Chung (jeffreymchung@gmail.com), Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe
class ObjectServerHolder {

    @GuardedBy("setObjectServer()")   //BUT reads are concurrent, so field must be volatile
    private volatile ObjectServer objectServer;

    //static singleton, =(
    private static volatile ObjectServerHolder singletonObjectServerHolder;

    //cached config for creating object containers
    private static volatile Db4oSettings settings = Db4oSettings.defaultSettings();

    //the current unit of work interface--
    private final ThreadLocal<ObjectContainer> objectContainer = new ThreadLocal<ObjectContainer>();


    public ObjectServerHolder() {
        singletonObjectServerHolder = this;
	}

	ObjectServer getObjectServer() {
		return objectServer;
	}

	synchronized void setObjectServer(ObjectServer objectServer) {
		if (this.objectServer != null) {
			throw new RuntimeException("Duplicate ObjectServer creation!  Only one ObjectServer is allowed per injector.");
		}

		this.objectServer = objectServer;
	}

	static ObjectServer getCurrentObjectServer() {
        final ObjectServer server = singletonObjectServerHolder.getObjectServer();

        if (null == server)
            throw new RuntimeException("No ObjectServer was found. Did you remember to call " +
                    "PersistenceService.start() *before* using the ObjectServer? In servlet environments, this is typically " +
                    "done in the init() lifecycle method of a servlet (or equivalent webapp initialization scheme)." +
                    " If you are connecting to a remote ObjectServer, then do not try to inject ObjectServer " +
                    "(use ObjectContainer instead).");

        return server;
	}

    //@ThreadLocal
    static void closeCurrentObjectContainer() throws Db4oIOException {
		ObjectContainer oc = singletonObjectServerHolder.objectContainer.get();

		if (oc != null) {
            try {
                if (!oc.ext().isClosed()) {
                    oc.close();
                }
            } finally {
                singletonObjectServerHolder.objectContainer.remove();
            }
        }
	}

    //@ThreadLocal
    static ObjectContainer checkCurrentObjectContainer() {
		return singletonObjectServerHolder.objectContainer.get();
	}

    //@ThreadLocal
    static ObjectContainer getCurrentObjectContainer() {
		ObjectContainer oc = singletonObjectServerHolder.objectContainer.get();

        //if the current container is dead, open a new one
        if (oc == null || oc.ext().isClosed()) {

            //open local server client
            if (settings.isLocal()) {
				oc = Db4o.openClient(getCurrentObjectServer().ext().configure(),
                        settings.host,
                        settings.port,
                        settings.user,
                        settings.password);

                //open remote client
            } else if (settings.isRemote()) {
                oc = Db4o.openClient(settings.configuration,
                        settings.host,
                        settings.port,
                        settings.user,
                        settings.password);

                //open file based client
            } else {
                oc = getCurrentObjectServer().openClient();
			}

            //ugh.  TODO refactor!
            singletonObjectServerHolder.objectContainer.set(oc);
		}

		return oc;
	}

    @Override
    public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || ! (o instanceof ObjectServerHolder)) return false;

		ObjectServerHolder that = (ObjectServerHolder) o;

		return (objectServer == null ? that.objectServer == null : objectServer.equals(that.objectServer));
	}

    @Override
    public int hashCode() {
		return (objectServer != null ? objectServer.hashCode() : 0);
	}

	public ObjectContainer getObjectContainer() {
		return getCurrentObjectContainer();
	}

	public void set(String user, String password, String host, int port, Configuration configuration, HostKind hostKind) {
        settings = new Db4oSettings(user, password, host, port, configuration, hostKind);
    }


    //an internal configuration wrapper class
    @Immutable
    private static class Db4oSettings {
        private final String user;
        private final String password;
        private final String host;
        private final int port;
        private final Configuration configuration;
        private final HostKind hostKind;


        private Db4oSettings(String user, String password, String host, int port,
                             Configuration configuration, HostKind hostKind) {

            this.user = user;
            this.password = password;
            this.host = host;
            this.port = port;
            this.configuration = configuration;

            this.hostKind = hostKind;
        }


        //default settings
        private Db4oSettings() {
            user = null;
            password = null;
            host = null;
            port = 0;
            configuration = null;

            hostKind = HostKind.FILE;
        }

        private static Db4oSettings defaultSettings() {
            return new Db4oSettings();
        }

        public boolean isLocal() {
            return HostKind.LOCAL.equals(hostKind);
        }

        public boolean isRemote() {
            return HostKind.REMOTE.equals(hostKind);
        }
    }

    static enum HostKind {
        REMOTE, LOCAL, FILE }
}
