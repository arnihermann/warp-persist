package com.wideplay.codemonkey.web;

import com.google.inject.Inject;
import com.wideplay.codemonkey.model.Blog;
import com.wideplay.codemonkey.model.SourceArtifact;
import com.wideplay.warp.annotations.OnEvent;
import com.wideplay.warp.annotations.event.PostRender;
import com.wideplay.warp.annotations.event.PreRender;
import com.wideplay.warp.persist.Transactional;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * On: 29/04/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public class Start {
    private final List<Blog> blogs;

    private String message;
    @Inject private Session session;
    private SourceArtifact artifact;


    public Start() {
        blogs = new ArrayList<Blog>();

        blogs.add(new Blog("hello", "blah", new Date()));
        blogs.add(new Blog("hellawskdoo", "blah", new Date()));
        blogs.add(new Blog("hello", "dokasdokblah", new Date()));
        blogs.add(new Blog("hasjdioaskjsdello", "blaoskdoaksdah", new Date()));
    }

    @OnEvent @Click public void clickY() {
        message = "Has been clicked";
        System.out.println("clickY called! click event...");
    }

    @OnEvent @PreRender @Transactional
    void pre() {
        message = "Is txn active? " + session.getTransaction().isActive();
        try {
            session.persist(artifact = new SourceArtifact());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @OnEvent @PostRender @Transactional void post() {
        boolean b = false;
        try {
            b = session.contains(artifact);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Was object in the same session? " + b);
    }

    public List<Blog> getBlogs() {
        return blogs;
    }


    public String getMessage() {
        return message;
    }
}
