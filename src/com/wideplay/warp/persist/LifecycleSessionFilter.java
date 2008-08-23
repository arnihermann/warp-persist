package com.wideplay.warp.persist;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SessionFilter that starts and stops all registered {@link PersistenceService} instances
 * upon {@link javax.servlet.Filter#init(javax.servlet.FilterConfig)} and
 * {@link javax.servlet.Filter#destroy()}.
 * 
 * @author Robbie Vanbrabant
 * @see com.wideplay.warp.persist.SessionFilter
 */
public class LifecycleSessionFilter extends SessionFilter {
    private static final List<PersistenceService> persistenceServices = new CopyOnWriteArrayList<PersistenceService>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        for(PersistenceService ps : persistenceServices)
            ps.start();
        super.init(filterConfig);
    }

    @Override
    public void destroy() {
        super.destroy();
        for(PersistenceService ps : persistenceServices)
            ps.shutdown();
        persistenceServices.clear();
    }

    /**
     * The different persistence strategies should add their
     * {@link com.wideplay.warp.persist.PersistenceService} here
     * at configuration time if they support {@link UnitOfWork#REQUEST}.
     */
    public static void registerPersistenceService(PersistenceService ps) {
        persistenceServices.add(ps);
    }
}
