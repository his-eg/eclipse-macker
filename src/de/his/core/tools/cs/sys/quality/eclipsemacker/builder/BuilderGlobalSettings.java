/**
 *
 */
package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.util.LinkedHashMap;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;

import de.his.core.tools.cs.sys.quality.eclipsemacker.Activator;
import de.his.core.tools.cs.sys.quality.eclipsemacker.gui.PreferenceConstants;
import de.his.core.tools.cs.sys.quality.eclipsemacker.preferences.MackerGlobalPreferenceConstants;

/**
 *
 * Diese Klasse enthaelt alle definierten Projekteinstellungen
 * die in der Property Page vorgenommen wurden.
 * Zudem
 * @author Bender
 */
public class BuilderGlobalSettings extends AbstractBuilderSettings {

	/**
     * Create a new BuilderSettings instance
     */
    public BuilderGlobalSettings () {
        //
    }


    /**
	 * HIS Spezifische settings setzen.
	 * @return builderSettings Objekt.
	 */
    @Override
    public AbstractBuilderSettings initSettings() {
    	LinkedHashMap<String, String> map = loadDispatcherProp();
    	try {
    		this.setDefaultM(Boolean.parseBoolean(map.get(PreferenceConstants.DEFAULT)));
        	this.setError(Boolean.parseBoolean(map.get(PreferenceConstants.ERROR)));
        	this.setRunOnFullBuild(Boolean.parseBoolean(map.get(PreferenceConstants.RUN_ON_FULL_BUILD)));
        	this.setRunOnIncBuild(Boolean.parseBoolean(map.get(PreferenceConstants.RUN_ON_INCREMENTAL_BUILD)));
        	this.setFilterContent(map.get(PreferenceConstants.CLASSPATH_FILTER).replace(", ", "\t"));
        	this.setUseFilter(Boolean.parseBoolean(map.get(PreferenceConstants.USE_CLASSPATH_FILTER)));
        	this.setFilterSourceContent(map.get(PreferenceConstants.SOURCE_FILTER).replace(", ", "\t"));
        	this.setUseSourceFilter(Boolean.parseBoolean(map.get(PreferenceConstants.USE_SOURCE_FILTER)));
        	this.setCheckContent(Boolean.parseBoolean(map.get(PreferenceConstants.CHECK_CONTENT)));
            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            String rulesDir = store.getString(MackerGlobalPreferenceConstants.P_FOLDER_IN_PROJECT_WITH_RULES);
            if (rulesDir == null || rulesDir.isEmpty()) {
                // if nothing is configured assume .settings/macker
                rulesDir = SETTINGS_MACKER;
            }
            this.setRulesDir(rulesDir);
            String rulesProject = store.getString(MackerGlobalPreferenceConstants.P_PROJECT_WITH_RULES);
            if (rulesProject == null || rulesProject.isEmpty()) {
                this.ruleProject = project.getName();
            } else {
                this.ruleProject = rulesProject;
            }
        	this.setjProject(JavaCore.create(project));
        	//rule files instanziieren
        	setRulesFromDirectory();
        	this.getSources();
    	} catch (NullPointerException e) {
    		e.printStackTrace();
    	}

    	return this;
    }


}
