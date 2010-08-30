/**
 * 
 */
package de.his.core.tools.cs.sys.quality.eclipsemacker.gui;




import java.io.File;
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
	private static final String RULES_DIR = "/qisserver/WEB-INF/internal/macker/rules/";
	private static final String FILTER_CLASSPATH = "de/his/appclient/\tde/his/appserver/\tde/his/core/";
	private static final String FILTER_SOURCE = "src/java/\tsrc/generated\tsrc/patches";
	private static final boolean INC_BUILD = true;
	private static final boolean FULL_BUILD = false;
	private static final boolean CHECK_CONTENT = false;
	private static final boolean WARNING = false;
	private static final boolean ERROR = false;
	private static final boolean DEFAULT = true;
	private static final boolean USE_CLASSPTAH_FILTER = true;
	private static final boolean USE_SOURCE_FILTER = true;
	
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
	private Button useSourceFilter;
	private Button buttonCheck;
	private List list; 
	private List listSource; 
	
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

		
		//TODO
		addRulesDir(parent);
		
		Label labelCf = new Label(parent, SWT.NONE);
		labelCf.setText("Classpath Filter");
		addClasspathFilter(parent);
		
		Label label = new Label(parent, SWT.NONE);
		label.setText("Source Filter");
		addSourceFilter(parent);
		
		addCheckboxes(parent);

		// Lade aktuelle Einstellungen
		try {
			IResource resource = ((IJavaProject) getElement()).getResource();
			
			String owner = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.RULES_PATH));
			String incB = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_INCREMENTAL_BUILD));
			String fullB = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_FULL_BUILD));
			String checkC = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.CHECK_CONTENT));
			String filter = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.CLASSPATH_FILTER));
			String useF = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_CLASSPATH_FILTER));
			
			String filterS = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.SOURCE_FILTER));
			String useFS = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_SOURCE_FILTER));
			
			String rW = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.WARNING));
			String rE = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.ERROR));
			String rD = resource.getPersistentProperty(new QualifiedName("", PreferenceConstants.DEFAULT));
			
			
			
			if(filter != null) {
				setListContent(filter, list);
			} else {
				setListContent(FILTER_CLASSPATH, list);
			}
			
			if(filterS != null) {
				setListContent(filterS, listSource);
			} else {
				setListContent(FILTER_SOURCE, listSource);
			}
			useSourceFilter.setSelection((useFS != null) ? new Boolean(useFS) : USE_SOURCE_FILTER);
			useFilter.setSelection((useF != null) ? new Boolean(useF) : USE_CLASSPTAH_FILTER);
			warning.setSelection((rW != null) ? new Boolean(rW) : WARNING);
			error.setSelection((rE != null) ? new Boolean(rE) : ERROR);
			defaultM.setSelection((rD != null) ? new Boolean(rD) : DEFAULT);
			
			checkContent.setSelection((checkC != null) ? new Boolean(checkC) : CHECK_CONTENT);
			fullBuild.setSelection((fullB != null) ? new Boolean(fullB) : FULL_BUILD);
			incBuild.setSelection((incB != null) ? new Boolean(incB) : INC_BUILD);
			rulesDir.setText((owner != null) ? owner : RULES_DIR);

			buttonCheck.setSelection(checkRulesDir(resource));
			
		} catch (CoreException e) {
			rulesDir.setText(RULES_DIR);
		}
	}

	

	private void addSourceFilter(Composite parent) {
		Composite f = new Composite(parent, SWT.None);
		
		GridLayout rf = new GridLayout();
		rf.numColumns = 4;
		f.setLayout(rf);
		listSource = new List(f, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		listSource.setToolTipText("Legt fest welche Source-Verzeichnisse in einem Projekt ueberprueft werden sollen");
		
		listSource.setLayoutData(gridData);
		Composite second = new Composite(f, SWT.None);
		
		GridLayout rz = new GridLayout();
		rz.numColumns = 3;
		second.setLayout(rz);
		
		Button buttonR = new Button(f, SWT.PUSH);
		buttonR.setText("Remove/");
		
		Button button = new Button(f, SWT.PUSH);
		button.setText("Add source folder");

		final Text path = new Text(f, SWT.SINGLE | SWT.BORDER);
		GridData gdz = new GridData(GridData.FILL_HORIZONTAL);
		
		gdz.widthHint = convertWidthInCharsToPixels(50);
		path.setLayoutData(gdz);
		
	    button.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		listSource.add(path.getText());
	    		getListContent(list);
	          }
	    });
		
	    buttonR.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		if (listSource.getSelectionIndex() > -1) {
	    			listSource.remove(listSource.getSelectionIndex());
	    		}
	          }
	    });
	}
	
	
	
	private void addRulesDir(Composite parent) {
		Label headerLabel = new Label(parent, SWT.NONE);
		headerLabel.setText("Macker Propertys");
		
		Composite first = new Composite(parent, SWT.None);
		
		GridLayout r = new GridLayout();
		r.numColumns = 3;
		first.setLayout(r);
		
		Label ruleLabel = new Label(first, SWT.NONE);
		ruleLabel.setText("Macker Rules Directory:");
		rulesDir = new Text(first, SWT.SINGLE | SWT.BORDER);
		rulesDir.setToolTipText("Ein zum Projekt relativer Pfad");
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(55);
		rulesDir.setLayoutData(gd);
		
		buttonCheck = new Button(first, SWT.CHECK);
		buttonCheck.setEnabled(false);
		buttonCheck.setSelection(false);

	}
	
	
	
	private void addCheckboxes(Composite parent) {
		Composite check = new Composite(parent, SWT.None);
		check.setLayout(new GridLayout());
		check.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		useFilter = new Button(check, SWT.CHECK);
		useFilter.setText("Use Classpath Filter");
		useFilter.setToolTipText("Legt fest welche Pakete in einem Projekt ueberprueft werden sollen");
		
		useSourceFilter = new Button(check, SWT.CHECK);
		useSourceFilter.setText("Use Source Filter");
		useSourceFilter.setToolTipText("Legt fest welche Source-Verzeichnisse in einem Projekt ueberprueft werden sollen");
		
		incBuild = new Button(check, SWT.CHECK);
		incBuild.setText("Run on Incremental Build");
		incBuild.setToolTipText("Nach jedem Speichervorgang Pruefen");
		
		fullBuild = new Button(check, SWT.CHECK);
		fullBuild.setText("Run on Full Build");
		fullBuild.setToolTipText("Pruefen bei einem Neuaufbau des Projektes5");

		checkContent = new Button(check, SWT.CHECK);
		checkContent.setText("Check Content");
		checkContent.setToolTipText("Fehleranzeige innerhalb einer Klasse");

		
		Label events = new Label(parent, SWT.None);
		events.setText("Show Macker Events as");
		
		radioGroup = new Composite(parent, SWT.NONE);
		GridLayout g = new GridLayout();
		g.numColumns = 3;
		radioGroup.setLayout(g);
		radioGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		warning = createRadioButton(radioGroup, "Warning");
		warning.setToolTipText("Zeige alle Verstoesse als Warnungen an");
		error = createRadioButton(radioGroup, "Error");
		error.setToolTipText("Zeige alle Verstoesse als Kompilierungsfehler an");
		defaultM = createRadioButton(radioGroup, "Default");
		defaultM.setToolTipText("Vorgegebene Einstellung verwenden");
	}
	
	private void addClasspathFilter(Composite parent) {
		Composite f = new Composite(parent, SWT.None);
		
		GridLayout rf = new GridLayout();
		rf.numColumns = 4;
		f.setLayout(rf);
		list = new List(f, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		list.setToolTipText("Legt fest welche Pakete in einem Projekt ueberprueft werden sollen.");
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
	    		getListContent(list);
	          }
	    });
		
	    buttonR.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		if (list.getSelectionIndex() > -1) {
	    			list.remove(list.getSelectionIndex());
	    		}
	          }
	    });
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
		
		listSource.setItems(new String[]{"src/java/", "src/generated", "src/patches"});
		warning.setSelection(WARNING);
		defaultM.setSelection(DEFAULT);
		error.setSelection(ERROR);
		useFilter.setSelection(USE_CLASSPTAH_FILTER);
	}
	
	/**
	 * Projekt Einstellungen dauerhaft speichern.
	 */
	public boolean performOk() {
		// store the values
		try {
			IResource resource = ((IJavaProject) getElement()).getResource();
			
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.USE_SOURCE_FILTER),
					new Boolean(useSourceFilter.getSelection()).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.USE_CLASSPATH_FILTER),
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
					new QualifiedName("", PreferenceConstants.CLASSPATH_FILTER),
					getListContent(list));
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.SOURCE_FILTER),
					getListContent(listSource));
			
			
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
			
			
			buttonCheck.setSelection(checkRulesDir(resource));
			
		} catch (CoreException e) {
			return false;
		}
		return true;
	}
	
	
	private boolean checkRulesDir (IResource project) {
		boolean found = false;
		File dir = new File(project.getLocation().toString() + rulesDir.getText());
		if (dir.exists() && dir.isDirectory()) {
			found = true;
		}
		return found;
	}
	
	/**
	 * Setzt die Werte des Classpath Filters
	 * @param content
	 */
	private void setListContent(String content, List list) {
		StringTokenizer st = new StringTokenizer(content, "\t");
	     while (st.hasMoreTokens()) { 
	       list.add(st.nextToken());
	     }
	}
	/**
	 * Gibt die aktuellen Werte des Classpath filters zurueck.
	 * @return
	 */
	private String getListContent(List list) {
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
					new QualifiedName("", PreferenceConstants.USE_CLASSPATH_FILTER),
					new Boolean(USE_CLASSPTAH_FILTER).toString());

			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.USE_SOURCE_FILTER),
					new Boolean(USE_SOURCE_FILTER).toString());
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.CLASSPATH_FILTER), FILTER_CLASSPATH);
			
			resource.setPersistentProperty(
					new QualifiedName("", PreferenceConstants.SOURCE_FILTER), FILTER_SOURCE);
			
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
