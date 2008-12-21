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

package com.wideplay.warp.persist;

import com.wideplay.warp.persist.dao.Finder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@Entity
@NamedQuery(name = HibernateAdvancedTxTestEntity.LIST_ALL_QUERY, query = "from HibernateAdvancedTxTestEntity")
public class HibernateAdvancedTxTestEntity {
    private Long id;
    private String text;
    public static final String LIST_ALL_QUERY = "HibernateTestEntity.listAll";

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Finder(query = "from HibernateTestEntity")
    public List<HibernateAdvancedTxTestEntity> listAll() { return null; }

    @Finder(query = "from HibernateTestEntity where text = ? or text = ?")
    public List<HibernateAdvancedTxTestEntity> listAllMatching(String s1, String s2) { return null; }
}