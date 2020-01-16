package de.neemann.digital.plugin;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.memory.DataField;

/**
 * Public function class of plugin
 */
public class PluginFun {
    /**
     * Create a new data field
     * @param size the size of data field
     * @return DataField
     */
    public DataField createDataField(int size) {
        return new DataField(size);
    }

    /**
     * Create the output port
     * @param name the name of output
     * @param dataBits the data bits of output
     * @param description the description of pin
     * @param isSetBD if setting the bid directional
     * @return ObservableValue
     */
    public ObservableValue createOutput(String name, int dataBits, ElementTypeDescription description, Boolean isSetBD) {
        ObservableValue observableValue = new ObservableValue(name, dataBits)
                .setToHighZ()
                .setPinDescription(description);
        if (isSetBD)
            observableValue.setBidirectional();
        return observableValue;
    }
}
