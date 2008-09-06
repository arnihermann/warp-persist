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
import com.db4o.config.Configuration;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.wideplay.warp.persist.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jeffrey Chung (jeffreymchung@gmail.com), Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = "db4o")
public class Db4oWorkManagerTest {
	private Injector injector;

	@BeforeClass
	public void preClass() {
		injector = Guice.createInjector(PersistenceService.usingDb4o()
				.across(UnitOfWork.REQUEST)
				.transactedWith(TransactionStrategy.LOCAL)
				.buildModule(),

				new AbstractModule() {
					protected void configure() {
						bindConstant().annotatedWith(Db4Objects.class).to("TestDatabase.data");

						bindConstant().annotatedWith(Names.named(Db4Objects.HOST)).to("localhost");
						bindConstant().annotatedWith(Names.named(Db4Objects.PORT)).to(4321);
						bindConstant().annotatedWith(Names.named(Db4Objects.USER)).to("autobot");
						bindConstant().annotatedWith(Names.named(Db4Objects.PASSWORD)).to("morethanmeetstheeye");

						Configuration config = Db4o.newConfiguration();
						bind(Configuration.class).toInstance(config);
					}
				}
		);

		injector.getInstance(PersistenceService.class).start();
	}

	@AfterClass
	public void postClass() {
		injector.getInstance(ObjectServer.class).close();
        SessionFilter.clearWorkManagers();
    }

	@Test
	public void verifySingleObjectContainerOverUnitOfWork() {
        injector.getInstance(WorkManager.class).beginWork();

        ObjectContainer unitOfWorkOC = injector.getInstance(ObjectContainer.class);

        Db4oDao dao = injector.getInstance(Db4oDao.class);

		assert Db4oDao.oc.equals(unitOfWorkOC)
				: "Duplicate object containers";

        assert unitOfWorkOC.equals(injector.getInstance(ObjectContainer.class))
                : "Duplicate object containers";

        Db4oTestObject obj = new Db4oTestObject("more than meets the eye");

        assert unitOfWorkOC.equals(injector.getInstance(ObjectContainer.class))
                : "Duplicate object containers";
        
        dao.persist(obj);

		assert Db4oDao.oc.equals(unitOfWorkOC)
				: "Duplicate object containers";

		// start a new object container
		dao = injector.getInstance(Db4oDao.class);

        assert unitOfWorkOC.equals(injector.getInstance(ObjectContainer.class))
                : "Duplicate object containers";

        assert dao.contains(obj) : "Object container was closed unexpectedly (no persistent behavior)";

        assert unitOfWorkOC.equals(injector.getInstance(ObjectContainer.class))
                : "Duplicate object containers";

        injector.getInstance(WorkManager.class).endWork();
	}

	public static class Db4oDao {
		static ObjectContainer oc;

		@Inject
		public Db4oDao(ObjectContainer oc) {
			Db4oDao.oc = oc;
		}

		@Transactional
		public <T> void persist(T t) {
			assert !oc.ext().isClosed() : "oc is not open";
			oc.set(t);

			assert oc.ext().isStored(t) : "Persisting object failed";
		}

		@Transactional
		public <T> boolean contains(T t) {
			return oc.ext().isStored(t);
		}
	}
}