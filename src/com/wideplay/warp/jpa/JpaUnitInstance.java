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
package com.wideplay.warp.jpa;

import java.io.Serializable;
import java.lang.annotation.Annotation;

class JpaUnitInstance<T extends Annotation> implements JpaUnit, Serializable {
    private final Class<T> value;

    private JpaUnitInstance(Class<T> annotation) {
        this.value = annotation;
    }

    public Class<T> value() {
        return this.value;
    }

    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof JpaUnit)) {
            return false;
        }

        JpaUnit other = (JpaUnit) o;
        return value.equals(other.value());
    }

    public String toString() {
        return "@" + JpaUnit.class.getName() + "(value=" + value + ")";
    }

    public Class<? extends Annotation> annotationType() {
        return JpaUnit.class;
    }

    public static <T extends Annotation> JpaUnitInstance<T> of(Class<T> annotation) {
        return new JpaUnitInstance<T>(annotation);
    }
}
