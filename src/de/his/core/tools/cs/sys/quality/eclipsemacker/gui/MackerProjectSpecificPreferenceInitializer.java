package de.his.core.tools.cs.sys.quality.eclipsemacker.gui;

import org.eclipse.jface.preference.IPreferenceStore;

import de.his.core.tools.cs.sys.quality.eclipsemacker.Activator;

/**
 * Diese Klasse laedt globale Einstellungen beim Pluginstart.
 * @author Bender
 * @SuppressWarnings("unused")
 */
public class MackerProjectSpecificPreferenceInitializer extends
		org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer {

    /**
     * Create a new
     */
	public MackerProjectSpecificPreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		store.setDefault(PreferenceConstants.RULES_PATH, "/rules/test/loc/");
		
		store.setDefault(PreferenceConstants.RUN_ON_FULL_BUILD, false);
		store.setDefault(PreferenceConstants.RUN_ON_INCREMENTAL_BUILD, true);
		store.setDefault(PreferenceConstants.CHECK_CONTENT, false);
		store.setDefault(PreferenceConstants.CHOICE, "DEFAULT");

	}

}
