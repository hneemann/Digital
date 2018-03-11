/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.lvalue;

import de.neemann.digital.hdl.hgs.ast.expr.Expr;
import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.rt.ArrayValue;
import de.neemann.digital.hdl.hgs.rt.IntValue;
import de.neemann.digital.hdl.hgs.rt.RtReference;
import de.neemann.digital.hdl.hgs.rt.RtValue;
import de.neemann.digital.lang.Lang;

/**
 *
 * @author ideras
 */
public class IndexedLValue extends LValue {
    private final LValue arrayVal;
    private final Expr indexExpr;

    /**
     * Create a new instance
     *
     * @param line the source line
     * @param expr the left value array
     * @param indexExpr the index expression
     */
    public IndexedLValue(int line, LValue expr, Expr indexExpr) {
        super(line);
        this.arrayVal = expr;
        this.indexExpr = indexExpr;
    }

    /**
     * Returns the array left value node
     *
     * @return  the array left value node
     */
    public LValue getArrayVal() {
        return arrayVal;
    }

    /**
     * Returns the index expression
     *
     * @return the index expression
     */
    public Expr getIndexExpr() {
        return indexExpr;
    }

    @Override
    public RtReference getReference(HGSRuntimeContext ctx) throws HGSException {
        RtReference vref = arrayVal.getReference(ctx);

        if (vref.getType() != RtValue.Type.ARRAY) {
           throw new HGSException(getLine(), Lang.get("lvalueNotArray", arrayVal));
        }

        ArrayValue arrv = ((ArrayValue) vref.getTarget());
        RtValue indexValue = indexExpr.evaluate(ctx);

        if (indexValue.getType() != RtValue.Type.INT) {
            throw new HGSException(getLine(), Lang.get("arrayIndexNotInt"));
        }

        int index = ((IntValue) indexValue).getValue();

        if (index < 0) {
            throw new HGSException(getLine(), Lang.get("invalidIndexInArrayAccess"));
        }
        arrv.ensureCapacity(index + 1);

        return arrv.getReferenceAt(index);
    }

    @Override
    public RtReference register(HGSRuntimeContext ctx, RtValue.Type type) throws HGSException {
        RtReference arrayRef = arrayVal.register(ctx, RtValue.Type.ARRAY);
        RtValue indexValue = indexExpr.evaluate(ctx);

        if (!indexValue.isInt()) {
            throw new HGSException(getLine(), Lang.get("arrayIndexNotInt"));
        }

        int index = ((IntValue) indexValue).getValue();
        ArrayValue arrv = ((ArrayValue) arrayRef.getTarget());

        arrv.ensureCapacity(index + 1);

        return arrv.getReferenceAt(index);
    }

}
