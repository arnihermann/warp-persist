package com.wideplay.warp.persist.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * On: 3/06/2007
 * 
 * <p>
 * Annotate any dynamic finder method's integer argument with this to pass in the first-result
 * index. Used for paging result lists.
 * </p>
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 * @see com.wideplay.warp.persist.dao.MaxResults
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FirstResult {
}
