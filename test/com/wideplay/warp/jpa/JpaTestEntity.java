package com.wideplay.warp.jpa;

import com.wideplay.warp.persist.dao.Finder;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna
 * @since 1.0
 */
@Entity
@NamedQuery(name = JpaTestEntity.LIST_ALL_QUERY, query = "from JpaTestEntity")
public class JpaTestEntity {
    private Long id;
    private String text;
    public static final String LIST_ALL_QUERY = "JpaTestEntity.listAll";

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

    @Finder(query = "from JpaTestEntity")
    public List<JpaTestEntity> listAll() { return null; }
}
