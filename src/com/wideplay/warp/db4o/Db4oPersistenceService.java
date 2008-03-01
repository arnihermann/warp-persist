package com.wideplay.warp.db4o;

import com.db4o.Db4o;
import com.db4o.ObjectServer;
import com.db4o.config.Configuration;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.util.Text;
import static com.wideplay.warp.util.Text.isNotEmpty;
import net.jcip.annotations.ThreadSafe;

/**
 * 
 * @author Jeffrey Chung (lwbruce@gmail.com), Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe
class Db4oPersistenceService extends PersistenceService {

	private final ObjectServerHolder objectServerHolder;
	private final String databaseFileName;


    //temporary config placeholders (injected)
    private volatile String host;
	private volatile String user;
	private volatile String password;
	private volatile int port = 0;
	private volatile Configuration configuration;

	@Inject
	public Db4oPersistenceService(ObjectServerHolder objectServerHolder, @Db4Objects String databaseFileName) {
		this.objectServerHolder = objectServerHolder;
		this.databaseFileName = databaseFileName;

        Text.nonEmpty(databaseFileName, "Db4o database file name was not set; please bindConstant()" +
                ".annotatedWith(Db4Objects.class) to a string containing the name of a Db4o database file.");
	}

	public synchronized void start() {
		if (configuration != null) {
			ObjectServer objectServer = Db4o.openServer(configuration, databaseFileName, port);

            //use host or local?
            if (isNotEmpty(host) &&
					isNotEmpty(user) &&
					isNotEmpty(password)) {

                objectServerHolder.set(user, password, host, port);
				objectServer.grantAccess(user, password);
			}
			
			objectServerHolder.setObjectServer(objectServer);
		} else {
			objectServerHolder.setObjectServer(Db4o.openServer(databaseFileName, port));
		}
	}


    //DO NOT Collapse these into a single setter (each is optional individually...)
    @Inject(optional = true)
	private void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Inject(optional = true)
	private void setHost(@Named(Db4Objects.HOST) String host) {
		this.host = host;
	}

	@Inject(optional = true)
	private void setPassword(@Named(Db4Objects.PASSWORD) String password) {
		this.password = password;
	}

	@Inject(optional = true)
	private void setPort(@Named(Db4Objects.PORT) int port) {
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
