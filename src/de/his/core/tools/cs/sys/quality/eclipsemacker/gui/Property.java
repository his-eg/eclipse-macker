/**
 * 
 */
package de.his.core.tools.cs.sys.quality.eclipsemacker.gui;




import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.swt.SWT;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author Bender
 *
 */
public class Property extends PropertyPage {

	private static final String RULES_DIR = "test/rules/loc";
	private static final String INC_BUILD = "true";
	private static final String FULL_BUILD = "false";
	private static final String CHECK_CONTENT = "false";

	private static final int TEXT_FIELD_WIDTH = 50;

	private Text rulesDir;
	private Button incBuild;
	private Button fullBuild;
	private Button checkContent;
	//private RadioGroupFieldEditor ra;
	
	private Button warning, error, defaultM;
	/**
	 * Constructor for SamplePropertyPage.
	 */
	public Property() {
		super();
	}




	private void addSecondSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// Label for owner field
		Label ownerLabel = new Label(composite, SWT.NONE);
		ownerLabel.setText("Macker Propertys");

		// Owner text field
		rulesDir = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		rulesDir.setLayoutData(gd);

		incBuild = new Button(composite, SWT.CHECK);
		incBuild.setText("Run on Incremental Build");
		
		fullBuild = new Button(composite, SWT.CHECK);
		fullBuild.setText("Run on Full Build");
		
		checkContent = new Button(composite, SWT.CHECK);
		checkContent.setText("Check Content");
		
		Composite radioGroup = new Composite(parent, SWT.NONE);
		radioGroup.setLayout(new GridLayout());
		radioGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		  
		warning = createRadioButton(radioGroup, "Warning");
		error = createRadioButton(radioGroup, "Error");
		defaultM = createRadioButton(radioGroup, "Default");

		// Populate owner text field
		try {
			IResource resource = ((IJavaProject) getElement()).getResource();
			
			String owner = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.RULES_PATH));
			String incB = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_INCREMENTAL_BUILD));
			String fullB = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_FULL_BUILD));
			String checkC = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.CHECK_CONTENT));
			//String rA = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.CHOICE));

			
			
			checkContent.setSelection((checkC != null) ? new Boolean(checkC) : new Boolean(CHECK_CONTENT));
			fullBuild.setSelection((fullB != null) ? new Boolean(fullB) : new Boolean(FULL_BUILD));
			incBuild.setSelection((incB != null) ? new Boolean(incB) : new Boolean(INC_BUILD));
			rulesDir.setText((owner != null) ? owner : RULES_DIR);
			
			
			
			
		} catch (CoreException e) {
			rulesDir.setText(RULES_DIR);
		}
	}

	
	private Button createRadioButton(Composite parent, String label) {
		  final Button button = new Button(parent, SWT.RADIO);
		  button.setText(label);
		

		  return button;
		}
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

	
		
		addSecondSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {
		super.performDefaults();
		// Populate the owner text field with the default value
		rulesDir.setText(RULES_DIR);
	}
	
	public boolean performOk() {
		// store the value in the owner text field
		try {
			IResource resource = ((IJavaProject) getElement()).getResource();
			
			resource.setPersistentProperty(
				new QualifiedName("", PreferenceConstants.RULES_PATH),
				rulesDir.getText());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.RUN_ON_INCREMENTAL_BUILD),
					new Boolean(incBuild.getSelection()).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.RUN_ON_FULL_BUILD),
					new Boolean(fullBuild.getSelection()).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.CHECK_CONTENT),
					new Boolean(checkContent.getSelection()).toString());
			
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

}
