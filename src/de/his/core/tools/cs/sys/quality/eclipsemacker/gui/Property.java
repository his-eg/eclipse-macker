/**
 * 
 */
package de.his.core.tools.cs.sys.quality.eclipsemacker.gui;




import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author Bender
 *
 */
public class Property extends PropertyPage {
	/*
	 * Default Values
	 */
	private static final String RULES_DIR = "test/rules/loc";
	private static final String FILTER = "de/his/appclient/\tde/his/appserver/\tde/his/core/";
	private static final boolean INC_BUILD = true;
	private static final boolean FULL_BUILD = false;
	private static final boolean CHECK_CONTENT = false;
	private static final boolean WARNING = false;
	private static final boolean ERROR = false;
	private static final boolean DEFAULT = true;
	private static final boolean USE_FILTER = true;
	
	
	private static final int TEXT_FIELD_WIDTH = 50;

	/*
	 * Gui Komponenten
	 */
	private Text rulesDir;
	private Button incBuild;
	private Button fullBuild;
	private Button checkContent;
	private Composite radioGroup;
	private Button warning;
	private Button error;
	private Button defaultM;
	private Button useFilter;
	private List list; 
	
	/**
	 * Constructor.
	 */
	public Property() {
		super();
	}



	
	/**
	 * GUI der Propertypage
	 * @param parent
	 */

	private void addSection(Composite parent) {
		Label headerLabel = new Label(parent, SWT.NONE);
		headerLabel.setText("Macker Propertys");

		Composite first = new Composite(parent, SWT.None);
		
		GridLayout r = new GridLayout();
		r.numColumns = 2;
		first.setLayout(r);
		Label ruleLabel = new Label(first, SWT.NONE);
		ruleLabel.setText("Macker Rules Directory:");
		rulesDir = new Text(first, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(60);
		rulesDir.setLayoutData(gd);
		

		Composite f = new Composite(parent, SWT.None);
		
		GridLayout rf = new GridLayout();
		rf.numColumns = 4;
		f.setLayout(rf);
		list = new List(f, SWT.V_SCROLL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;

		list.setLayoutData(gridData);
		Composite second = new Composite(f, SWT.None);
		
		GridLayout rz = new GridLayout();
		rz.numColumns = 3;
		second.setLayout(rz);
		
		Button buttonR = new Button(f, SWT.PUSH);
		buttonR.setText("Remove/");
		
		Button button = new Button(f, SWT.PUSH);
		button.setText("Add classpath");

		final Text path = new Text(f, SWT.SINGLE | SWT.BORDER);
		GridData gdz = new GridData(GridData.FILL_HORIZONTAL);
		
		gdz.widthHint = convertWidthInCharsToPixels(50);
		path.setLayoutData(gdz);
		
	    button.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		list.add(path.getText());
	    		getListContent();
	          }
	    });
		
	    buttonR.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		if (list.getSelectionIndex() > -1) {
	    			list.remove(list.getSelectionIndex());
	    		}
	          }
	    });
		
		
		
		Composite check = new Composite(parent, SWT.None);
		check.setLayout(new GridLayout());
		check.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		useFilter = new Button(check, SWT.CHECK);
		useFilter.setText("Use Classpath Filter");
		
		incBuild = new Button(check, SWT.CHECK);
		incBuild.setText("Run on Incremental Build");
		
		fullBuild = new Button(check, SWT.CHECK);
		fullBuild.setText("Run on Full Build");
		
		checkContent = new Button(check, SWT.CHECK);
		checkContent.setText("Check Content");

		Label events = new Label(parent, SWT.None);
		events.setText("Show Macker Events as");
		
		radioGroup = new Composite(parent, SWT.NONE);
		GridLayout g = new GridLayout();
		g.numColumns = 3;
		radioGroup.setLayout(g);
		radioGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		warning = createRadioButton(radioGroup, "Warning");
		error = createRadioButton(radioGroup, "Error");
		defaultM = createRadioButton(radioGroup, "Default");

		// Lade aktuelle Einstellungen
		try {
			IResource resource = ((IJavaProject) getElement()).getResource();
			
			String owner = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.RULES_PATH));
			String incB = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_INCREMENTAL_BUILD));
			String fullB = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_FULL_BUILD));
			String checkC = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.CHECK_CONTENT));
			String filter = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.FILTER));
			String useF = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_FILTER));
			
			String rW = resource.getPersistentProperty(new QualifiedName("", "WARNING"));
			String rE = resource.getPersistentProperty(new QualifiedName("", "ERROR"));
			String rD = resource.getPersistentProperty(new QualifiedName("", "DEFAULT"));
			
			if(filter != null) {
				setListContent(filter);
			} else {
				setListContent(FILTER);
			}
			
			useFilter.setSelection((useF != null) ? new Boolean(useF) : USE_FILTER);
			warning.setSelection((rW != null) ? new Boolean(rW) : WARNING);
			error.setSelection((rE != null) ? new Boolean(rE) : ERROR);
			defaultM.setSelection((rD != null) ? new Boolean(rD) : DEFAULT);
			
			checkContent.setSelection((checkC != null) ? new Boolean(checkC) : CHECK_CONTENT);
			fullBuild.setSelection((fullB != null) ? new Boolean(fullB) : FULL_BUILD);
			incBuild.setSelection((incB != null) ? new Boolean(incB) : INC_BUILD);
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

		addSection(composite);
		
		return composite;
	}

	/**
	 * Default Werte setzen
	 */
	protected void performDefaults() {
		super.performDefaults();
		
		rulesDir.setText(RULES_DIR);
		checkContent.setSelection(CHECK_CONTENT);
		fullBuild.setSelection(FULL_BUILD);
		incBuild.setSelection(INC_BUILD);
		//TODO 
		list.setItems(new String[]{"de/his/appclient/", 
				"de/his/appserver/", "de/his/core/"});
		
		warning.setSelection(WARNING);
		defaultM.setSelection(DEFAULT);
		error.setSelection(ERROR);
		useFilter.setSelection(USE_FILTER);
	}
	
	/**
	 * Projekt Einstellungen dauerhaft speichern.
	 */
	public boolean performOk() {
		// store the values
		try {
			IResource resource = ((IJavaProject) getElement()).getResource();
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.USE_FILTER),
					new Boolean(useFilter.getSelection()).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.DEFAULT),
					new Boolean(defaultM.getSelection()).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.ERROR),
					new Boolean(error.getSelection()).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.WARNING),
					new Boolean(warning.getSelection()).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.FILTER),
					getListContent());
			
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
	
	
	/**
	 * Setzt die Werte des Classpath Filters
	 * @param content
	 */
	private void setListContent(String content) {
		StringTokenizer st = new StringTokenizer(content, "\t");
	     while (st.hasMoreTokens()) { 
	       list.add(st.nextToken());
	     }
	}
	/**
	 * Gibt die aktuellen Werte des Classpath filters zurueck.
	 * @return
	 */
	private String getListContent() {
		String content = "";
		for (int i = 0; i < list.getItems().length; i++) {
			if (!list.getItem(i).equals("")) {
				content += list.getItem(i)+ "\t";
			}
		}
		return content;
	}
	
	/**
	 * Setzt beim ersten Start eines Projektes mit dem Plugin
	 * die Defaultwerte.
	 * @param resource
	 * @return
	 */
	public boolean init(IResource resource) {
		
		try {
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.USE_FILTER),
					new Boolean(USE_FILTER).toString());

			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.FILTER), FILTER);
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.DEFAULT), new Boolean(DEFAULT).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.ERROR),
					new Boolean(ERROR).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.WARNING),
					new Boolean(WARNING).toString());
			
			resource.setPersistentProperty(
				new QualifiedName("", PreferenceConstants.RULES_PATH),
				RULES_DIR);
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.RUN_ON_INCREMENTAL_BUILD),
					new Boolean(INC_BUILD).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.RUN_ON_FULL_BUILD),
					new Boolean(FULL_BUILD).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.CHECK_CONTENT),
					new Boolean(CHECK_CONTENT).toString());
			
		} catch (CoreException e) {
			return false;
		}
		return true;
	}
	
	
	

}
