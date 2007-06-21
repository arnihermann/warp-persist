package com.wideplay.warp.persist;

import com.google.inject.Module;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public interface PersistenceModuleBuilder {
    Module buildModule();
}
