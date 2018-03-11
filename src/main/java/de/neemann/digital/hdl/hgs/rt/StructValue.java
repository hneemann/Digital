/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.rt;

import java.util.HashMap;

/**
 *
 * @author ideras
 */
public class StructValue extends RtValue implements IStruct {
    private final HashMap<String, RtReference> fields;

    /**
     * Creates a new instance.
     */
    public StructValue() {
        fields = new HashMap<>();
    }

    @Override
    public RtReference getFieldReference(String fieldName) {
        return fields.get(fieldName);
    }

    @Override
    public RtValue getFieldValue(String fieldName) {
        if (fields.containsKey(fieldName)) {
            return fields.get(fieldName).getTarget();
        } else {
            return null;
        }
    }

    /**
     * Add a new field to the struct of the type specified.
     *
     * @param fieldName the name of the field to add.
     * @param type      the type of the field.
     */
    public void createField(String fieldName, RtValue.Type type) {
        fields.put(fieldName, new RtReference(type));
    }

    /**
     * Add a field to struct if not already created.
     *
     * @param fieldName the name of the field to add.
     * @param type      the type of field to add.
     */
    public void createFieldIfNotExist(String fieldName, RtValue.Type type) {
        if (!fields.containsKey(fieldName)) {
            fields.put(fieldName, new RtReference(type));
        }
    }

    @Override
    public void setFieldValue(String fieldName, RtValue value) {
        if (fields.containsKey(fieldName)) {
            RtReference vref = fields.get(fieldName);
            vref.set(value);
        } else {
            fields.put(fieldName, new RtReference(value));
        }
    }

    /**
     * Set a value of a field to an integer.
     *
     * @param fieldName the field name.
     * @param value     the integer value to set.
     */
    public void setFieldValue(String fieldName, int value) {
        setFieldValue(fieldName, new IntValue(value));
    }

    /**
     * Set the value of a field to a string.
     *
     * @param fieldName the field name
     * @param value     the string value to set.
     */
    public void setFieldValue(String fieldName, String value) {
        setFieldValue(fieldName, new StringValue(value));
    }

    @Override
    public Type getType() {
        return Type.STRUCT;
    }

    @Override
    public String toString() {
                StringBuilder sb = new StringBuilder();
        boolean first = true;

        sb.append("{");
        for (String f : fields.keySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            RtReference vref = fields.get(f);

            sb.append(f).append(": ").append(vref.getTarget().toString());
        }
        sb.append("}");

        return sb.toString();
    }
}
