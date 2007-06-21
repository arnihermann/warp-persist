package com.wideplay.warp.persist.dao;

import com.google.inject.name.Named;
import com.wideplay.warp.hibernate.HibernateTestEntity;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * On: 3/06/2007
 *
 * @author Dhanji R. Prasanna
 * @since 1.0
 */
public interface TestAccessor {

    @Finder(query = "from HibernateTestEntity")
    List<HibernateTestEntity> listAll();

    @Finder(query = "from HibernateTestEntity")
    HibernateTestEntity[] listAllAsArray();

    @Finder(namedQuery = HibernateTestEntity.LIST_ALL_QUERY)
    List<HibernateTestEntity> listEverything();

    @Finder(query = "from HibernateTestEntity where text = :text", returnAs = HashSet.class)
    Set<HibernateTestEntity> find(@Named("text") String id);

    @Finder(query = "from HibernateTestEntity where id = :id")
    HibernateTestEntity fetch(@Named("id") Long id);

    @Finder(query = "from HibernateTestEntity where id = ? and text = ?")
    HibernateTestEntity fetchById(Long id, @MaxResults int i, String text);

    @Finder(query = "from HibernateTestEntity")
    List<HibernateTestEntity> listAll(@MaxResults int i);
}
