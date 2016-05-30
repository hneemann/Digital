package de.neemann.digital.builder.Gal16v8;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.builder.BuilderCollector;
import de.neemann.digital.builder.ExpressionExporter;
import de.neemann.digital.builder.PinMap;
import de.neemann.digital.builder.jedec.FuseMap;
import de.neemann.digital.builder.jedec.FuseMapFiller;
import de.neemann.digital.builder.jedec.FuseMapFillerException;
import de.neemann.digital.builder.jedec.JedecWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Class to create a JEDEC file for the Lattice GAL16V8
 *
 * @author hneemann
 */
public class Gal16v8JEDECExporter implements ExpressionExporter<Gal16v8JEDECExporter> {
    private static final int SIG = 2056;
    private static final int SYN = 2192;
    private static final int AC0 = 2193;
    private static final int PTD_START = 2128;
    private static final int PTD_END = 2191;
    private static final int XOR = 2048;
    private static final int AC1 = 2120;


    private final FuseMap map;
    private final FuseMapFiller filler;
    private final BuilderCollector builder;
    private final PinMap pinMap;

    /**
     * Creates new instance
     */
    public Gal16v8JEDECExporter() {
        map = new FuseMap(2194);
        for (int i = PTD_START; i <= PTD_END; i++)
            map.setFuse(i, true);
        filler = new FuseMapFiller(map, 16);

        builder = new BuilderCollector();
        pinMap = new PinMap()
                .setAvailInputs(2, 3, 4, 5, 6, 7, 8, 9)
                .setAvailOutputs(12, 13, 14, 15, 16, 17, 18, 19);
    }

    private void init(boolean registered) {
        if (registered) {
            map.setFuse(SYN, false);
            map.setFuse(AC0, true);
            for (int i = 0; i < 8; i++)
                map.setFuse(AC1 + i, true);
        } else {
            map.setFuse(SYN, true);
            map.setFuse(AC0, false);
            for (int i = 0; i < 8; i++)
                map.setFuse(AC1 + i, false);
        }
    }

    @Override
    public BuilderCollector getBuilder() {
        return builder;
    }

    /**
     * enables register in registered mode
     *
     * @param i the OLMC to enable the register for
     * @return this for chained calls
     */
    private Gal16v8JEDECExporter enableRegisterFor(int i) {
        map.setFuse(AC1 + i, false);
        return this;
    }

    @Override
    public Gal16v8JEDECExporter assignPin(String name, int pin) throws FuseMapFillerException {
        pinMap.assignPin(name, pin);
        return this;
    }

    @Override
    public void writeTo(OutputStream out) throws FuseMapFillerException, IOException {
        boolean registered = !builder.getRegistered().isEmpty();
        init(registered);

        for (String in : builder.getInputs()) {
            int i = pinMap.getInputFor(in) - 2;
            filler.addVariable(i * 2, new Variable(in));
        }
        for (String o : builder.getOutputs()) {
            int i = 19 - pinMap.getOutputFor(o);
            filler.addVariable(i * 2 + 1, new Variable(o));
        }

        for (String o : builder.getOutputs()) {
            int OLMC = 19 - pinMap.getOutputFor(o);
            int offs = OLMC * 256;
            if (builder.getCombinatorial().containsKey(o)) {
                map.setFuse(XOR+OLMC);   // set XOR to compensate inverted driver
                if (registered) {
                    for (int j = 0; j < 32; j++) map.setFuse(offs + j);
                    filler.fillExpression(offs + 32, builder.getCombinatorial().get(o), 7);
                } else {
                    filler.fillExpression(offs, builder.getCombinatorial().get(o), 8);
                }
            } else if (builder.getRegistered().containsKey(o)) {
                enableRegisterFor(OLMC);
                map.setFuse(XOR+OLMC);   // set XOR to compensate inverted driver
                filler.fillExpression(offs, builder.getRegistered().get(o), 8);
            } else
                throw new FuseMapFillerException("variable " + o + " not found!");
        }

        if (!registered) {
            // turn on unused AC1(n) bits
            for (int i : pinMap.getUnusedOutputs())
                map.setFuse(AC1+(19-i));
        }


        new JedecWriter(out).println("Digital GAL16v8 assembler*").write(map).close();
    }

}
