package com.wideplay.codemonkey.web;

import com.wideplay.codemonkey.model.Blog;
import com.wideplay.codemonkey.model.SourceArtifact;
import com.wideplay.warp.annotations.OnEvent;
import com.wideplay.warp.annotations.URIMapping;
import com.wideplay.warp.annotations.event.PreRender;
import com.wideplay.warp.annotations.event.PostRender;
import com.wideplay.warp.persist.Transactional;
import com.google.inject.Inject;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.EntityManager;

/**
 * Created with IntelliJ IDEA.
 * On: 29/04/2007
 *
 * @author Dhanji R. Prasanna
 * @since 1.0
 */
@URIMapping("/jpa")
public class StartJpa {

    private String message;
    @Inject
    private EntityManager em;
    private SourceArtifact artifact;


    public StartJpa() {
    }

    @OnEvent @PreRender
    @Transactional
    void pre() {
        message = "Is txn active? " + em.getTransaction().isActive();
        try {
            em.persist(artifact = new SourceArtifact());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @OnEvent @PostRender
    @Transactional void post() {
        boolean b = false;
        try {
            b = em.contains(artifact);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Was object in the same em? " + b);
    }

    public String getMessage() {
        return message;
    }
}
