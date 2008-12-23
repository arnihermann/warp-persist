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

package com.wideplay.warp.persist.dao;

import com.google.inject.name.Named;
import com.wideplay.warp.persist.hibernate.HibernateTestEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * On: 3/06/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
public interface HibernateTestAccessor {

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

    @Finder(query = "from HibernateTestEntity where id IN(:list)")
    HibernateTestEntity fetchByIdArray(@Named("list") Long[] ids);

    @Finder(query = "from HibernateTestEntity where id IN(:list)")
    HibernateTestEntity fetchByIdList(@Named("list") List<Long> ids);

    @Finder(query = "from HibernateTestEntity where id IN(?)")
    HibernateTestEntity fetchByIdUnnamedList(List<Long> ids);
}
