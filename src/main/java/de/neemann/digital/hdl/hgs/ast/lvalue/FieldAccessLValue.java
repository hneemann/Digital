/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.lvalue;

import de.neemann.digital.hdl.hgs.rt.RtValue;
import de.neemann.digital.hdl.hgs.rt.IStruct;
import de.neemann.digital.hdl.hgs.rt.RtReference;
import de.neemann.digital.hdl.hgs.rt.StructValue;
import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.lang.Lang;

/**
 *
 * @author ideras
 */
public class FieldAccessLValue extends LValue {
    private final LValue structVal;
    private final String fieldName;

    /**
     * Creates a new instance
     *
     * @param line the source line
     * @param structVal the left value
     * @param fieldName the field name
     */
    public FieldAccessLValue(int line, LValue structVal, String fieldName) {
        super(line);
        this.structVal = structVal;
        this.fieldName = fieldName;
    }

    /**
     * Return the struct left value node
     *
     * @return the struct left value node
     */
    public LValue getStructVal() {
        return structVal;
    }

    /**
     * Returns the field name
     *
     * @return the field name
     */
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public RtReference getReference(HGSRuntimeContext ctx) throws HGSException {
        RtReference vref = structVal.getReference(ctx);

        if (!(vref.getTarget().isArray() || vref.getTarget().isStruct())) {
            throw new HGSException(getLine(), Lang.get("lvalueNotStruct", structVal));
        }

        IStruct structv = ((IStruct) vref.getTarget());

        if (vref.getTarget().isStruct()) {
            ((StructValue) vref.getTarget()).createFieldIfNotExist(fieldName, RtValue.Type.NOTHING);
        }

        return structv.getFieldReference(fieldName);
    }

    @Override
    public RtReference register(HGSRuntimeContext ctx, RtValue.Type type) throws HGSException {
        RtReference structRef = structVal.register(ctx, RtValue.Type.STRUCT);
        StructValue struct = ((StructValue) structRef.getTarget());
        struct.createField(fieldName, type);

        return struct.getFieldReference(fieldName);
    }
}
