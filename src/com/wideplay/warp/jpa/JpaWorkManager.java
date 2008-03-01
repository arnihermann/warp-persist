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

package com.wideplay.warp.jpa;

import com.wideplay.warp.persist.WorkManager;

import net.jcip.annotations.Immutable;

/**
 * Created with IntelliJ IDEA.
 * User: dhanji
 * Date: Oct 8, 2007
 * Time: 7:30:05 AM
 *
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
@Immutable
class JpaWorkManager implements WorkManager {


    public void beginWork() {
        //triggers an em creation
        EntityManagerFactoryHolder.getCurrentEntityManager();
    }

    public void endWork() {
        EntityManagerFactoryHolder.closeCurrentEntityManager();

//        //do nothing if there is no em
//        if (null == EntityManagerFactoryHolder.checkCurrentEntityManager())
//            return;
//
//        //check if it has been closed yet
//        final EntityManager currentEntityManager = EntityManagerFactoryHolder.checkCurrentEntityManager();
//        if (!currentEntityManager.isOpen())
//            return;
//
//        //close up session when done
//        currentEntityManager.close();
    }
}