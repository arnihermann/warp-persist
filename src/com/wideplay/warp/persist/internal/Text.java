/**
 * Copyright (C) 2008 Wideplay Interactive.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wideplay.warp.persist.internal;

/**
 *
 * <p>
 * A Strings utility.
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
public class Text {

    /**
     * An assertion utility
     *
     * @param str A string to test for emptiness (i.e. non null and not the empty string
     * when trimmed)
     * @param message A message to throw as an IllegalArgumentException
     * @throws IllegalArgumentException Thrown with the message if this string is empty
     */
    public static void nonEmpty(String str, String message) {
        if (empty(str))
            throw new IllegalArgumentException(message);
    }

    /**
     *
     * @param str A string to test for emptiness (i.e. non null and not the empty string
     * when trimmed)
     * @return Returns true if the string is empty.
     */
    public static boolean empty(String str) {
        return null == str || "".equals(str.trim());
    }
}
