package de.his.core.tools.cs.sys.quality.eclipsemacker.gui;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

import de.his.core.tools.cs.sys.quality.eclipsemacker.Activator;

/**
 * @author Bender
 *
 */


	/**
	 * GUI um Einstellungen an dem Builder vorzunehmen, ueber
	 * window->preferences->Macker Settings
	 * 
	 * @author Bender
	 */

	public class Preferences extends FieldEditorPreferencePage implements
			IWorkbenchPreferencePage {

		public Preferences() {
			super(GRID);

		}

		public void createFieldEditors() {
//			addField(new DirectoryFieldEditor(PreferenceConstants.RULES_PATH, "&Macker Rules Directory:",
//					getFieldEditorParent()));
			
			
			addField(new StringFieldEditor(PreferenceConstants.RULES_PATH, "&Macker Rules Directory:",
					getFieldEditorParent()));
			
			addField(new BooleanFieldEditor(PreferenceConstants.RUN_ON_FULL_BUILD,
					"&Run Macker on Full Build", getFieldEditorParent()));
			
			addField(new BooleanFieldEditor(PreferenceConstants.RUN_ON_INCREMENTAL_BUILD,
					"&Run Macker on Incremental Build", getFieldEditorParent()));
			
			addField(new BooleanFieldEditor(PreferenceConstants.CHECK_CONTENT,
					"&Check Class Content", getFieldEditorParent()));
			
			addField(new RadioGroupFieldEditor("CHOICE",
					"Show Macker Events as", 4,
					new String[][] { { "&Info", "INFO" },
							{ "&Warning", "WARNING" }, { "&Error", "ERROR" }, { "&Default", "DEFAULT" } }, getFieldEditorParent()));
			
		}

		
		@Override
		public void init(IWorkbench workbench) {
			setPreferenceStore(Activator.getDefault().getPreferenceStore());
			setDescription("Macker Preference Page");
		}
	}

