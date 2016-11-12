package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.io.*;
import de.neemann.digital.core.memory.RAMDualPort;
import de.neemann.digital.core.memory.RAMSinglePort;
import de.neemann.digital.core.pld.*;
import de.neemann.digital.core.wiring.*;
import de.neemann.digital.draw.elements.Tunnel;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ieee.IEEEAndShape;
import de.neemann.digital.draw.shapes.ieee.IEEENotShape;
import de.neemann.digital.draw.shapes.ieee.IEEEOrShape;
import de.neemann.digital.draw.shapes.ieee.IEEEXOrShape;
import de.neemann.digital.gui.LibrarySelector;
import de.neemann.digital.gui.components.data.DummyElement;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.lang.Lang;

import java.util.HashMap;

/**
 * Used to create a shape matching a given name
 *
 * @author hneemann
 */
public final class ShapeFactory {

    private final HashMap<String, Creator> map = new HashMap<>();
    private final ElementLibrary library;

    /**
     * Creates a new instance
     *
     * @param library the library to get information about the parts to visualize
     */
    public ShapeFactory(ElementLibrary library) {
        this(library, false);
    }

    /**
     * Creates a new instance
     *
     * @param library the library to get information about the parts to visualize
     * @param ieee    true if IEEE shapes required
     */
    public ShapeFactory(ElementLibrary library, boolean ieee) {
        this.library = library;
        if (ieee) {
            map.put(And.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEAndShape(inputs, outputs, false));
            map.put(NAnd.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEAndShape(inputs, outputs, true));
            map.put(Or.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEOrShape(inputs, outputs, false));
            map.put(NOr.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEOrShape(inputs, outputs, true));
            map.put(XOr.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEXOrShape(inputs, outputs, false));
            map.put(XNOr.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEXOrShape(inputs, outputs, true));
            map.put(Not.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEENotShape(inputs, outputs));
        } else {
            map.put(And.DESCRIPTION.getName(), new CreatorSimple("&", false));
            map.put(Or.DESCRIPTION.getName(), new CreatorSimple("\u22651", false));
            map.put(NAnd.DESCRIPTION.getName(), new CreatorSimple("&", true));
            map.put(NOr.DESCRIPTION.getName(), new CreatorSimple("\u22651", true));
            map.put(XOr.DESCRIPTION.getName(), new CreatorSimple("=1", false));
            map.put(XNOr.DESCRIPTION.getName(), new CreatorSimple("=1", true));
            map.put(Not.DESCRIPTION.getName(), new CreatorSimple("", true));
        }


        map.put(RAMDualPort.DESCRIPTION.getName(), (attr, inputs, outputs) -> new RAMShape(attr, RAMDualPort.DESCRIPTION.getInputDescription(attr), RAMDualPort.DESCRIPTION.getOutputDescriptions(attr)));
        map.put(RAMSinglePort.DESCRIPTION.getName(), (attr, inputs, outputs) -> new RAMShape(attr, RAMSinglePort.DESCRIPTION.getInputDescription(attr), RAMSinglePort.DESCRIPTION.getOutputDescriptions(attr)));

        map.put(In.DESCRIPTION.getName(), InputShape::new);
        map.put(Reset.DESCRIPTION.getName(), ResetShape::new);
        map.put(Const.DESCRIPTION.getName(), ConstShape::new);
        map.put(Ground.DESCRIPTION.getName(), GroundShape::new);
        map.put(VDD.DESCRIPTION.getName(), VDDShape::new);
        map.put(Switch.DESCRIPTION.getName(), SwitchShape::new);
        map.put(Out.DESCRIPTION.getName(), OutputShape::new);
        map.put(Out.LEDDESCRIPTION.getName(), LEDShape::new);
        map.put(Button.DESCRIPTION.getName(), ButtonShape::new);
        map.put(Probe.DESCRIPTION.getName(), ProbeShape::new);
        map.put(Clock.DESCRIPTION.getName(), ClockShape::new);
        map.put(Out.SEVENDESCRIPTION.getName(), SevenSegShape::new);
        map.put(Out.SEVENHEXDESCRIPTION.getName(), SevenSegHexShape::new);
        map.put(DummyElement.DATADESCRIPTION.getName(), DataShape::new);

        map.put(Break.DESCRIPTION.getName(), BreakShape::new);
        map.put(Delay.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new DelayShape());

        map.put(Multiplexer.DESCRIPTION.getName(), MuxerShape::new);
        map.put(Demultiplexer.DESCRIPTION.getName(), DemuxerShape::new);
        map.put(Decoder.DESCRIPTION.getName(), DemuxerShape::new);

        map.put(Splitter.DESCRIPTION.getName(), SplitterShape::new);
        map.put(Driver.DESCRIPTION.getName(), DriverShape::new);
        map.put(DriverInvSel.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new DriverShape(attributes, inputs, outputs, true));
        map.put(Tunnel.DESCRIPTION.getName(), TunnelShape::new);

        map.put(DummyElement.TEXTDESCRIPTION.getName(), TextShape::new);
        map.put(TestCaseElement.TESTCASEDESCRIPTION.getName(), TestCaseShape::new);

        map.put(Diode.DESCRIPTION.getName(), DiodeShape::new);
        map.put(DiodeForeward.DESCRIPTION.getName(), DiodeForewardShape::new);
        map.put(DiodeBackward.DESCRIPTION.getName(), DiodeBackwardShape::new);
        map.put(PullUp.DESCRIPTION.getName(), PullUpShape::new);
        map.put(PullDown.DESCRIPTION.getName(), PullDownShape::new);
    }

    /**
     * Returns a shape matching the given name.
     * If no shape is found, a special "missing shape" shape is returned.
     *
     * @param elementName       the elemnets name
     * @param elementAttributes the elements attributes
     * @return the shape
     */
    public Shape getShape(String elementName, ElementAttributes elementAttributes) {
        Creator cr = map.get(elementName);
        try {
            if (cr == null) {
                if (library == null)
                    throw new NodeException(Lang.get("err_noShapeFoundFor_N", elementName));
                else {
                    ElementTypeDescription pt = library.getElementType(elementName);
                    if (pt instanceof LibrarySelector.ElementTypeDescriptionCustom) {
                        LibrarySelector.ElementTypeDescriptionCustom customDescr = (LibrarySelector.ElementTypeDescriptionCustom) pt;
                        return new GenericShape(
                                pt.getShortName(),
                                pt.getInputDescription(elementAttributes),
                                pt.getOutputDescriptions(elementAttributes),
                                elementAttributes.getLabel(),
                                true,
                                customDescr.getAttributes().get(Keys.WIDTH))
                                .setColor(customDescr.getAttributes().get(Keys.BACKGROUND_COLOR));
                    } else
                        return new GenericShape(
                                pt.getShortName(),
                                pt.getInputDescription(elementAttributes),
                                pt.getOutputDescriptions(elementAttributes),
                                elementAttributes.getLabel(),
                                true);
                }
            } else {
                ElementTypeDescription pt = library.getElementType(elementName);
                return cr.create(elementAttributes,
                        pt.getInputDescription(elementAttributes),
                        pt.getOutputDescriptions(elementAttributes));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new MissingShape(elementName, e);
        }
    }

    private interface Creator {
        Shape create(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) throws NodeException;
    }


    private static final class CreatorSimple implements Creator {

        private final String name;
        private final boolean invers;

        private CreatorSimple(String name, boolean invers) {
            this.name = name;
            this.invers = invers;
        }

        @Override
        public Shape create(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) throws NodeException {
            return new GenericShape(name, inputs, outputs).invert(invers);
        }
    }
}
