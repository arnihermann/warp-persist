package com.wideplay.warp.persist;

import com.google.inject.Module;

/**
 * {@link com.google.inject.Module} returned by
 * a {@link com.wideplay.warp.persist.PersistenceStrategy}.
 * 
 * @author Robbie Vanbrabant
 */
public interface PersistenceModule extends Module {
    void visit(PersistenceModuleVisitor visitor);
}
