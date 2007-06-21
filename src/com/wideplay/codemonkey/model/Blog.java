package com.wideplay.codemonkey.model;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * On: May 10, 2007 7:53:55 PM
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 */
public class Blog {
    private String text;
    private String subject;
    private Date enteredOn;

    public Blog(String text, String subject, Date enteredOn) {
        this.text = text;
        this.subject = subject;
        this.enteredOn = enteredOn;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getEnteredOn() {
        return enteredOn;
    }

    public void setEnteredOn(Date enteredOn) {
        this.enteredOn = enteredOn;
    }
}
