package com.wideplay.warp.persist.dao;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.wideplay.warp.jpa.JpaTestEntity;
import com.wideplay.warp.jpa.JpaUnit;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.Transactional;
import com.wideplay.warp.persist.UnitOfWork;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
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
public class JpaDynamicFindersTest {
    private Injector injector;
    private static final String TEXT_1 = "unique text1" + new Date();
    private static final String TEXT_2 = "unique text2" + new Date();

    @BeforeClass
    public void pre() {
        injector = Guice.createInjector(PersistenceService.usingJpa()
                .across(UnitOfWork.TRANSACTION)
                .addAccessor(JpaTestAccessor.class)
                .buildModule(),

                new AbstractModule() {

                    protected void configure() {
                        bindConstant().annotatedWith(JpaUnit.class).to("testUnit");
                    }
                });

        injector.getInstance(PersistenceService.class).start();
    }

    @Test
    public void testListAll() {

        //set up some test data
        injector.getInstance(FinderDao.class).store();

        //now attempt to query it out
        JpaTestAccessor accessor = injector.getInstance(JpaTestAccessor.class);

        List<JpaTestEntity> results = accessor.listAll();

        assert results.size() >= 2 : "all results not returned!";

        assert results.get(0).getText().equals(TEXT_1) || results.get(0).getText().equals(TEXT_2) : "attribs not persisted correctly";
        assert results.get(1).getText().equals(TEXT_1) || results.get(1).getText().equals(TEXT_2) : "attribs not persisted correctly";
    }


    @Test
    public void testReturnAsSet() {

        //set up some test data
        injector.getInstance(FinderDao.class).store();

        //now attempt to query it out
        JpaTestAccessor accessor = injector.getInstance(JpaTestAccessor.class);

        Set<JpaTestEntity> results = accessor.set();

        assert results.size() >= 2 : "all results not returned!";

        for (JpaTestEntity result : results) {
            assert result.getText().equals(TEXT_1) || result.getText().equals(TEXT_2) : "attribs not persisted correctly";
        }
    }

    @Test
    public void testSingleResultFetchWithNamedParameter() {

        //set up some test data
        Long id = injector.getInstance(FinderDao.class).store().getId();

        //now attempt to query it out
        JpaTestAccessor accessor = injector.getInstance(JpaTestAccessor.class);

        JpaTestEntity result = accessor.fetch(id);

        assert null != result : "result not returned!";

        assert result.getId().equals(id) : "attribs not persisted correctly";
    }

    public static class FinderDao {
        private final EntityManager em;

        @Inject
        public FinderDao(EntityManager em) {
            this.em = em;
        }

        @Transactional
        JpaTestEntity store() {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(TEXT_1);

            em.persist(entity);

            entity = new JpaTestEntity();
            entity.setText(TEXT_2);
            
            em.persist(entity);

            return entity;
        }
    }
}
