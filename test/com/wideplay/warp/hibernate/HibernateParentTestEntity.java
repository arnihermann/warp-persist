package com.wideplay.warp.hibernate;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
@Entity
//@NamedQuery(name = HibernateParentTestEntity.LIST_ALL_QUERY, query = "from HibernateTestEntity")
public class HibernateParentTestEntity {
    private Long id;
    private String text;
    private List<HibernateTestEntity> children = new ArrayList<HibernateTestEntity>();

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

    @OneToMany
    public List<HibernateTestEntity> getChildren() {
        return children;
    }

    public void setChildren(List<HibernateTestEntity> entity) {
        this.children = entity;
    }
}
