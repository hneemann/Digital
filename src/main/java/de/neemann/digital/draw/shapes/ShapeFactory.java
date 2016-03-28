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
import de.neemann.digital.core.memory.RAMDualPort;
import de.neemann.digital.core.memory.RAMSinglePort;
import de.neemann.digital.core.wiring.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.gui.LibrarySelector;
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
        map.put(Delay.DESCRIPTION.getName(), new CreatorSimple("", Delay.DESCRIPTION, false));

        map.put(XOr.DESCRIPTION.getName(), new CreatorSimple("=1", XOr.DESCRIPTION, false));
        map.put(XNOr.DESCRIPTION.getName(), new CreatorSimple("=1", XNOr.DESCRIPTION, true));

        map.put(RAMDualPort.DESCRIPTION.getName(), attr -> new RAMShape("RAM", RAMDualPort.DESCRIPTION.getInputNames(attr), outputInfos(RAMDualPort.DESCRIPTION, attr), attr.get(AttributeKey.Label)));
        map.put(RAMSinglePort.DESCRIPTION.getName(), attr -> new RAMShape("RAM", RAMSinglePort.DESCRIPTION.getInputNames(attr), outputInfos(RAMSinglePort.DESCRIPTION, attr), attr.get(AttributeKey.Label)));

        map.put(In.DESCRIPTION.getName(), attr -> new InputShape(attr.get(AttributeKey.Label)));
        map.put(Const.DESCRIPTION.getName(), attr -> new ConstShape(attr.get(AttributeKey.Value)));
        map.put(Out.DESCRIPTION.getName(), attr -> new OutputShape(attr.get(AttributeKey.Label)));
        map.put(Out.LEDDESCRIPTION.getName(), attr -> new LEDShape(attr.get(AttributeKey.Label), attr.get(AttributeKey.Color)));
        map.put(Out.PROBEDESCRIPTION.getName(), attr -> new ProbeShape(attr.get(AttributeKey.Label)));
        map.put(Clock.DESCRIPTION.getName(), attr -> new ClockShape(attr.get(AttributeKey.Label)));
        map.put(Out.SEVENDESCRIPTION.getName(), attr -> new SevenSegShape(attr.get(AttributeKey.Label), attr.get(AttributeKey.Color)));
        map.put(Out.SEVENHEXDESCRIPTION.getName(), attr -> new SevenSegHexShape(attr.get(AttributeKey.Label), attr.get(AttributeKey.Color)));


        map.put(Multiplexer.DESCRIPTION.getName(), attr -> new MuxerShape(attr.get(AttributeKey.SelectorBits), attr.get(AttributeKey.FlipSelPositon)));
        map.put(Demultiplexer.DESCRIPTION.getName(), attr -> new DemuxerShape(attr.get(AttributeKey.SelectorBits), true, attr.get(AttributeKey.FlipSelPositon)));
        map.put(Decoder.DESCRIPTION.getName(), attr -> new DemuxerShape(attr.get(AttributeKey.SelectorBits), false, attr.get(AttributeKey.FlipSelPositon)));

        map.put(Splitter.DESCRIPTION.getName(), attr -> new SplitterShape(attr.get(AttributeKey.InputSplit), attr.get(AttributeKey.OutputSplit)));
        map.put(Driver.DESCRIPTION.getName(), attr -> new DriverShape(attr.get(AttributeKey.FlipSelPositon)));
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
                    throw new NodeException(Lang.get("err_noShapeFoundFor_N", partName), null);
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
