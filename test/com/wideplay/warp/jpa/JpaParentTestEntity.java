package com.wideplay.warp.jpa;

import com.wideplay.warp.persist.dao.Finder;

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
public class JpaParentTestEntity {
    private Long id;
    private List<JpaTestEntity> children = new ArrayList<JpaTestEntity>();

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToMany
    public List<JpaTestEntity> getChildren() {
        return children;
    }

    public void setChildren(List<JpaTestEntity> children) {
        this.children = children;
    }
}
