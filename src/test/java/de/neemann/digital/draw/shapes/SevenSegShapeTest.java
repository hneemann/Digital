/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.io.CommonConnectionType;
import de.neemann.digital.draw.elements.IOState;
import junit.framework.TestCase;

public class SevenSegShapeTest extends TestCase {

    public void testCommonCathode() {
        ElementAttributes attr = new ElementAttributes()
                .set(Keys.COMMON_CONNECTION, true)
                .set(Keys.COMMON_CONNECTION_TYPE, CommonConnectionType.cathode);

        ObservableValue a = new ObservableValue("a", 1);
        ObservableValue b = new ObservableValue("b", 1);
        ObservableValue c = new ObservableValue("c", 1);
        ObservableValue d = new ObservableValue("d", 1);
        ObservableValue e = new ObservableValue("e", 1);
        ObservableValue f = new ObservableValue("f", 1);
        ObservableValue g = new ObservableValue("g", 1);
        ObservableValue dp = new ObservableValue("dp", 1);
        ObservableValue cc = new ObservableValue("cc", 1);
        final ObservableValues observableValues = new ObservableValues(a, b, c, d, e, f, g, dp, cc);
        PinDescriptions inputs = new PinDescriptions(observableValues);
        SevenSegShape ss = new SevenSegShape(attr, inputs, new PinDescriptions());
        IOState state = new IOState(observableValues, null, null);
        ss.applyStateMonitor(state);

        checkValue(false, ss, 0, a, 0, 1, cc, 0, 1);
        checkValue(false, ss, 0, a, 0, 1, cc, 1, 1);
        checkValue(false, ss, 0, a, 1, 1, cc, 0, 1);
        checkValue(false, ss, 0, a, 1, 1, cc, 1, 1);

        checkValue(false, ss, 0, a, 0, 1, cc, 0, 0);
        checkValue(false, ss, 0, a, 0, 1, cc, 1, 0);
        checkValue(false, ss, 0, a, 1, 1, cc, 0, 0);
        checkValue(false, ss, 0, a, 1, 1, cc, 1, 0);

        checkValue(false, ss, 0, a, 0, 0, cc, 0, 1);
        checkValue(false, ss, 0, a, 0, 0, cc, 1, 1);
        checkValue(false, ss, 0, a, 1, 0, cc, 0, 1);
        checkValue(false, ss, 0, a, 1, 0, cc, 1, 1);

        checkValue(false, ss, 0, a, 0, 0, cc, 0, 0);
        checkValue(false, ss, 0, a, 0, 0, cc, 1, 0);
        checkValue(true, ss, 0, a, 1, 0, cc, 0, 0);
        checkValue(false, ss, 0, a, 1, 0, cc, 1, 0);

        checkValue(false, ss, 1, b, 0, 1, cc, 0, 1);
        checkValue(false, ss, 1, b, 0, 1, cc, 1, 1);
        checkValue(false, ss, 1, b, 1, 1, cc, 0, 1);
        checkValue(false, ss, 1, b, 1, 1, cc, 1, 1);

        checkValue(false, ss, 1, b, 0, 1, cc, 0, 0);
        checkValue(false, ss, 1, b, 0, 1, cc, 1, 0);
        checkValue(false, ss, 1, b, 1, 1, cc, 0, 0);
        checkValue(false, ss, 1, b, 1, 1, cc, 1, 0);

        checkValue(false, ss, 1, b, 0, 0, cc, 0, 1);
        checkValue(false, ss, 1, b, 0, 0, cc, 1, 1);
        checkValue(false, ss, 1, b, 1, 0, cc, 0, 1);
        checkValue(false, ss, 1, b, 1, 0, cc, 1, 1);

        checkValue(false, ss, 1, b, 0, 0, cc, 0, 0);
        checkValue(false, ss, 1, b, 0, 0, cc, 1, 0);
        checkValue(true, ss, 1, b, 1, 0, cc, 0, 0);
        checkValue(false, ss, 1, b, 1, 0, cc, 1, 0);
    }

    public void testCommonAnode() {
        ElementAttributes attr = new ElementAttributes()
                .set(Keys.COMMON_CONNECTION, true)
                .set(Keys.COMMON_CONNECTION_TYPE, CommonConnectionType.anode);

        ObservableValue a = new ObservableValue("a", 1);
        ObservableValue b = new ObservableValue("b", 1);
        ObservableValue c = new ObservableValue("c", 1);
        ObservableValue d = new ObservableValue("d", 1);
        ObservableValue e = new ObservableValue("e", 1);
        ObservableValue f = new ObservableValue("f", 1);
        ObservableValue g = new ObservableValue("g", 1);
        ObservableValue dp = new ObservableValue("dp", 1);
        ObservableValue cc = new ObservableValue("ca", 1);
        final ObservableValues observableValues = new ObservableValues(a, b, c, d, e, f, g, dp, cc);
        PinDescriptions inputs = new PinDescriptions(observableValues);
        SevenSegShape ss = new SevenSegShape(attr, inputs, new PinDescriptions());
        IOState state = new IOState(observableValues, null, null);
        ss.applyStateMonitor(state);

        checkValue(false, ss, 0, a, 0, 1, cc, 0, 1);
        checkValue(false, ss, 0, a, 0, 1, cc, 1, 1);
        checkValue(false, ss, 0, a, 1, 1, cc, 0, 1);
        checkValue(false, ss, 0, a, 1, 1, cc, 1, 1);

        checkValue(false, ss, 0, a, 0, 1, cc, 0, 0);
        checkValue(false, ss, 0, a, 0, 1, cc, 1, 0);
        checkValue(false, ss, 0, a, 1, 1, cc, 0, 0);
        checkValue(false, ss, 0, a, 1, 1, cc, 1, 0);

        checkValue(false, ss, 0, a, 0, 0, cc, 0, 1);
        checkValue(false, ss, 0, a, 0, 0, cc, 1, 1);
        checkValue(false, ss, 0, a, 1, 0, cc, 0, 1);
        checkValue(false, ss, 0, a, 1, 0, cc, 1, 1);

        checkValue(false, ss, 0, a, 0, 0, cc, 0, 0);
        checkValue(true, ss, 0, a, 0, 0, cc, 1, 0);
        checkValue(false, ss, 0, a, 1, 0, cc, 0, 0);
        checkValue(false, ss, 0, a, 1, 0, cc, 1, 0);
    }

    private void checkValue(boolean expected, SevenSegShape ss, int i, ObservableValue a, int av, int ahz, ObservableValue b, int bv, int bhz) {
        a.set(av, ahz);
        b.set(bv, bhz);
        ss.readObservableValues();
        assertEquals(expected, ss.getStyle(i));
    }
}