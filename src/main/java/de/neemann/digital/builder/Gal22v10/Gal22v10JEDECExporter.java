/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.Gal22v10;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.builder.*;
import de.neemann.digital.builder.Gal16v8.BuilderCollectorGAL;
import de.neemann.digital.builder.jedec.FuseMap;
import de.neemann.digital.builder.jedec.FuseMapFiller;
import de.neemann.digital.builder.jedec.FuseMapFillerException;
import de.neemann.digital.builder.jedec.JedecWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Class to create a JEDEC file for the Lattice GAL22V10
 */
public class Gal22v10JEDECExporter implements ExpressionExporter<Gal22v10JEDECExporter> {
    private static final int[] PRODUCTS_BY_OLMC = new int[]{8, 10, 12, 14, 16, 16, 14, 12, 10, 8};
    private static final int[] OE_FUSE_NUM_BY_OLMC = new int[]{44, 440, 924, 1496, 2156, 2904, 3652, 4312, 4884, 5368};
    private static final int S0 = 5808;
    private static final int S1 = 5809;
    private final FuseMap map;
    private final FuseMapFiller filler;
    private final BuilderCollector builder;
    private final PinMap pinMap;

    /**
     * Creates new instance
     */
    public Gal22v10JEDECExporter() {
        map = new FuseMap(5892);
        filler = new FuseMapFiller(map, 22);

        pinMap = new PinMap()
                .setAvailInputs(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13)
                .setAvailOutputs(14, 15, 16, 17, 18, 19, 20, 21, 22, 23);
        builder = new BuilderCollectorGAL(pinMap);
    }

    @Override
    public BuilderCollector getBuilder() {
        return builder;
    }

    @Override
    public PinMap getPinMapping() {
        return pinMap;
    }

    @Override
    public void writeTo(OutputStream out) throws FuseMapFillerException, IOException, PinMapException {
        for (String in : builder.getInputs()) {
            int i = pinMap.getInputFor(in);
            if (i==13)
                filler.addVariable(21, new Variable(in));
            else
                filler.addVariable((i-1) * 2, new Variable(in));
        }
        for (String o : builder.getOutputs()) {
            int i = 23 - pinMap.getOutputFor(o);
            filler.addVariableReverse(i * 2 + 1, new Variable(o));
        }

        for (String o : builder.getOutputs()) {
            int olmc = 23 - pinMap.getOutputFor(o);
            int offs = OE_FUSE_NUM_BY_OLMC[olmc];
            for (int j = 0; j < 44; j++) map.setFuse(offs + j); // turn on OE
            map.setFuse(S0 + olmc * 2);                         // set olmc to active high
            if (builder.getCombinatorial().containsKey(o)) {
                map.setFuse(S1 + olmc * 2);
                filler.fillExpression(offs + 44, builder.getCombinatorial().get(o), PRODUCTS_BY_OLMC[olmc]);
            } else if (builder.getRegistered().containsKey(o)) {
                filler.fillExpression(offs + 44, builder.getRegistered().get(o), PRODUCTS_BY_OLMC[olmc]);
            } else
                throw new FuseMapFillerException("variable " + o + " not found!");
        }

        try (JedecWriter w=new JedecWriter(out)) {
            w.println("Digital GAL22v10 assembler*").write(map);
        }
    }

}
