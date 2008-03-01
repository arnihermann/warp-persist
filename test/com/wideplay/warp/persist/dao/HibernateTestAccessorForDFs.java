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

import com.wideplay.warp.hibernate.HibernateTestEntity;
import com.wideplay.warp.persist.Transactional;
import com.google.inject.name.Named;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * On: 3/06/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
public interface HibernateTestAccessorForDFs {

    @Finder(query = "from HibernateTestEntityTxnal")
    List<HibernateTestEntityTxnal> listAll();


    @Finder(query = "from HibernateTestEntityTxnal")
    HibernateTestEntityTxnal[] listAllAsArray();


    @Finder(namedQuery = HibernateTestEntityTxnal.LIST_ALL_QUERY)
    List<HibernateTestEntityTxnal> listEverything();


    @Finder(query = "from HibernateTestEntityTxnal where text = :text", returnAs = HashSet.class)
    Set<HibernateTestEntityTxnal> find(@Named("text") String id);


    @Finder(query = "from HibernateTestEntityTxnal where id = :id")
    HibernateTestEntityTxnal fetch(@Named("id") Long id);


    @Finder(query = "from HibernateTestEntityTxnal where id = ? and text = ?")
    HibernateTestEntityTxnal fetchById(Long id, @MaxResults int i, String text);


    @Finder(query = "from HibernateTestEntityTxnal")
    List<HibernateTestEntityTxnal> listAll(@MaxResults int i);
}
