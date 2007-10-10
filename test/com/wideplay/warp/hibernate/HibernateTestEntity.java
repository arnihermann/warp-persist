package com.wideplay.warp.hibernate;

import com.wideplay.warp.persist.dao.Finder;

import javax.persistence.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
@Entity
@NamedQuery(name = HibernateTestEntity.LIST_ALL_QUERY, query = "from HibernateTestEntity")
public class HibernateTestEntity {
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
    public List<HibernateTestEntity> listAll() { return null; }

    @Finder(query = "from HibernateTestEntity where text = ? or text = ?")
    public List<HibernateTestEntity> listAllMatching(String s1, String s2) { return null; }
}
