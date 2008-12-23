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
import com.db4o.ObjectServer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.warp.persist.WorkManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * 
 * @author Jeffrey Chung (jeffreymchung@gmail.com)
 */
@Test(suiteName = "db4o")
public class ObjectServerProvisionTest {
	private Injector injector;

	@BeforeClass
	public void preClass() {
		injector = Guice.createInjector(PersistenceService.usingDb4o()
				.across(UnitOfWork.TRANSACTION)
				.transactedWith(TransactionStrategy.LOCAL)
				.buildModule(),

				new AbstractModule() {
					protected void configure() {	
						bindConstant().annotatedWith(Db4Objects.class).to("TestDatabase.data");
					}
				}
		);
	}

	@AfterClass
	public void postClass() {
		injector.getInstance(ObjectServer.class).close();
	}
	
	@AfterTest
    public void postTest() {
    	injector.getInstance(WorkManager.class).endWork();
    }
	
	@Test
	public void testObjectContainerStartup() {
		//assert injector.getInstance(ObjectServerHolder.class).equals(injector.getInstance(ObjectServerHolder.class));
		assert injector.getInstance(PersistenceService.class).equals(injector.getInstance(PersistenceService.class))
				: "Singleton violation: " + Db4oPersistenceService.class.getName();
		
		injector.getInstance(PersistenceService.class).start();
		
		assert !injector.getInstance(ObjectContainer.class).ext().isClosed() : "Object container is not open";
	}
}
