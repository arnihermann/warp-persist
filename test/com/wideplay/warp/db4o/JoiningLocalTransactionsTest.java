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
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.wideplay.warp.persist.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;

/**
 * 
 * @author Jeffrey Chung (jeffreymchung@gmail.com)
 */
@Test(suiteName = "db4o")
public class JoiningLocalTransactionsTest {
	private Injector injector;
	private static final String UNIQUE_TEXT = JoiningLocalTransactionsTest.class + "some unique text" + new Date();
    private static final String OTHER_UNIQUE_TEXT = JoiningLocalTransactionsTest.class + "some other unique text" + new Date();
    
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

		injector.getInstance(PersistenceService.class).start();
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
    public void testSimpleTxn() {
    	injector.getInstance(JoiningLocalTransactionsTest.TransactionalObject.class).runInTxn();
    	
    	ObjectContainer oc = injector.getInstance(ObjectContainer.class);
    	
    	ObjectSet<Db4oTestObject> objSet = oc.query(new Predicate<Db4oTestObject>() {
	    		public boolean match(Db4oTestObject obj) {
	    			return obj.getText().equals(UNIQUE_TEXT);
	    		}
	    	});
    	injector.getInstance(WorkManager.class).endWork();
    	
    	assert objSet.get(0) != null : "Nothing returned from predicate query: fatal";
    	assert objSet.get(0).getText().equals(UNIQUE_TEXT) : "Queried object did not match";
    }
    
    @Test
    public void testSimpleTxnRollbackOnChecked() {
    	try {
			injector.getInstance(JoiningLocalTransactionsTest.TransactionalObject.class).runInTxnThrowingChecked();
		} catch (IOException e) {
			System.out.println("Caught (expecting rollback): " + e);
		}
		
		ObjectContainer oc = injector.getInstance(ObjectContainer.class);
		
		ObjectSet<Db4oTestObject> objSet = oc.query(new Predicate<Db4oTestObject>() {
    		public boolean match(Db4oTestObject obj) {
    			return obj.getText().equals(OTHER_UNIQUE_TEXT);
    		}
    	});
		injector.getInstance(WorkManager.class).endWork();
		
		assert objSet.isEmpty() : "Result was returned: rollback did not occur";
    }
    
    @Test
    public void testSimpleTxnRollbackOnUnchecked() {
    	try {
			injector.getInstance(JoiningLocalTransactionsTest.TransactionalObject.class).runInTxnThrowingUnchecked();
		} catch (RuntimeException e) {
			System.out.println("Caught (expecting rollback): " + e);
		}
		
		ObjectContainer oc = injector.getInstance(ObjectContainer.class);
		
		ObjectSet<Db4oTestObject> objSet = oc.query(new Predicate<Db4oTestObject>() {
    		public boolean match(Db4oTestObject obj) {
    			return obj.getText().equals(OTHER_UNIQUE_TEXT);
    		}
    	});
		injector.getInstance(WorkManager.class).endWork();
		
		assert objSet.isEmpty() : "Result was returned: rollback did not occur";
    }
    
    public static class TransactionalObject {
    	private final ObjectContainer oc;
    	
    	@Inject
    	public TransactionalObject(ObjectContainer oc) {
    		this.oc = oc;
    	}
    	
    	@Transactional
    	public void runInTxn() {
    		runInTxnInternal();
    	}
    	
    	@Transactional(rollbackOn = IOException.class)
    	private void runInTxnInternal() {
    		Db4oTestObject obj = new Db4oTestObject(UNIQUE_TEXT);
    		oc.set(obj);
    	}
    	
    	@Transactional(rollbackOn = IOException.class)
    	public void runInTxnThrowingChecked() throws IOException {
    		runInTxnThrowingCheckedInternal();
    	}
    	
    	@Transactional
    	private void runInTxnThrowingCheckedInternal() throws IOException {
    		Db4oTestObject obj = new Db4oTestObject(OTHER_UNIQUE_TEXT);
    		oc.set(obj);
    		
    		throw new IOException();
    	}
    	
    	@Transactional
    	public void runInTxnThrowingUnchecked() {
    		runInTxnThrowingUncheckedInternal();
    	}
    	
    	@Transactional(rollbackOn = IOException.class)
    	private void runInTxnThrowingUncheckedInternal() {
    		Db4oTestObject obj = new Db4oTestObject(OTHER_UNIQUE_TEXT);
    		oc.set(obj);
    		
    		throw new IllegalStateException();
    	}
    }
}
