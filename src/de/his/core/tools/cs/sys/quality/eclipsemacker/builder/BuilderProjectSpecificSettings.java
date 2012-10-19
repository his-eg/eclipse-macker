package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.JavaCore;

import de.his.core.tools.cs.sys.quality.eclipsemacker.gui.PreferenceConstants;

/**
 * Configures the builder with project specific settings
 * @author keunecke
 *
 */
public class BuilderProjectSpecificSettings extends AbstractBuilderSettings {

    @Override
    public AbstractBuilderSettings initSettings() {
        this.setDefaultM(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.DEFAULT))));
        this.setError(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.ERROR))));
        this.setRunOnFullBuild(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_FULL_BUILD))));
        this.setRunOnIncBuild(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_INCREMENTAL_BUILD))));
        this.setFilterContent(getPersistentProperty(new QualifiedName("", PreferenceConstants.CLASSPATH_FILTER)));
        this.setUseFilter(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_CLASSPATH_FILTER))));
        this.setFilterSourceContent(getPersistentProperty(new QualifiedName("", PreferenceConstants.SOURCE_FILTER)));
        this.setUseSourceFilter(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_SOURCE_FILTER))));
        this.setCheckContent(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.CHECK_CONTENT))));
        String rulesDir = getPersistentProperty(new QualifiedName("", PreferenceConstants.RULES_PATH));
        if (rulesDir == null || rulesDir.isEmpty()) {
            rulesDir = SETTINGS_MACKER;
        }
        String rulesProjectName = "webapps";
        String configuredProject = getPersistentProperty(new QualifiedName("", PreferenceConstants.RULES_PROJECT));
        if (configuredProject != null) {
            rulesProjectName = configuredProject;
        }

        this.ruleProject = rulesProjectName;
        this.setRulesDir(rulesDir);
        this.setjProject(JavaCore.create(project));
        //rule files instanziieren
        setRulesFromDirectory();
        this.getSources();
        return this;
    }

}
