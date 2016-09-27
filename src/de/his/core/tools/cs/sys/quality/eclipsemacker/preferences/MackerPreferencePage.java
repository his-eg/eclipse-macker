package de.his.core.tools.cs.sys.quality.eclipsemacker.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.his.core.tools.cs.sys.quality.eclipsemacker.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class MackerPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

    /**
     * Create Macker Preference Page
     */
	public MackerPreferencePage() {
		super(GRID);
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
    @Override
    public void createFieldEditors() {
        addGlobalRuleLocationConfiguration();
        addField(new BooleanFieldEditor(MackerGlobalPreferenceConstants.DEBUG_SWITCH, "Debug to console", getFieldEditorParent()));
	}

	private void addGlobalRuleLocationConfiguration() {
		ProjectNameStringFieldEditor ruleProjectNameEditor = new ProjectNameStringFieldEditor(MackerGlobalPreferenceConstants.P_PROJECT_WITH_RULES, "Project with Rules:", getFieldEditorParent());
		ruleProjectNameEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
		addField(ruleProjectNameEditor);
        addField(new StringFieldEditor(MackerGlobalPreferenceConstants.P_FOLDER_IN_PROJECT_WITH_RULES, "Folder with Rules:", getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Preferences for the Macker integration");
	}
	
}