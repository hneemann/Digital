package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.Value;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;
import static de.neemann.digital.draw.shapes.OutputShape.OUT_SIZE;
import static de.neemann.digital.draw.shapes.OutputShape.RADL;

/**
 * The InOut shape
 */
public class InOutShape implements Shape {

    static final int CENTER_IDX = OUT_SIZE + 1;
    static final int HALF_CENTER_IDX = CENTER_IDX / 2;

    static final Vector LATEX_RAD = new Vector(Style.MAXLINETHICK, Style.MAXLINETHICK);

    private final String label;
    private final PinDescriptions inputs;
    private final IntFormat format;
    private IOState ioState;
    private Value value;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public InOutShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        String pinNumber = attr.get(Keys.PINNUMBER);
        if (pinNumber.length() == 0)
            this.label = attr.getLabel();
        else
            this.label = attr.getLabel() + " (" + pinNumber + ")";

        format = attr.get(Keys.INT_FORMAT);
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), inputs.get(0)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        this.ioState = ioState;
        ioState.getInput(0).addObserverToValue(guiObserver);
        return null;
    }

    @Override
    public void readObservableValues() {
        if (ioState != null)
            value = ioState.getInput(0).getCopy();
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        if (graphic.isFlagSet(Graphic.Flag.smallIO)) {
            Vector center = new Vector(LATEX_RAD.x, 0);
            graphic.drawCircle(center.sub(LATEX_RAD), center.add(LATEX_RAD), Style.NORMAL);
            Vector textPos = new Vector(SIZE2 + LATEX_RAD.x, 0);
            graphic.drawText(textPos, label, Orientation.LEFTCENTER, Style.INOUT);
        } else {
            Style style = Style.NORMAL;
            // 如果 dataBits > 1，那么就显示位数在连接的 wire 上
            if (value != null) {
                style = Style.getWireStyle(value);
                if (value.getBits() > 1) {
                    Vector textPos =  new Vector(1 + OUT_SIZE, -4 - OUT_SIZE);
                    graphic.drawText(textPos, format.formatToView(value), Orientation.CENTERBOTTOM, Style.NORMAL);
                }
            }

            Vector center = new Vector(CENTER_IDX, 0);
            graphic.drawPolygon(
                    new Polygon(true)
                            .add(HALF_CENTER_IDX, 0)
                            .add(CENTER_IDX, -HALF_CENTER_IDX)
                            .add(CENTER_IDX + HALF_CENTER_IDX, 0)
                            .add(CENTER_IDX, HALF_CENTER_IDX), style
            );
            graphic.drawCircle(center.sub(RADL), center.add(RADL), Style.NORMAL);
            Vector textPos = new Vector(OUT_SIZE * 3, 0);
            graphic.drawText(textPos, label, Orientation.LEFTCENTER, Style.INOUT);
        }
    }
}