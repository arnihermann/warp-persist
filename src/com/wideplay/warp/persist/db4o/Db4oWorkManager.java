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

import com.db4o.ObjectContainer;
import com.wideplay.warp.persist.InternalWorkManager;
import com.wideplay.warp.persist.WorkManager;

/**
 * @author Jeffrey Chung (jeffreymchung@gmail.com)
 */
class Db4oWorkManager implements WorkManager {
    private final InternalWorkManager<ObjectContainer> internalWorkManager;

    public Db4oWorkManager(InternalWorkManager<ObjectContainer> internalWorkManager) {
        this.internalWorkManager = internalWorkManager;
    }

    public void beginWork() {
        this.internalWorkManager.beginWork();
    }

    public void endWork() {
        this.internalWorkManager.endWork();
    }

    public String toString() {
        return super.toString();
    }
}
