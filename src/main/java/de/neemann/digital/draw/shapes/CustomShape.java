package de.neemann.digital.draw.shapes;

import java.io.IOException;
import java.util.ArrayList;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.svg.ImportSVG;
import de.neemann.digital.draw.graphics.svg.NoParsableSVGException;
import de.neemann.digital.draw.graphics.svg.SVG;
import de.neemann.digital.draw.graphics.svg.SVGDrawable;
import de.neemann.digital.draw.graphics.svg.SVGFragment;

/**
 * Custom shape, generated from an SVG File
 * @author felix
 */
public class CustomShape implements Shape {

    private ArrayList<SVGFragment> fragments;
    private Pins pins;

    /**
     * Creates a Custom Shape
     * @param svg
     *            SVG Object
     * @param inputs
     *            Input Pins
     * @param outputs
     *            Output Pins
     * @throws NoParsableSVGException
     *             if the SVG is not parsable
     * @throws IOException
     *             if the SVG couldn't be read
     */
    public CustomShape(SVG svg, PinDescriptions inputs, PinDescriptions outputs)
            throws NoParsableSVGException, IOException {
        ImportSVG importer=null;
        try {
            importer = new ImportSVG(svg, inputs, outputs);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        // importer.setPinDescriptions(inputs, outputs);
        this.pins = importer.getPins();
        this.fragments = importer.getFragments();
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        if (fragments != null) {
            for (SVGFragment f : fragments) {
                if (f != null && f.getDrawables() != null) {
                    for (SVGDrawable d : f.getDrawables()) {
                        if (d != null) {
                            d.draw(graphic);
                        }
                    }
                }
            }
        }

    }

    @Override
    public Pins getPins() {
        return pins;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        // TODO Auto-generated method stub
        return null;
    }

}
