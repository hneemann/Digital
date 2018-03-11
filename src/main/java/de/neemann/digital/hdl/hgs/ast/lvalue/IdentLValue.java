/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.lvalue;

import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.rt.RtReference;
import de.neemann.digital.hdl.hgs.rt.RtValue;
import de.neemann.digital.lang.Lang;

/**
 *
 * @author ideras
 */
public class IdentLValue extends LValue {
    private final String name;

    /**
     * Create a new instance
     *
     * @param line the source line
     * @param name the identifier name
     */
    public IdentLValue(int line, String name) {
        super(line);
        this.name = name;
    }

    /**
     * Returns the identifier name
     *
     * @return the identifier name
     */
    public String getName() {
        return name;
    }

    @Override
    public RtReference getReference(HGSRuntimeContext ctx) throws HGSException {
        if (!ctx.containsVariable(name)) {
            throw new HGSException(getLine(), Lang.get("variableNotDefined", name));
        }
        return ctx.getVariableRef(name);
    }

    @Override
    public RtReference register(HGSRuntimeContext ctx, RtValue.Type type) throws HGSException {
        ctx.registerVariable(name, type);

        return ctx.getVariableRef(name);
    }
}
