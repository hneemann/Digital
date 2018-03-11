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
public class RtReference {
    private RtValue target;

    /**
     * Creates a new instance.
     *
     * @param target the target value.
     */
    public RtReference(RtValue target) {
        this.target = target;
    }

    /**
     * Creates a new instance with a null target value.
     */
    public RtReference() {
        this.target = null;
    }

    /**
     * Create a new instance with a target value of the type specified.
     *
     * @param type the target value type.
     */
    public RtReference(RtValue.Type type) {
        switch (type) {
            case INT: set(new IntValue(0)); break;
            case STRING: set(new StringValue("")); break;
            case ARRAY: set(new ArrayValue()); break;
            case STRUCT: set(new StructValue()); break;
            case NOTHING: set(null); break;
            default:
                throw new RuntimeException("Unexpected type.");
        }
    }

    /**
     * Returns the target value type.
     *
     * @return the target value type.
     */
    public RtValue.Type getType() {
        return (target == null)? RtValue.Type.NOTHING : target.getType();
    }

    /**
     * Set the target value.
     *
     * @param value the new target value.
     */
    public final void set(RtValue value) {
        target = value;
    }

    /**
     * Returns the target value.
     *
     * @return the target value.
     */
    public RtValue getTarget() {
        return target;
    }

    /**
     * Checks if the target is null.
     *
     * @return true is the target is null, false otherwise.
     */
    public boolean isNull() {
        return (target == null);
    }
}
