package de.neemann.digital.gui.components.graphics.svgimport;

import java.awt.Point;
import java.util.ArrayList;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.gui.components.AttributeDialog;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.Editor.EditorParseException;

public class CustomAttributeDialog extends AttributeDialog {
	private static final long serialVersionUID = -2850674703234321173L;
	private CircuitComponent circuitComponent;
	private PreviewComponent prev;

	public CustomAttributeDialog(CircuitComponent circuitComponent, Point p,
			@SuppressWarnings("rawtypes") ArrayList<Key> list, ElementAttributes elementAttributes,PreviewComponent prev) {
		super(circuitComponent, p, list, elementAttributes);
		this.circuitComponent=circuitComponent;
		this.prev=prev;
	}

	@Override
	public void fireOk() throws EditorParseException {
		super.fireOk();
		for (int i = 0; i < circuitComponent.getCircuit().getElements().size(); i++) {
			if (circuitComponent.getCircuit().getElements().get(i) == getVisualElement()) {
				circuitComponent.getCircuit().getElements().set(i,prev.getElement());
				break;
			}
		}
		circuitComponent.repaintNeeded();
	}
}
