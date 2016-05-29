package de.neemann.digital.draw.builder.Gal16v8;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.draw.builder.JedecCreator;
import de.neemann.digital.draw.builder.jedec.FuseMap;
import de.neemann.digital.draw.builder.jedec.FuseMapFiller;
import de.neemann.digital.draw.builder.jedec.FuseMapFillerException;
import de.neemann.digital.draw.builder.jedec.JedecWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Class to create a JEDEC file for the Lattice GAL16V8
 *
 * @author hneemann
 */
public class Gal16v8 implements JedecCreator<Gal16v8> {
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
    private final HashMap<String, Integer> pinMap;

    /**
     * Creates new instance
     */
    public Gal16v8() {
        map = new FuseMap(2194);
        for (int i = PTD_START; i <= PTD_END; i++)
            map.setFuse(i, true);
        filler = new FuseMapFiller(map, 16);

        builder = new BuilderCollector();
        pinMap = new HashMap<>();
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
    private Gal16v8 enableRegisterFor(int i) {
        map.setFuse(AC1 + i, false);
        return this;
    }

    @Override
    public Gal16v8 assignPin(String name, int pin) throws FuseMapFillerException {
        if (pinMap.containsKey(name))
            throw new FuseMapFillerException("Pin " + name + " assigned twice");
        pinMap.put(name, pin);
        return this;
    }

    private int getInputFor(String in) throws FuseMapFillerException {
        Integer p = pinMap.get(in);
        if (p == null) {
            for (int i = 2; i <= 9; i++) {
                if (!pinMap.containsValue(i)) {
                    pinMap.put(in, i);
                    p = i;
                    break;
                }
            }
        }
        if (p == null) {
            throw new FuseMapFillerException("to manny inputs defined");
        } else if (p < 2 || p > 9) {
            throw new FuseMapFillerException("input " + p + " not allowed!");
        }

        return p - 2;
    }

    private int getOutputFor(String out) throws FuseMapFillerException {
        Integer p = pinMap.get(out);
        if (p == null) {
            for (int i = 19; i >= 12; i--) {
                if (!pinMap.containsValue(i)) {
                    pinMap.put(out, i);
                    p = i;
                    break;
                }
            }
        }
        if (p == null) {
            throw new FuseMapFillerException("to manny outputs defined");
        } else if (p < 12 || p > 19) {
            throw new FuseMapFillerException("output " + p + " not allowed!");
        }

        return 19 - p;
    }


    @Override
    public void writeTo(OutputStream out) throws FuseMapFillerException, IOException {
        boolean registered = !builder.getRegistered().isEmpty();
        init(registered);

        for (String in : builder.getInputs()) {
            int i = getInputFor(in);
            filler.addVariable(i * 2, new Variable(in));
        }
        for (String o : builder.getOutputs()) {
            int i = getOutputFor(o);
            filler.addVariable(i * 2 + 1, new Variable(o));
        }

        for (String o : builder.getOutputs()) {
            int offs = getOutputFor(o) * 256;
            if (builder.getCombinatorial().containsKey(o)) {
                for (int j = 0; j < 32; j++) map.setFuse(offs + j);
                filler.fillExpression(offs + 32, builder.getCombinatorial().get(o), 7);
            } else if (builder.getRegistered().containsKey(o)) {
                enableRegisterFor(getOutputFor(o));
                filler.fillExpression(offs, builder.getRegistered().get(o), 8);
            } else
                throw new FuseMapFillerException("variable " + o + " not found!");

        }

        new JedecWriter(out).println("Digital GAL16v8 assembler*").write(map).close();
    }

}
