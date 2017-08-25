package de.neemann.digital.hdl.model;

/**
 * Used to utilize the board specific clock resources
 */
public interface ClockIntegrator {

    /**
     * Modifies the model to integrate the clock sources
     *
     * @param model the model
     * @throws HDLException HDLException
     */
    void integrateClocks(HDLModel model) throws HDLException;

}

