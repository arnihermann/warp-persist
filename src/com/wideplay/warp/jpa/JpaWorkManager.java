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