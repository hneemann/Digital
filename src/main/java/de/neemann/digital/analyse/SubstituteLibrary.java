/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.core.Bits;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.flipflops.FlipflopJK;
import de.neemann.digital.core.flipflops.FlipflopT;
import de.neemann.digital.core.io.InValue;
import de.neemann.digital.core.memory.Counter;
import de.neemann.digital.core.memory.CounterPreset;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.Register;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.library.LibraryInterface;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to substitute certain complex builtin components by simple custom components.
 * Used to allow to analyse this components in a more simple way.
 */
public class SubstituteLibrary implements LibraryInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubstituteLibrary.class);
    private static final Map<String, SubstituteInterface> MAP = new HashMap<>();

    static {
        MAP.put(FlipflopJK.DESCRIPTION.getName(), new SubstituteGenericHGSParser("JK_FF.dig"));
        MAP.put(FlipflopT.DESCRIPTION.getName(), new SubstituteMatching()
                .add(attr -> attr.get(Keys.WITH_ENABLE), new SubstituteGenericHGSParser("T_FF_EN.dig"))
                .add(attr -> true, new SubstituteGenericHGSParser("T_FF.dig"))
        );
        MAP.put(Counter.DESCRIPTION.getName(), new SubstituteGenericHGSParser("Counter.dig"));
        MAP.put(CounterPreset.DESCRIPTION.getName(), new SubstituteGenericHGSParser("CounterPreset.dig"));
        MAP.put(Register.DESCRIPTION.getName(), new SubstituteGenericHGSParser("Register.dig"));
    }

    private final ElementLibrary parent;

    /**
     * Creates a new instance
     *
     * @param parent the parent library used to create the not substitutable components.
     */
    public SubstituteLibrary(ElementLibrary parent) {
        this.parent = parent;
    }

    @Override
    public ElementTypeDescription getElementType(String elementName, ElementAttributes attr) throws ElementNotFoundException {
        SubstituteInterface subst = MAP.get(elementName);
        if (subst != null) {
            try {
                ElementTypeDescription type = subst.getElementType(attr, parent);
                if (type != null)
                    return type;
            } catch (PinException | IOException e) {
                throw new ElementNotFoundException(Lang.get("err_substitutingError"), e);
            }
        }
        return parent.getElementType(elementName, attr);
    }

    @Override
    public ShapeFactory getShapeFactory() {
        return parent.getShapeFactory();
    }

    private interface SubstituteInterface {
        ElementTypeDescription getElementType(ElementAttributes attr, ElementLibrary library) throws PinException, IOException;
    }

    private static final class SubstituteMatching implements SubstituteInterface {
        private final ArrayList<Matcher> matcher;

        private SubstituteMatching() {
            matcher = new ArrayList<>();
        }

        private SubstituteMatching add(Accept accept, SubstituteInterface substituteInterface) {
            matcher.add(new Matcher(accept, substituteInterface));
            return this;
        }

        @Override
        public ElementTypeDescription getElementType(ElementAttributes attr, ElementLibrary library) throws PinException, IOException {
            for (Matcher m : matcher) {
                ElementTypeDescription type = m.getElementType(attr, library);
                if (type != null)
                    return type;
            }
            return null;
        }
    }

    private static final class Matcher implements SubstituteInterface {
        private final Accept accept;
        private final SubstituteInterface substituteInterface;

        private Matcher(Accept accept, SubstituteInterface substituteInterface) {
            this.accept = accept;
            this.substituteInterface = substituteInterface;
        }

        @Override
        public ElementTypeDescription getElementType(ElementAttributes attr, ElementLibrary library) throws PinException, IOException {
            if (accept.accept(attr))
                return substituteInterface.getElementType(attr, library);
            return null;
        }
    }

    private interface Accept {
        boolean accept(ElementAttributes attr);
    }

    private static abstract class SubstituteGeneric implements SubstituteInterface {
        private final String filename;
        private Circuit circuit;

        private SubstituteGeneric(String filename) {
            this.filename = filename;
        }

        @Override
        public ElementTypeDescription getElementType(ElementAttributes attr, ElementLibrary library) throws PinException, IOException {
            if (circuit == null) {
                LOGGER.debug("load substitute circuit " + filename);
                InputStream in = getClass().getClassLoader().getResourceAsStream("analyser/" + filename);
                if (in == null)
                    throw new IOException("substituting failed: could not find file " + filename);

                circuit = Circuit.loadCircuit(in, library.getShapeFactory());
            }

            Circuit c = circuit.createDeepCopy();
            // disable the normal generic handling!
            c.getAttributes().set(Keys.IS_GENERIC, false);
            generify(attr, c);

            return ElementLibrary.createCustomDescription(new File(filename), c, library);
        }

        private void generify(ElementAttributes attr, Circuit circuit) throws IOException {
            for (VisualElement v : circuit.getElements()) {
                String gen = v.getElementAttributes().get(Keys.GENERIC).trim();
                if (!gen.isEmpty())
                    generify(attr, gen, v.getElementAttributes());
            }
        }

        abstract void generify(ElementAttributes sourceAttributes, String gen, ElementAttributes nodeAttributes) throws IOException;
    }

    private static final class SubstituteGenericHGSParser extends SubstituteGeneric {
        private final HashMap<String, Statement> map;

        private SubstituteGenericHGSParser(String filename) {
            super(filename);
            map = new HashMap<>();
        }

        @Override
        void generify(ElementAttributes sourceAttributes, String gen, ElementAttributes nodeAttributes) throws IOException {
            try {
                Statement s = map.get(gen);
                if (s == null) {
                    LOGGER.debug("generic: " + gen);
                    s = new Parser(gen).parse(false);
                    map.put(gen, s);
                }
                Context context = new Context((File) null)
                        .declareVar("orig", sourceAttributes)
                        .declareVar("this", new AllowSetAttributes(nodeAttributes));
                s.execute(context);
            } catch (ParserException | HGSEvalException e) {
                throw new IOException(e);
            }
        }

    }

    /**
     * Allows writing access to the attributes.
     */
    public static final class AllowSetAttributes implements HGSMap {
        private final ElementAttributes attr;

        /**
         * Creates a new instance.
         *
         * @param attr the attributes to write to.
         */
        public AllowSetAttributes(ElementAttributes attr) {
            this.attr = attr;
        }

        @Override
        public void hgsMapPut(String key, Object val) throws HGSEvalException {
            Key k = Keys.getKeyByName(key);
            if (k == null) {
                throw new HGSEvalException("key " + key + " is invalid");
            } else {
                Class<?> expectedClass = k.getDefault().getClass();

                val = doImplicitTypeCasts(expectedClass, val);

                boolean isAssignable = expectedClass.isAssignableFrom(val.getClass());
                if (!isAssignable)
                    throw new HGSEvalException("error writing to " + key + ": value of type " + val.getClass().getSimpleName() + " can't be assigned to " + expectedClass.getSimpleName());
                attr.set(k, val);
            }
        }

        @Override
        public Object hgsMapGet(String key) {
            return attr.hgsMapGet(key);
        }
    }

    static Object doImplicitTypeCasts(Class<?> expectedClass, Object val) {
        if (expectedClass == Integer.class && val instanceof Long) {
            long l = (Long) val;
            if (l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE)
                return (int) l;
        } else if (expectedClass == Long.class && val instanceof Number) {
            return ((Number) val).longValue();
        } else if (expectedClass == Color.class && val instanceof Number) {
            return new Color(((Number) val).intValue());
        } else if (expectedClass == Boolean.class && val instanceof Number) {
            long b = ((Number) val).longValue();
            return b != 0;
        } else if (expectedClass == InValue.class) {
            if (val instanceof Number)
                return new InValue(((Number) val).longValue());
            else {
                try {
                    return new InValue(val.toString());
                } catch (Bits.NumberFormatException e) {
                    return val;
                }
            }
        } else if (expectedClass == InverterConfig.class && val instanceof java.util.List) {
            InverterConfig.Builder b = new InverterConfig.Builder();
            for (Object i : (java.util.List) val)
                b.add(i.toString());
            return b.build();
        } else if (expectedClass == DataField.class && val instanceof java.util.List) {
            java.util.List list = (java.util.List) val;
            long[] longs = new long[list.size()];
            for (int i = 0; i < list.size(); i++)
                if (list.get(i) instanceof Number)
                    longs[i] = ((Number) list.get(i)).longValue();
                else
                    return val;
            return new DataField(longs);
        } else if (expectedClass == Rotation.class && val instanceof Number) {
            int r = ((Number) val).intValue();
            return new Rotation(r % 4);
        } else if (expectedClass == File.class && val instanceof String) {
            return new File(val.toString());
        } else if (expectedClass == TestCaseDescription.class && val instanceof String) {
            try {
                return new TestCaseDescription(val.toString());
            } catch (Exception e) {
                return val;
            }
        } else if (expectedClass.isEnum() && val instanceof Number) {
            Class<Enum<?>> e = (Class<Enum<?>>) expectedClass;
            Object[] values = e.getEnumConstants();
            int index = ((Number) val).intValue();
            if (index < 0 || index >= values.length)
                return values[0];
            else
                return values[index];
        }
        return val;
    }

}
