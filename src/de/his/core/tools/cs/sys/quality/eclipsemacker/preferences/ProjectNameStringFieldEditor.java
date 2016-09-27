package de.his.core.tools.cs.sys.quality.eclipsemacker.preferences;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
/**
 * Extended StringFieldEditor to validate entered value against existing project names
 * 
 * @author markus
 */
public class ProjectNameStringFieldEditor extends StringFieldEditor {
	
	ProjectNameStringFieldEditor(String preferenceKey, String label, Composite parent) {
		super(preferenceKey, label, parent);
	}

	@Override
	protected boolean doCheckState() {
		List<IProject> projects = Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());
		String projectName = this.getStringValue();
		
		for (IProject project : projects) {
			if(project.getName().equals(projectName)) {
				return true;
			}
		}
		this.setErrorMessage(String.format("The project %s does not exist in the workspace.", projectName));
		return false;
	}

}
