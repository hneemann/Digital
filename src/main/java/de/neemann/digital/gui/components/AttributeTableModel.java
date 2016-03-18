package de.neemann.digital.gui.components;

import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.PartAttributes;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class AttributeTableModel implements TableModel {
    private final ArrayList<AttributeKey> list;
    private final PartAttributes partAttributes;

    public AttributeTableModel(ArrayList<AttributeKey> list, PartAttributes partAttributes) {
        this.list = list;
        this.partAttributes = partAttributes;
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) return "Name";
        else return "Value";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AttributeKey attributeKey = list.get(rowIndex);
        if (columnIndex == 0)
            return attributeKey.getName();
        else
            return partAttributes.get(attributeKey);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            AttributeKey attributeKey = list.get(rowIndex);
            Object newValue = createValue(aValue, attributeKey);
            if (newValue != null)
                partAttributes.set(attributeKey, newValue);
        }
    }

    private Object createValue(Object aValue, AttributeKey attributeKey) {
        if (attributeKey.getValueClass() == Integer.class)
            return Integer.parseInt(aValue.toString());
        else if (attributeKey.getValueClass() == String.class)
            return aValue.toString();
        if (attributeKey.getValueClass() == Double.class)
            return Double.parseDouble(aValue.toString());
        return null;
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
    }
}
