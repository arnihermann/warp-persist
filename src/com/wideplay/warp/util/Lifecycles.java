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
package com.wideplay.warp.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class Lifecycles {
    private Lifecycles() {}

    public static <E extends Exception> void failEarlyAndLeaveNoOneBehind(List<Lifecycle> lifecycles, ExceptionalRunnable<E> exceptionalRunnable) throws E {
        failEarly(lifecycles);
        try {
            exceptionalRunnable.run();
        } finally {
            leaveNoOneBehind(lifecycles);
        }
    }

    public static void failEarly(List<Lifecycle> lifecycles) {
        // Iterate with index so we can clean up if needed.
        for (int i = 0, size = lifecycles.size(); i < size; i++) {
            try {
                lifecycles.get(i).start();
            } catch (RuntimeException e) {
                // clean up what we did so far and end this madness.
                try {
                    leaveNoOneBehind(lifecycles.subList(0, i));
                } catch (final RuntimeException closeErrors) {
                    // Better than nothing.
                    throw new MessageDelegatingRuntimeException(closeErrors.getMessage(), e);
                }
                throw e;
            }
        }
    }

    /**
     * Tries to end work for as much work as possible, in order.
     * Accumulates exceptions and rethrows them in a RuntimeException.
     */
    public static void leaveNoOneBehind(List<Lifecycle> lifecycles) {
        StringBuilder exceptionMessages = new StringBuilder();
        for (Lifecycle lifecycle : lifecycles) {
            try {
                lifecycle.stop();
            } catch (RuntimeException e) {
                // record the exception and proceed
                exceptionMessages.append(String.format("Could not end work for '%s': %s%n%s%n",
                        lifecycle.toString(), e.getMessage(), stackTraceAsString(e)));
            }
        }
        if (exceptionMessages.length() > 0) {
            throw new RuntimeException(exceptionMessages.toString());
        }
    }

    private static String stackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            e.printStackTrace(pw);
            return sw.getBuffer().toString();
        } finally {
            try {
                sw.close();
            } catch (IOException ignored) {
            } finally {
                pw.close();
            }
        }
    }

    // Static class because a generic class can't create a anonymous subclass of Throwable
    private static class MessageDelegatingRuntimeException extends RuntimeException {
        private final String newMessage;

        public MessageDelegatingRuntimeException(String newMessage, Throwable nestedThrowable) {
            super(nestedThrowable);
            this.newMessage = newMessage;
        }

        @Override
        public String getMessage() {
            return String.format("Unable to start work: %s%nUnable to clean up after failing:%n%s",
                    super.getMessage(), newMessage);
        }
    }
}
