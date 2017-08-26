package de.neemann.digital.hdl.vhdl.boards;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.gui.components.data.DummyElement;

/**
 * Provides additional information for a specific board
 */
public final class BoardProvider {

    private static final class InstanceHolder {
        static final BoardProvider INSTANCE = new BoardProvider();
    }

    /**
     * @return the BoardProvider instance
     */
    public static BoardProvider getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private BoardProvider() {
    }

    /**
     * Returns a spscific board
     *
     * @param circuit the circuit
     * @return the board or null
     */
    public BoardInterface getBoard(Circuit circuit) {
        String board = null;
        for (VisualElement element : circuit.getElements()) {
            if (element.equalsDescription(DummyElement.TEXTDESCRIPTION)) {
                String text = element.getElementAttributes().get(Keys.DESCRIPTION).toLowerCase();
                if (text.startsWith("board:")) {
                    board = text.substring(6).trim();
                }
            }
        }

        if (board == null)
            return null;

        if (board.equals("basys3"))
            return new Vivado("LVCMOS33", "W5", 10, new ClockIntegratorARTIX7(10));

        return null;
    }
}
