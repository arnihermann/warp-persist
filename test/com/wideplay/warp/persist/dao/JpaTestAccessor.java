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
import com.wideplay.warp.jpa.JpaTestEntity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * On: 3/06/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
public interface JpaTestAccessor {

    @Finder(query = "from JpaTestEntity")
    List<JpaTestEntity> listAll();

    @Finder(query = "from JpaTestEntity", returnAs = LinkedHashSet.class)
    Set<JpaTestEntity> set();

    @Finder(query = "from JpaTestEntity where id = :id")
    JpaTestEntity fetch(@Named("id")Long id);

    @Finder(query = "from JpaTestEntity where id = ?")
    JpaTestEntity fetchById(Long id);

    @Finder(query = "from JpaTestEntity where id IN(:list)")
    JpaTestEntity fetchByIdArray(@Named("list") Long[] ids);

    @Finder(query = "from JpaTestEntity where id IN(:list)")
    JpaTestEntity fetchByIdList(@Named("list") List<Long> ids);

    @Finder(query = "from JpaTestEntity where id IN(?)")
    JpaTestEntity fetchByIdUnnamedList(List<Long> ids);
}
