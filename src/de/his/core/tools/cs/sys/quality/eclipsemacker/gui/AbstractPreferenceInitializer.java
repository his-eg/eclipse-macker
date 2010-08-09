package de.his.core.tools.cs.sys.quality.eclipsemacker.gui;

import org.eclipse.jface.preference.IPreferenceStore;

import de.his.core.tools.cs.sys.quality.eclipsemacker.Activator;

public class AbstractPreferenceInitializer extends
		org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer {

	public AbstractPreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.RULES_PATH, "D:/Tomcat/layering-rules.xml");
		//store.setDefault(PreferenceConstants.MODULARITY_RULES_PATH, "D:/Tomcat/modularity-rules.xml");
		
		store.setDefault(PreferenceConstants.CHOICE, "DEFAULT");

	}

}
