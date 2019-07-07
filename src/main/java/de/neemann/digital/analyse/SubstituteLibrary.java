/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.library.LibraryInterface;
import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        MAP.put("JK_FF", new SubstituteGenericHGSParser("JK_FF.dig"));
        MAP.put("T_FF", new SubstituteMatching()
                .add(attr -> attr.get(Keys.WITH_ENABLE), new SubstituteGenericHGSParser("T_FF_EN.dig"))
                .add(attr -> true, new SubstituteGenericHGSParser("T_FF.dig"))
        );
        MAP.put("Counter", new SubstituteGenericHGSParser("Counter.dig"));
        MAP.put("CounterPreset", new SubstituteGenericHGSParser("CounterPreset.dig"));
        MAP.put("Register", new SubstituteGenericHGSParser("Register.dig"));
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

            return ElementLibrary.createCustomDescription(new File(filename), c, library).isSubstitutedBuiltIn();
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
                Context context = new Context()
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
            } else
                attr.set(k, val);
        }

        @Override
        public Object hgsMapGet(String key) {
            return attr.hgsMapGet(key);
        }
    }
}
