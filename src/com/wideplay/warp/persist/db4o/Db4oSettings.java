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
package com.wideplay.warp.persist.db4o;

import com.db4o.config.Configuration;

public class Db4oSettings {
    private final String user;
    private final String password;
    private final String host;
    private final int port;
    private final Configuration configuration;
    private final HostKind hostKind;
    private final String databaseFileName;

    public Db4oSettings(String user, String password, String host, int port,
                        Configuration configuration, HostKind hostKind, String databaseFileName) {
        this.databaseFileName = databaseFileName;
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        this.configuration = configuration;
        this.hostKind = hostKind;
    }

    public boolean isLocal() {
        return HostKind.LOCAL.equals(hostKind);
    }

    public boolean isRemote() {
        return HostKind.REMOTE.equals(hostKind);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getDatabaseFileName() {
        return databaseFileName;
    }

    public HostKind getHostKind() {
        return this.hostKind;
    }
}
