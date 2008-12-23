package com.wideplay.warp.persist.spi;

import com.google.inject.Module;
import com.wideplay.warp.persist.spi.PersistenceModuleVisitor;

/**
 * {@link com.google.inject.Module} returned by
 * a {@link com.wideplay.warp.persist.PersistenceStrategy}.
 * 
 * @author Robbie Vanbrabant
 */
public interface PersistenceModule extends Module {
    void visit(PersistenceModuleVisitor visitor);
}
