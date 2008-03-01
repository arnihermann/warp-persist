package com.wideplay.codemonkey.web;

import com.google.inject.Inject;
import com.wideplay.codemonkey.model.SourceArtifact;
import com.wideplay.warp.annotations.OnEvent;
import com.wideplay.warp.annotations.URIMapping;
import com.wideplay.warp.annotations.event.PostRender;
import com.wideplay.warp.annotations.event.PreRender;
import com.wideplay.warp.persist.Transactional;
import com.db4o.ObjectContainer;

import javax.persistence.EntityManager;

/**
 * Created with IntelliJ IDEA.
 * On: 29/04/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
@URIMapping("/db4o")
public class StartDb4o {

    private String message;

    @Inject
    private ObjectContainer em;
    private SourceArtifact artifact;


    public StartDb4o() {
    }

    @OnEvent @PreRender
    @Transactional
    void pre() {
        message = "Is ObjectContainer active? " + !em.ext().isClosed();
        try {
            em.set(artifact = new SourceArtifact());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @OnEvent @PostRender
    @Transactional void post() {
        boolean b = false;
        try {
            b = em.ext().isStored(artifact);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Was object in the same em? " + b);
    }

    public String getMessage() {
        return message;
    }
}