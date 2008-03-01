package com.wideplay.warp.util;

/**
 * Created with IntelliJ IDEA.
 * User: dhanji
 * Date: Mar 1, 2008
 * Time: 10:21:41 AM
 *
 * <p>
 * A utility analogous to com.google.inject.util.Strings 
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
public class Text {

    public static void nonEmpty(String str, String message) {
        if (isNotEmpty(str))
            throw new IllegalArgumentException(message);
    }

    public static boolean isNotEmpty(String str) {
        return null == str || "".equals(str.trim());
    }
}
