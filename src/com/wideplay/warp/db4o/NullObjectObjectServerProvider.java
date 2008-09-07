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

import com.db4o.ObjectContainer;
import com.db4o.ObjectServer;
import com.db4o.config.Configuration;
import com.db4o.ext.ExtObjectServer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ObjectServerProvider that doesn't give out valid
 * instances, and should not be used with Guice.
 * Only used to store properties and to serve as a key
 * in {@link com.wideplay.warp.persist.ManagedContext} in
 * {@link HostKind#REMOTE}.
 * <p>
 * Somewhat implements the Null Object design pattern.
 * 
 * @author Robbie Vanbrabant
 */
public class NullObjectObjectServerProvider extends AbstractObjectServerProvider {
    private static final AtomicInteger uniqueNumber = new AtomicInteger();
    private final int number = uniqueNumber.getAndIncrement();

    public NullObjectObjectServerProvider(Db4oSettings settings) {
        super(settings);
        if (!HostKind.REMOTE.equals(settings.getHostKind())) {
            throw new IllegalArgumentException("Only use this class with HostKind.REMOTE");
        }
    }

    public ObjectServer get() {
        // Avoid NPE on PersistenceService.shutdown()
        return new ObjectServer() {
            public boolean close() {
                return false;
            }
            public ExtObjectServer ext() {
                throw new UnsupportedOperationException();
            }
            public void grantAccess(String s, String s1) {
                throw new UnsupportedOperationException();
            }
            public ObjectContainer openClient() {
                throw new UnsupportedOperationException();
            }
            public ObjectContainer openClient(Configuration configuration) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NullObjectObjectServerProvider that = (NullObjectObjectServerProvider) o;

        return number == that.number;
    }

    public int hashCode() {
        return number;
    }
}
