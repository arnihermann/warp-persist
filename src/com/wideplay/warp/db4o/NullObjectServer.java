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
 * @author Robbie Vanbrabant
 */
public class NullObjectServer implements ObjectServer {
    private static final AtomicInteger uniqueNumber = new AtomicInteger();
    private final int number = uniqueNumber.getAndIncrement();

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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NullObjectServer that = (NullObjectServer) o;

        return number == that.number;
    }

    public int hashCode() {
        return number;
    }
}
