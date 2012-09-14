package de.his.core.tools.cs.sys.quality.eclipsemacker.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.his.core.tools.cs.sys.quality.eclipsemacker.Activator;

/**
 * Class used to initialize default preference values.
 */
public class MackerGlobalPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
    @Override
    public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(MackerGlobalPreferenceConstants.P_PROJECT_WITH_RULES, "webapps");
        store.setDefault(MackerGlobalPreferenceConstants.P_FOLDER_IN_PROJECT_WITH_RULES, "qisserver/WEB-INF/internal/macker/rules");
	}

}
