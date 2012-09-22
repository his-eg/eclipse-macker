package de.his.core.tools.cs.sys.quality.eclipsemacker.gui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.his.core.tools.cs.sys.quality.eclipsemacker.Activator;


/**
 * GUI um Einstellungen an dem Builder vorzunehmen, ueber
 * window->preferences->Macker Settings.
 * 
 * Wurde durch eine Property Page ersetzt um fuer jedes Projekt
 * im Workspace individuelle Einstellungen vornehmen zu koennen.
 * 
 * @author Bender
 * @SuppressWarnings("unused")
 */

public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    /**
     * Create a new Preferences
     */
    public Preferences() {
        super(GRID);


    }

    @Override
    public void createFieldEditors() {

        addField(new StringFieldEditor(PreferenceConstants.RULES_PATH, "&Macker Rules Directory:", getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.RUN_ON_FULL_BUILD, "&Run Macker on Full Build", getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.RUN_ON_INCREMENTAL_BUILD, "&Run Macker on Incremental Build", getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.CHECK_CONTENT, "&Check Class Content", getFieldEditorParent()));

        addField(new RadioGroupFieldEditor("CHOICE", "Show Macker Events as", 4, new String[][] { { "&Info", "INFO" }, { "&Warning", "WARNING" }, { "&Error", "ERROR" }, { "&Default", "DEFAULT" } }, getFieldEditorParent()));

    }


    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Project specific Macker preferences");
    }
}
