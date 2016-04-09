package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.io.Const;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.io.Probe;
import de.neemann.digital.core.memory.RAMDualPort;
import de.neemann.digital.core.memory.RAMSinglePort;
import de.neemann.digital.core.wiring.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.gui.LibrarySelector;
import de.neemann.digital.gui.components.data.DummyElement;
import de.neemann.digital.lang.Lang;

import java.util.HashMap;

/**
 * @author hneemann
 */
public final class ShapeFactory {

    private final HashMap<String, Creator> map = new HashMap<>();
    private final ElementLibrary library;

    public ShapeFactory(ElementLibrary library) {
        this.library = library;
        map.put(And.DESCRIPTION.getName(), new CreatorSimple("&", And.DESCRIPTION, false));
        map.put(Or.DESCRIPTION.getName(), new CreatorSimple("\u22651", Or.DESCRIPTION, false));
        map.put(NAnd.DESCRIPTION.getName(), new CreatorSimple("&", NAnd.DESCRIPTION, true));
        map.put(NOr.DESCRIPTION.getName(), new CreatorSimple("\u22651", NOr.DESCRIPTION, true));
        map.put(Not.DESCRIPTION.getName(), new CreatorSimple("", Not.DESCRIPTION, true));
        map.put(Delay.DESCRIPTION.getName(), attr -> new DelayShape());

        map.put(XOr.DESCRIPTION.getName(), new CreatorSimple("=1", XOr.DESCRIPTION, false));
        map.put(XNOr.DESCRIPTION.getName(), new CreatorSimple("=1", XNOr.DESCRIPTION, true));

        map.put(RAMDualPort.DESCRIPTION.getName(), attr -> new RAMShape("RAM", RAMDualPort.DESCRIPTION.getInputNames(attr), outputInfos(RAMDualPort.DESCRIPTION, attr), attr.get(AttributeKey.Label)));
        map.put(RAMSinglePort.DESCRIPTION.getName(), attr -> new RAMShape("RAM", RAMSinglePort.DESCRIPTION.getInputNames(attr), outputInfos(RAMSinglePort.DESCRIPTION, attr), attr.get(AttributeKey.Label)));

        map.put(In.DESCRIPTION.getName(), InputShape::new);
        map.put(Reset.DESCRIPTION.getName(), ResetShape::new);
        map.put(Const.DESCRIPTION.getName(), ConstShape::new);
        map.put(Out.DESCRIPTION.getName(), OutputShape::new);
        map.put(Out.LEDDESCRIPTION.getName(), LEDShape::new);
        map.put(Probe.DESCRIPTION.getName(), ProbeShape::new);
        map.put(Clock.DESCRIPTION.getName(), ClockShape::new);
        map.put(Out.SEVENDESCRIPTION.getName(), SevenSegShape::new);
        map.put(Out.SEVENHEXDESCRIPTION.getName(), SevenSegHexShape::new);
        map.put(DummyElement.DATADESCRIPTION.getName(), DataShape::new);

        map.put(Break.DESCRIPTION.getName(), BreakShape::new);

        map.put(Multiplexer.DESCRIPTION.getName(), MuxerShape::new);
        map.put(Demultiplexer.DESCRIPTION.getName(), attr -> new DemuxerShape(attr, true));
        map.put(Decoder.DESCRIPTION.getName(), attr -> new DemuxerShape(attr, false));

        map.put(Splitter.DESCRIPTION.getName(), SplitterShape::new);
        map.put(Driver.DESCRIPTION.getName(), DriverShape::new);
    }

    private OutputPinInfo[] outputInfos(ElementTypeDescription description, ElementAttributes attributes) {
        ObservableValue[] o = description.createElement(attributes).getOutputs();
        OutputPinInfo[] outInfos = new OutputPinInfo[o.length];
        for (int i = 0; i < outInfos.length; i++)
            outInfos[i] = new OutputPinInfo(o[i].getName(), o[i].isBidirectional());
        return outInfos;
    }

    public Shape getShape(String partName, ElementAttributes elementAttributes) {
        Creator cr = map.get(partName);
        try {
            if (cr == null) {
                if (library == null)
                    throw new NodeException(Lang.get("err_noShapeFoundFor_N", partName));
                else {
                    ElementTypeDescription pt = library.getElementType(partName);
                    if (pt instanceof LibrarySelector.ElementTypeDescriptionCustom) {
                        LibrarySelector.ElementTypeDescriptionCustom customDescr = (LibrarySelector.ElementTypeDescriptionCustom) pt;
                        return new GenericShape(
                                pt.getShortName(),
                                pt.getInputNames(elementAttributes),
                                outputInfos(pt, elementAttributes),
                                elementAttributes.get(AttributeKey.Label),
                                true,
                                customDescr.getAttributes().get(AttributeKey.Width));
                    } else
                        return new GenericShape(
                                pt.getShortName(),
                                pt.getInputNames(elementAttributes),
                                outputInfos(pt, elementAttributes),
                                elementAttributes.get(AttributeKey.Label),
                                true);
                }
            } else
                return cr.create(elementAttributes);
        } catch (Exception e) {
            return new MissingShape(partName, e);
        }
    }

    private interface Creator {
        Shape create(ElementAttributes attributes) throws NodeException;
    }


    public class CreatorSimple implements Creator {

        private final String name;
        private final ElementTypeDescription description;
        private final boolean invers;

        public CreatorSimple(String name, ElementTypeDescription description, boolean invers) {
            this.name = name;
            this.description = description;
            this.invers = invers;
        }

        @Override
        public Shape create(ElementAttributes attributes) throws NodeException {
            return new GenericShape(name, description.getInputNames(attributes), outputInfos(description, attributes)).invert(invers);
        }
    }
}
