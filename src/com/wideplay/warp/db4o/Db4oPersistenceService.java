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
import com.db4o.ObjectServer;
import com.db4o.config.Configuration;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import static com.wideplay.warp.db4o.ObjectServerHolder.HostKind;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.util.Text;
import static com.wideplay.warp.util.Text.empty;
import net.jcip.annotations.ThreadSafe;

/**
 * 
 * @author Jeffrey Chung (jeffreymchung@gmail.com), Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe
class Db4oPersistenceService extends PersistenceService {

	private final ObjectServerHolder objectServerHolder;

    //temporary config placeholders (injected)
    private volatile String databaseFileName;
    private volatile String host;
	private volatile String user;
	private volatile String password;
	private volatile int port = 0;
	private volatile Configuration configuration;

	@Inject
	public Db4oPersistenceService(ObjectServerHolder objectServerHolder) {
		this.objectServerHolder = objectServerHolder;
	}

	public synchronized void start() {
        HostKind hostKind;
        if (databaseFileName == null) {
            if (!empty(host))
                hostKind = HostKind.REMOTE;
            else
                throw new IllegalStateException("Must specify either database file name: " +
                        "bindConstant().annotatedWith(Db4Objects); or a remote server host: bindConstant()" +
                        ".annotatedWith(Names.named(Db4Objects.HOST)).to(\"localhost\")");
        } else if (!empty(host)) {
            hostKind = HostKind.LOCAL;
        } else
            hostKind = HostKind.FILE;
        

        //validate configuration object
        if ( (!HostKind.FILE.equals(hostKind)) && null == configuration)
            throw new IllegalStateException("Must specify a Configuration when using " + hostKind + " server mode." +
                    " For starters, try: bind(Configuration.class).toInstance(Db4o.newConfiguration());");


        //pass on configuration TODO (ugh) need better design pattern here...
        objectServerHolder.set(user, password, host, port, configuration, hostKind);


        //use local (i.e. open our own) object server?
        if (HostKind.LOCAL.equals(hostKind)) {
            ObjectServer objectServer = Db4o.openServer(configuration, databaseFileName, port);

            //auth if credentials are available
            if (!empty(user))
                objectServer.grantAccess(user, password);

            objectServerHolder.setObjectServer(objectServer);


            //otherwise it's a simple local-file database
        } else if (HostKind.FILE.equals(hostKind)) {
            objectServerHolder.setObjectServer(Db4o.openServer(databaseFileName, port));
        }


	}

    public void shutdown() {
        // TODO (Robbie) Dhanji, is this enough? I'm not familiar with DB4O.
        objectServerHolder.getObjectServer().close();
    }


    //DO NOT Collapse these into a single setter (each is optional individually...)
    @Inject(optional = true)
    public void setDatabaseFileName(@Db4Objects String databaseFileName) {
        this.databaseFileName = databaseFileName;

        Text.nonEmpty(databaseFileName, "Db4o database file name was not set; please bindConstant()" +
                ".annotatedWith(Db4Objects.class) to a string containing the name of a Db4o database file.");
    }

    @Inject(optional = true)
	private void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
	}

	@Inject(optional = true)
	private void setHost(@Named(Db4Objects.HOST) String host) {
		this.host = host;

        Text.nonEmpty(host, "Please specify a valid host name.");
    }

	@Inject(optional = true)
	private void setPassword(@Named(Db4Objects.PASSWORD) String password) {
		this.password = password;
	}

	@Inject(optional = true)
	private void setPort(@Named(Db4Objects.PORT) int port) {
        if (port < 0 || port > 65535)
            throw new IllegalArgumentException("Port number was invalid (must be in range 0-65535). Was: "
                    + port);

        this.port = port;
	}

	@Inject(optional = true)
	private void setUser(@Named(Db4Objects.USER) String user) {
		this.user = user;
	}
	
	@Override
	public boolean equals(Object obj) {
        return  obj instanceof Db4oPersistenceService &&

                objectServerHolder.equals( ((Db4oPersistenceService) obj).objectServerHolder);
	}
	
	@Override
	public int hashCode() {
		return (objectServerHolder != null ? objectServerHolder.hashCode() : 0);
	}
}
