/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.CustomElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.library.LibraryInterface;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to substitute certain complex builtin components by simple custom components.
 * Used to allow to analyse this components in a more simple way.
 */
public class SubstituteLibrary implements LibraryInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubstituteLibrary.class);
    private static final Map<String, SubstituteInterface> map = new HashMap<>();
    private static final SubstituteInterface T_FF_WITH_ENABLE = new Substitute("T_FF_EN.dig");
    private static final SubstituteInterface T_FF_WITHOUT_ENABLE = new Substitute("T_FF.dig");

    static {
        map.put("JK_FF", new Substitute("JK_FF.dig"));
        map.put("T_FF", (attr, library) -> {
            if (attr.get(Keys.WITH_ENABLE))
                return T_FF_WITH_ENABLE.getElementType(attr, library);
            else
                return T_FF_WITHOUT_ENABLE.getElementType(attr, library);
        });
        map.put("Counter", new Substitute("Counter.dig"));
    }

    private final ElementLibrary parent;

    public SubstituteLibrary(ElementLibrary parent) {
        this.parent = parent;
    }

    @Override
    public ElementTypeDescription getElementType(String elementName, ElementAttributes attr) throws ElementNotFoundException {
        SubstituteInterface subst = map.get(elementName);
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

    private static final class Substitute implements SubstituteInterface {
        private final String filename;
        private ElementLibrary.ElementTypeDescriptionCustom typeDescriptionCustom;

        private Substitute(String filename) {
            this.filename = filename;
        }

        @Override
        public ElementTypeDescription getElementType(ElementAttributes attr, ElementLibrary library) throws PinException, IOException {
            if (typeDescriptionCustom == null) {
                LOGGER.info("load substitute circuit " + filename);
                InputStream in = getClass().getClassLoader().getResourceAsStream("analyser/" + filename);
                if (in == null)
                    throw new IOException("substituting failed: could not find file " + filename);

                Circuit circuit = modify(Circuit.loadCircuit(in, library.getShapeFactory()));

                typeDescriptionCustom =
                        new ElementLibrary.ElementTypeDescriptionCustom(new File(filename),
                                attributes -> new CustomElement(circuit, library),
                                circuit);
            }
            return typeDescriptionCustom;
        }

        private Circuit modify(Circuit circuit) {
            return circuit;
        }
    }

}
