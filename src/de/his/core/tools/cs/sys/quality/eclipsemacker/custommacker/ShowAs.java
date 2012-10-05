package de.his.core.tools.cs.sys.quality.eclipsemacker.custommacker;

/**
 * Definiert wie Macker-Events anhand
 * von Eclipse-Markern angezeigt werden sollen.
 * @author Bender
 */

public enum ShowAs {
    /**
     * propagate message type
     */
	DEFAULT,
    /**
     * Display as info
     */
	INFO,
    /**
     * Display as warning
     */
	WARNING,
    /**
     * Display as error
     */
	ERROR
}