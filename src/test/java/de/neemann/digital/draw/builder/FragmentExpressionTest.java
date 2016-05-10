package de.neemann.digital.draw.builder;

import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class FragmentExpressionTest extends TestCase {

    public void testCalcBackOffset1() throws Exception {
        assertEquals(0, FragmentExpression.calcBackOffset(1, 0));

        assertEquals(1, FragmentExpression.calcBackOffset(2, 0));
        assertEquals(1, FragmentExpression.calcBackOffset(2, 1));

        assertEquals(1, FragmentExpression.calcBackOffset(3, 0));
        assertEquals(0, FragmentExpression.calcBackOffset(3, 1));
        assertEquals(1, FragmentExpression.calcBackOffset(3, 2));

        assertEquals(1, FragmentExpression.calcBackOffset(4, 0));
        assertEquals(2, FragmentExpression.calcBackOffset(4, 1));
        assertEquals(2, FragmentExpression.calcBackOffset(4, 2));
        assertEquals(1, FragmentExpression.calcBackOffset(4, 3));

        assertEquals(1, FragmentExpression.calcBackOffset(5, 0));
        assertEquals(2, FragmentExpression.calcBackOffset(5, 1));
        assertEquals(0, FragmentExpression.calcBackOffset(5, 2));
        assertEquals(2, FragmentExpression.calcBackOffset(5, 3));
        assertEquals(1, FragmentExpression.calcBackOffset(5, 4));

        assertEquals(1, FragmentExpression.calcBackOffset(6, 0));
        assertEquals(2, FragmentExpression.calcBackOffset(6, 1));
        assertEquals(3, FragmentExpression.calcBackOffset(6, 2));
        assertEquals(3, FragmentExpression.calcBackOffset(6, 3));
        assertEquals(2, FragmentExpression.calcBackOffset(6, 4));
        assertEquals(1, FragmentExpression.calcBackOffset(6, 5));

        assertEquals(1, FragmentExpression.calcBackOffset(7, 0));
        assertEquals(2, FragmentExpression.calcBackOffset(7, 1));
        assertEquals(3, FragmentExpression.calcBackOffset(7, 2));
        assertEquals(0, FragmentExpression.calcBackOffset(7, 3));
        assertEquals(3, FragmentExpression.calcBackOffset(7, 4));
        assertEquals(2, FragmentExpression.calcBackOffset(7, 5));
        assertEquals(1, FragmentExpression.calcBackOffset(7, 6));

    }

}