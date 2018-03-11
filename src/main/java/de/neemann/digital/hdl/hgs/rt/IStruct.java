/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.rt;

/**
 *
 * @author ideras
 */
public interface IStruct {
    /**
     * Return the reference of the specified field.
     *
     * @param fieldName the field name.
     * @return the field reference or null if the field doesn't exist.
     */
    RtReference getFieldReference(String fieldName);

    /**
     * Return the value of the specified field.
     *
     * @param fieldName the field name.
     * @return the field value or null if the field doesn't exist.
     */
    RtValue getFieldValue(String fieldName);

    /**
     * Sets a field value.
     *
     * @param fieldName the field name.
     * @param value the value
     */
    void setFieldValue(String fieldName, RtValue value);
}
