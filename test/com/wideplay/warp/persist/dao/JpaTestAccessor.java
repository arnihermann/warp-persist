package com.wideplay.warp.persist.dao;

import com.google.inject.name.Named;
import com.wideplay.warp.jpa.JpaTestEntity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * On: 3/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public interface JpaTestAccessor {

    @Finder(query = "from JpaTestEntity")
    List<JpaTestEntity> listAll();

    @Finder(query = "from JpaTestEntity", returnAs = LinkedHashSet.class)
    Set<JpaTestEntity> set();

    @Finder(query = "from JpaTestEntity where id = :id")
    JpaTestEntity fetch(@Named("id")Long id);
}
