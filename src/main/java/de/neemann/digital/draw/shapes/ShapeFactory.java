/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.extern.External;
import de.neemann.digital.core.io.*;
import de.neemann.digital.core.memory.*;
import de.neemann.digital.core.pld.*;
import de.neemann.digital.core.switching.*;
import de.neemann.digital.core.wiring.*;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.Tunnel;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementTypeDescriptionCustom;
import de.neemann.digital.draw.library.JarComponentManager;
import de.neemann.digital.draw.shapes.custom.CustomShape;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.draw.shapes.ieee.IEEEAndShape;
import de.neemann.digital.draw.shapes.ieee.IEEENotShape;
import de.neemann.digital.draw.shapes.ieee.IEEEOrShape;
import de.neemann.digital.draw.shapes.ieee.IEEEXOrShape;
import de.neemann.digital.gui.components.data.DummyElement;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseElement;

import java.util.HashMap;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * Used to create a shape matching a given name
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
        library.setShapeFactory(this);
        if (ieee) {
            map.put(And.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEAndShape(inputs, outputs, false, attributes));
            map.put(NAnd.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEAndShape(inputs, outputs, true, attributes));
            map.put(Or.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEOrShape(inputs, outputs, false, attributes));
            map.put(NOr.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEOrShape(inputs, outputs, true, attributes));
            map.put(XOr.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEXOrShape(inputs, outputs, false, attributes));
            map.put(XNOr.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEEXOrShape(inputs, outputs, true, attributes));
            map.put(Not.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new IEEENotShape(inputs, outputs, attributes));
        } else {
            map.put(And.DESCRIPTION.getName(), new CreatorSimple("&", false));
            map.put(Or.DESCRIPTION.getName(), new CreatorSimple("\u22651", false));
            map.put(NAnd.DESCRIPTION.getName(), new CreatorSimple("&", true));
            map.put(NOr.DESCRIPTION.getName(), new CreatorSimple("\u22651", true));
            map.put(XOr.DESCRIPTION.getName(), new CreatorSimple("=1", false));
            map.put(XNOr.DESCRIPTION.getName(), new CreatorSimple("=1", true));
            map.put(Not.DESCRIPTION.getName(),
                    (attributes, inputs, outputs) -> {
                        final boolean ws = attributes.get(Keys.WIDE_SHAPE);
                        return new GenericShape(ws ? "1" : "", inputs, outputs)
                                .setTopBottomBorder(ws ? SIZE : SIZE2)
                                .invert(true)
                                .setWide(ws);
                    });
        }


        map.put(RAMDualPort.DESCRIPTION.getName(), (attr, inputs, outputs) -> new RAMShape(attr, RAMDualPort.DESCRIPTION));
        map.put(RAMSinglePort.DESCRIPTION.getName(), (attr, inputs, outputs) -> new RAMShape(attr, RAMSinglePort.DESCRIPTION));
        map.put(RAMSinglePortSel.DESCRIPTION.getName(), (attr, inputs, outputs) -> new RAMShape(attr, RAMSinglePortSel.DESCRIPTION));
        map.put(EEPROM.DESCRIPTION.getName(), (attr, inputs, outputs) -> new RAMShape(attr, EEPROM.DESCRIPTION));
        map.put(EEPROMDualPort.DESCRIPTION.getName(), (attr, inputs, outputs) -> new RAMShape(attr, EEPROMDualPort.DESCRIPTION));
        map.put(RAMDualAccess.DESCRIPTION.getName(), (attr, inputs, outputs) -> new RAMShape(attr, RAMDualAccess.DESCRIPTION));
        map.put(RegisterFile.DESCRIPTION.getName(), (attr, inputs, outputs) -> new RAMShape(attr, RegisterFile.DESCRIPTION, 4));
        map.put(BlockRAMDualPort.DESCRIPTION.getName(), (attr, inputs, outputs) -> new RAMShape(attr, BlockRAMDualPort.DESCRIPTION));

        map.put(In.DESCRIPTION.getName(), InputShape::new);
        map.put(Reset.DESCRIPTION.getName(), ResetShape::new);
        map.put(Const.DESCRIPTION.getName(), ConstShape::new);
        map.put(Ground.DESCRIPTION.getName(), GroundShape::new);
        map.put(VDD.DESCRIPTION.getName(), VDDShape::new);
        map.put(Out.DESCRIPTION.getName(), OutputShape::new);
        map.put(Out.LEDDESCRIPTION.getName(), LEDShape::new);
        map.put(LightBulb.DESCRIPTION.getName(), LightBulbShape::new);
        map.put(RGBLED.DESCRIPTION.getName(), RGBLEDShape::new);
        map.put(Out.POLARITYAWARELEDDESCRIPTION.getName(), PolarityAwareLEDShape::new);
        map.put(Button.DESCRIPTION.getName(), ButtonShape::new);
        map.put(ButtonLED.DESCRIPTION.getName(), ButtonLEDShape::new);
        map.put(Probe.DESCRIPTION.getName(), ProbeShape::new);
        map.put(Clock.DESCRIPTION.getName(), ClockShape::new);
        map.put(Out.SEVENDESCRIPTION.getName(), SevenSegShape::new);
        map.put(Out.SEVENHEXDESCRIPTION.getName(), SevenSegHexShape::new);
        map.put(Out.SIXTEENDESCRIPTION.getName(), SixteenShape::new);
        map.put(DummyElement.DATADESCRIPTION.getName(), DataShape::new);
        map.put(RotEncoder.DESCRIPTION.getName(), RotEncoderShape::new);
        map.put(StepperMotorUnipolar.DESCRIPTION.getName(), StepperMotorShape::new);
        map.put(StepperMotorBipolar.DESCRIPTION.getName(), StepperMotorShape::new);
        map.put(DipSwitch.DESCRIPTION.getName(), DipSwitchShape::new);

        map.put(Switch.DESCRIPTION.getName(), SwitchShape::new);
        map.put(SwitchDT.DESCRIPTION.getName(), SwitchDTShape::new);
        map.put(Fuse.DESCRIPTION.getName(), FuseShape::new);
        map.put(Relay.DESCRIPTION.getName(), RelayShape::new);
        map.put(RelayDT.DESCRIPTION.getName(), RelayDTShape::new);
        map.put(NFET.DESCRIPTION.getName(), FETShapeN::new);
        map.put(FGNFET.DESCRIPTION.getName(), FGFETShapeN::new);
        map.put(FGPFET.DESCRIPTION.getName(), FGFETShapeP::new);
        map.put(PFET.DESCRIPTION.getName(), FETShapeP::new);
        map.put(TransGate.DESCRIPTION.getName(), TransGateShape::new);

        map.put(Break.DESCRIPTION.getName(), BreakShape::new);
        map.put(Delay.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new DelayShape());

        map.put(Multiplexer.DESCRIPTION.getName(), MuxerShape::new);
        map.put(Demultiplexer.DESCRIPTION.getName(), DemuxerShape::new);
        map.put(Decoder.DESCRIPTION.getName(), DemuxerShape::new);
        map.put(BitSelector.DESCRIPTION.getName(), BitSelShape::new);
        map.put(PriorityEncoder.DESCRIPTION.getName(),
                (attributes, inputs, outputs) -> new GenericShape(
                        PriorityEncoder.DESCRIPTION.getShortName(),
                        inputs,
                        outputs,
                        attributes.getLabel(),
                        true,
                        4));

        map.put(Splitter.DESCRIPTION.getName(), SplitterShape::new);
        map.put(Driver.DESCRIPTION.getName(), DriverShape::new);
        map.put(DriverInvSel.DESCRIPTION.getName(), (attributes, inputs, outputs) -> new DriverShape(attributes, inputs, outputs, true));
        map.put(BusSplitter.DESCRIPTION.getName(), BusSplitterShape::new);
        map.put(Tunnel.DESCRIPTION.getName(), TunnelShape::new);

        map.put(DummyElement.TEXTDESCRIPTION.getName(), TextShape::new);
        map.put(DummyElement.RECTDESCRIPTION.getName(), RectShape::new);
        map.put(TestCaseElement.TESTCASEDESCRIPTION.getName(), TestCaseShape::new);
        map.put(AsyncSeq.DESCRIPTION.getName(), AsyncClockShape::new);

        map.put(Diode.DESCRIPTION.getName(), DiodeShape::new);
        map.put(DiodeForward.DESCRIPTION.getName(), DiodeForewardShape::new);
        map.put(DiodeBackward.DESCRIPTION.getName(), DiodeBackwardShape::new);
        map.put(PullUp.DESCRIPTION.getName(), PullUpShape::new);
        map.put(PullDown.DESCRIPTION.getName(), PullDownShape::new);
        map.put(PinControl.DESCRIPTION.getName(), PinControlShape::new);

        // disables string formatting for external components, see #272
        map.put(External.DESCRIPTION.getName(),
                (attributes, inputs, outputs) ->
                        new GenericShape(External.DESCRIPTION.getShortName(), inputs, outputs,
                                attributes.getLabel(), true, attributes.get(Keys.WIDTH)) {
                            @Override
                            public String format(String s) {
                                return s.replace("_", "\\_");
                            }
                        });

        final JarComponentManager jcm = library.getJarComponentManager();
        if (jcm != null)
            for (JarComponentManager.AdditionalShape c : jcm)
                map.put(c.getDescription().getName(), c.getShape());
    }

    /**
     * Returns a shape matching the given name.
     * If no shape is found, a special "missing shape" shape is returned.
     *
     * @param elementName       the elements name
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
                    if (pt instanceof ElementTypeDescriptionCustom) {
                        // Custom component
                        ElementTypeDescriptionCustom customDescr = (ElementTypeDescriptionCustom) pt;
                        CustomCircuitShapeType shapeType = customDescr.getAttributes().get(Keys.SHAPE_TYPE);
                        final CustomCircuitShapeType localShapeType = elementAttributes.get(Keys.SHAPE_TYPE);
                        if (!localShapeType.equals(CustomCircuitShapeType.DEFAULT))
                            shapeType = localShapeType;

                        switch (shapeType) {
                            case DIL:
                                return new DILShape(
                                        pt.getShortName(),
                                        pt.getInputDescription(elementAttributes),
                                        pt.getOutputDescriptions(elementAttributes),
                                        elementAttributes.getLabel(),
                                        customDescr.getAttributes());
                            case LAYOUT:
                                return new LayoutShape(customDescr, elementAttributes);
                            case CUSTOM:
                                final CustomShapeDescription customShapeDescription = customDescr.getAttributes().get(Keys.CUSTOM_SHAPE);
                                if (!customShapeDescription.isEmpty())
                                    return new CustomShape(customShapeDescription, elementAttributes.getLabel(),
                                            pt.getInputDescription(elementAttributes),
                                            pt.getOutputDescriptions(elementAttributes));
                            default:
                                return new GenericShape(
                                        pt.getShortName(),
                                        pt.getInputDescription(elementAttributes),
                                        pt.getOutputDescriptions(elementAttributes),
                                        elementAttributes.getLabel(),
                                        true,
                                        customDescr.getAttributes().get(Keys.WIDTH))
                                        .setTopBottomBorder(SIZE2 + Style.MAXLINETHICK)
                                        .setColor(customDescr.getAttributes().get(Keys.BACKGROUND_COLOR));
                        }
                    } else {
                        // Internal component without it's own shape.
                        return new GenericShape(
                                pt.getShortName(),
                                pt.getInputDescription(elementAttributes),
                                pt.getOutputDescriptions(elementAttributes),
                                elementAttributes.getLabel(),
                                true,
                                elementAttributes.get(Keys.WIDTH))
                                .setInverterConfig(elementAttributes.get(Keys.INVERTER_CONFIG));
                    }
                }
            } else {
                // Internal component with it's own shape.
                ElementTypeDescription pt = library.getElementType(elementName);
                return cr.create(elementAttributes,
                        pt.getInputDescription(elementAttributes),
                        pt.getOutputDescriptions(elementAttributes));
            }
        } catch (Exception e) {
            return new MissingShape(elementName, e);
        }
    }

    /**
     * Creates a new shape
     */
    public interface Creator {
        /**
         * Called to create a new shape
         *
         * @param attributes the elements attributes
         * @param inputs     the inputs
         * @param outputs    the outputs
         * @return the shape
         * @throws NodeException NodeException
         * @throws PinException  PinException
         */
        Shape create(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) throws NodeException, PinException;
    }


    private static final class CreatorSimple implements Creator {

        private final String name;
        private final boolean invers;

        private CreatorSimple(String name, boolean invers) {
            this.name = name;
            this.invers = invers;
        }

        @Override
        public Shape create(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
            return new GenericShape(name, inputs, outputs)
                    .invert(invers)
                    .setWide(attributes.get(Keys.WIDE_SHAPE))
                    .setInverterConfig(attributes.get(Keys.INVERTER_CONFIG));
        }
    }
}
