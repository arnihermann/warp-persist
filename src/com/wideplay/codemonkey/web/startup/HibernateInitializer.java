package com.wideplay.codemonkey.web.startup;

import com.wideplay.warp.persist.PersistenceService;
import com.google.inject.Inject;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 2:33:20 PM
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 */
public class HibernateInitializer {
    private final PersistenceService service;

    @Inject
    public HibernateInitializer(PersistenceService service) {
        this.service = service;

        service.start();
        System.out.println("Initializer started up...");
    }

    public static Properties loadProperties(String name) {
        Properties properties = new Properties();
        final InputStream stream = HibernateInitializer.class.getResourceAsStream(name);
        try {
            properties.load(stream);
        } catch (IOException e) {
//            log.warn("Unable to find/load persistence.properties for hibernate module (assuming defaults)", e);
            return new Properties();
        } finally {
            try {
                stream.close();
            } catch(IOException e) {
                //cant do anything
//                log.warn("Exception encountered while closing stream after loading " + PERSISTENCE_PROPERTIES, e);
            }
        }

        return properties;
    }
}
