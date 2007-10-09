package com.wideplay.warp.persist.dao;

import com.wideplay.warp.persist.Transactional;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
@Entity
@NamedQuery(name = HibernateTestEntityTxnal.LIST_ALL_QUERY, query = "from HibernateTestEntityTxnal")
public class HibernateTestEntityTxnal {
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

    @Transactional
    @Finder(query = "from HibernateTestEntityTxnal")
    public List<HibernateTestEntityTxnal> listAll() { return null; }
}
