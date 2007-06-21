package com.wideplay.warp.persist.dao;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.Session;
import com.google.inject.Injector;
import com.google.inject.Guice;
import com.google.inject.AbstractModule;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.warp.hibernate.HibernateTestEntity;
import com.wideplay.codemonkey.web.startup.Initializer;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 4/06/2007
 * Time: 15:57:24
 * <p/>
 *
 * @author dprasanna
 * @since 1.0
 */
public class HibernateDynamicFindersTest {
    private Injector injector;
    private static final String TEXT_1 = "unique text1" + new Date();
    private static final String TEXT_2 = "unique text2" + new Date();

    @BeforeTest
    public void pre() {
        injector = Guice.createInjector(PersistenceService.usingHibernate()
                .across(UnitOfWork.TRANSACTION)
                .addAccessor(TestAccessor.class)
                .buildModule(),

                new AbstractModule() {

                    protected void configure() {
                        bind(Configuration.class).toInstance(new AnnotationConfiguration()
                            .addAnnotatedClass(HibernateTestEntity.class)
                            .setProperties(Initializer.loadProperties("spt-persistence.properties")));
                    }
                });

        injector.getInstance(PersistenceService.class).start();
    }

    @Test public void testDynamicFinderListAll() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestEntity accessor = injector.getInstance(HibernateTestEntity.class);
        List<HibernateTestEntity> results = accessor.listAll();
        session.getTransaction().commit();

        //assert them
        assert results.size() >= 2 : "atleast 2 results expected! was: " + results.size();

        assert results.get(0).getText().equals(TEXT_1) || results.get(0).getText().equals(TEXT_2) : "attribs not persisted correctly";
        assert results.get(1).getText().equals(TEXT_1) || results.get(1).getText().equals(TEXT_2) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorListAll() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        TestAccessor accessor = injector.getInstance(TestAccessor.class);
        List<HibernateTestEntity> results = accessor.listAll();
        session.getTransaction().commit();

        //assert them
        assert results.size() >= 2 : "atleast 2 results expected! was: " + results.size();

        assert results.get(0).getText().equals(TEXT_1) || results.get(0).getText().equals(TEXT_2) : "attribs not persisted correctly";
        assert results.get(1).getText().equals(TEXT_1) || results.get(1).getText().equals(TEXT_2) : "attribs not persisted correctly";

    }


    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorListAllWithPaging() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);
        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);
        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        TestAccessor accessor = injector.getInstance(TestAccessor.class);
        List<HibernateTestEntity> results = accessor.listAll(1);
        session.getTransaction().commit();

        //assert them
        assert results.size() == 1 : "only 1 result expected! was: " + results.size();

        assert results.get(0).getText().equals(TEXT_1) || results.get(0).getText().equals(TEXT_2) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
//    @Test TODO fix 
    public void testDynamicAccessorListAllAsArray() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        TestAccessor accessor = injector.getInstance(TestAccessor.class);
        HibernateTestEntity[] results = accessor.listAllAsArray();
        session.getTransaction().commit();

        //assert them
        assert results.length >= 2 : "atleast 2 results expected! was: " + results.length;

        assert results[0].getText().equals(TEXT_1) || results[0].getText().equals(TEXT_2) : "attribs not persisted correctly";
        assert results[1].getText().equals(TEXT_1) || results[1].getText().equals(TEXT_2) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorNamedQuery() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        TestAccessor accessor = injector.getInstance(TestAccessor.class);
        List<HibernateTestEntity> results = accessor.listEverything();
        session.getTransaction().commit();

        //assert them
        assert results.size() >= 2 : "atleast 2 results expected! was: " + results.size();

        assert results.get(0).getText().equals(TEXT_1) || results.get(0).getText().equals(TEXT_2) : "attribs not persisted correctly";
        assert results.get(1).getText().equals(TEXT_1) || results.get(1).getText().equals(TEXT_2) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorFinderWithBoundParamsAsSet() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        TestAccessor accessor = injector.getInstance(TestAccessor.class);
        Set<HibernateTestEntity> results = accessor.find(TEXT_1);
        session.getTransaction().commit();

        //assert them
        assert results.size() >= 1 : "atleast 1 results expected! was: " + results.size();

        for (HibernateTestEntity res : results)
            assert res.getText().equals(TEXT_1) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorFinderWithNamedBoundParamsSingleFetch() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        TestAccessor accessor = injector.getInstance(TestAccessor.class);
        HibernateTestEntity result = accessor.fetch(entity.getId());
        session.getTransaction().commit();

        //assert them
        assert result != null : "atleast 1 results expected!";

        assert result.getText().equals(TEXT_2) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorFinderWithRawBoundParamsSingleFetch() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        TestAccessor accessor = injector.getInstance(TestAccessor.class);
        HibernateTestEntity result = accessor.fetchById(entity.getId(), 1, TEXT_2);
        session.getTransaction().commit();

        //assert them
        assert result != null : "atleast 1 results expected!";

        assert result.getText().equals(TEXT_2) : "attribs not persisted correctly";

    }
}
