/**
 *
 */
package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.innig.macker.rule.RulesException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import de.his.core.tools.cs.sys.quality.eclipsemacker.custommacker.CustomMacker;
import de.his.core.tools.cs.sys.quality.eclipsemacker.gui.PreferenceConstants;

/**
 *
 * Diese Klasse enthaelt alle definierten Projekteinstellungen
 * die in der Property Page vorgenommen wurden.
 * Zudem
 * @author Bender
 */
public class BuilderSettings {

	/** projekt settings (HIS1)*/
	private static final String PROPERTIES_FILE = "/qisserver/WEB-INF/internal/macker/rules/macker_properties.txt";


    private boolean warnung = false;
    private boolean error = false;
    private boolean defaultM = false;
    private String filterClassContent = "";
    private String filterSourceContent = "";
    private boolean useClassFilter = false;
    private boolean useSourceFilter = false;
    private boolean checkContent = false;
    private boolean runOnFullBuild = false;
    private boolean runOnIncBuild = false;
    private String ruleDir = "";
    private ArrayList<File> ruleFiles = new ArrayList<File>();
    private ArrayList<String> classpathFolders = new ArrayList<String>();
    private ArrayList<String> sourceFolders = new ArrayList<String>();


    /** Das Aktuelle Java Projekt*/

    private IProject project;
    private IJavaProject jProject;


    public BuilderSettings () {

    }


    /**
     * HIS Spezifische settings laden
     */

	private LinkedHashMap<String, String> loadDispatcherProp() {
		String s = "";
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		String separatorDispProp = "=";

		try {

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(
			        new FileInputStream(project.getLocation().toString() + PROPERTIES_FILE)));

			while((s = in.readLine()) != null) {

				if (!s.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(s, separatorDispProp);
					map.put(st.nextToken(), st.nextToken());
				}
		      }
			 in.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			}

		return map;
	}


	/**
	 * HIS Spezifische settings setzen.
	 * @return builderSettings Objekt.
	 */
    public BuilderSettings setHISSettings() {
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
        	this.setRulesDir(map.get(PreferenceConstants.RULES_PATH));
        	this.setjProject(JavaCore.create(project));
        	//rule files instanziieren
        	setRulesFromDirectory();
        	this.getSources();
    	} catch (NullPointerException e) {
    		e.printStackTrace();
    	}

    	return this;
    }





    /**
     * Die im angegebenen "Macker Rules Dir." befindlichen Dateien (*.xml)
     * werden instanziiert und in einer Liste gespeichert.
     *
     */
	public void setRulesFromDirectory() {
		ArrayList<File> r = new ArrayList<File>();

			File dir = new File(project.getLocation().toString() + getRulesDir());

			if (dir.exists() && dir.isDirectory()) {
				File[] fileList = dir.listFiles();

				for(File f : fileList) {
					if (f.getName().endsWith(".xml")) {
						r.add(f);
					}
				}
			}
		setRulesFull(r);
	}

	/**
	 * Die instanziierten Macker Regelen werden einem CustomMacker
	 * Objekt uebergeben.
	 * @param cm CustomMacker Objekt.
	 */
	public void addRulesToMacker(CustomMacker cm) {
		for (File f : getRulesFull()) {
			try {
				cm.addRulesFile(f);
			} catch (RulesException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Aus einem Projekt werden die Einstellungen (Property Page) geladen
	 * und gespeichert.
	 */
    public void setProjectSettings() {

    	this.setDefaultM(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.DEFAULT))));
    	this.setError(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.ERROR))));
    	this.setRunOnFullBuild(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_FULL_BUILD))));
    	this.setRunOnIncBuild(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_INCREMENTAL_BUILD))));
    	this.setFilterContent(getPersistentProperty (new QualifiedName("", PreferenceConstants.CLASSPATH_FILTER)));
    	this.setUseFilter(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_CLASSPATH_FILTER))));
    	this.setFilterSourceContent(getPersistentProperty (new QualifiedName("", PreferenceConstants.SOURCE_FILTER)));
    	this.setUseSourceFilter(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_SOURCE_FILTER))));
    	this.setCheckContent(Boolean.parseBoolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.CHECK_CONTENT))));
    	this.setRulesDir(getPersistentProperty (new QualifiedName("", PreferenceConstants.RULES_PATH)));
    	this.setjProject(JavaCore.create(project));
    	//rule files instanziieren
    	setRulesFromDirectory();
    	this.getSources();
    }

    /**
     * Getter um Projekt Einstellungen aus der Property Page zu laden.
     * @param qn QualifiedName Objekt.
     * @return String Value.
     */
	private String getPersistentProperty (QualifiedName qn) {
		try {
			return getProject().getPersistentProperty(qn);
		} catch (CoreException e) {
			return "";
		}
	}

   //Getter/Setter--------------------------------------------------------------------------------------


	/**
	 * @return the project
	 */
	public IProject getProject() {
		return project;
	}


	/**
	 * @return the filterSourceContent
	 */
	public String getFilterSourceContent() {
		return filterSourceContent;
	}



	/**
	 * @param filterSourceContent the filterSourceContent to set
	 */
	public void setFilterSourceContent(String filterSourceContent) {
		this.filterSourceContent = filterSourceContent;
	}



	/**
	 * @return the useSourceFilter
	 */
	public boolean isUseSourceFilter() {
		return useSourceFilter;
	}



	/**
	 * @param useSourceFilter the useSourceFilter to set
	 */
	public void setUseSourceFilter(boolean useSourceFilter) {
		this.useSourceFilter = useSourceFilter;
	}


	/**
	 * Laden der Sourcefilter Vorgaben.
	 * @return the sources, Liste mit definierten Source Verzeichnissen
	 * welche spaeter ueberprueft werden sollen.
	 */
	public ArrayList<String> getSources() {
		if (this.sourceFolders.size() == 0) {
			//setFilterSourceContent
			String list = this.getFilterSourceContent();
			StringTokenizer st = new StringTokenizer(list, "\t");

			while (st.hasMoreTokens()) {
				sourceFolders.add(st.nextToken());
			}
		}

		return sourceFolders;
	}



	/**
	 * @param sources the sources to set
	 */
	public void setSources(ArrayList<String> sources) {
		this.sourceFolders = sources;
	}



	/**
	 * @return the jProject
	 */
	public IJavaProject getjProject() {
		return jProject;
	}



	/**
	 * @param jProject the jProject to set
	 */
	public void setjProject(IJavaProject jProject) {
		this.jProject = jProject;
	}



	/**
	 * @return the rulesDir
	 */
	public String getRulesDir() {
		return ruleDir;
	}



	/**
	 * @param rulesDir the rulesDir to set
	 */
	public void setRulesDir(String rulesDir) {
		this.ruleDir = rulesDir;
	}



	/**
	 * @param project the project to set
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

	/**
	 * @return the warnung
	 */
	public boolean isWarnung() {
		return warnung;
	}

	/**
	 * @param warnung the warnung to set
	 */
	public void setWarnung(boolean warnung) {
		this.warnung = warnung;
	}

	/**
	 * @return the error
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(boolean error) {
		this.error = error;
	}

	/**
	 * @return the defaultM
	 */
	public boolean isDefaultM() {
		return defaultM;
	}

	/**
	 * @param defaultM the defaultM to set
	 */
	public void setDefaultM(boolean defaultM) {
		this.defaultM = defaultM;
	}

	/**
	 * @return the filterContent
	 */
	public String getFilterContent() {
		return filterClassContent;
	}

	/**
	 * @param filterContent the filterContent to set
	 */
	public void setFilterContent(String filterContent) {
		this.filterClassContent = filterContent;
	}

	/**
	 * @return the useFilter
	 */
	public boolean isUseFilter() {
		return useClassFilter;
	}

	/**
	 * @param useFilter the useFilter to set
	 */
	public void setUseFilter(boolean useFilter) {
		this.useClassFilter = useFilter;
	}

	/**
	 * @return the checkContent
	 */
	public boolean isCheckContent() {
		return checkContent;
	}

	/**
	 * @param checkContent the checkContent to set
	 */
	public void setCheckContent(boolean checkContent) {
		this.checkContent = checkContent;
	}

	/**
	 * @return the runOnFullBuild
	 */
	public boolean isRunOnFullBuild() {
		return runOnFullBuild;
	}

	/**
	 * @param runOnFullBuild the runOnFullBuild to set
	 */
	public void setRunOnFullBuild(boolean runOnFullBuild) {
		this.runOnFullBuild = runOnFullBuild;
	}

	/**
	 * @return the runOnIncBuild
	 */
	public boolean isRunOnIncBuild() {
		return runOnIncBuild;
	}

	/**
	 * @param runOnIncBuild the runOnIncBuild to set
	 */
	public void setRunOnIncBuild(boolean runOnIncBuild) {
		this.runOnIncBuild = runOnIncBuild;
	}

	/**
	 * @return the rulesFull
	 */
	public ArrayList<File> getRulesFull() {
		return ruleFiles;
	}

	/**
	 * @param rulesFull the rulesFull to set
	 */
	public void setRulesFull(ArrayList<File> rulesFull) {
		this.ruleFiles = rulesFull;
	}

	/**
	 * Ermitteln der vom Projekt verwendeten ClassPath Verzeichnisse.
	 * @return the classpaths
	 */
	public ArrayList<String> getClasspaths() {
		if (this.classpathFolders.size() == 0) {
			IJavaProject jp = getjProject();
			try {
				IClasspathEntry[] rawClasspath = jp.getRawClasspath();
				for (int i = 0; i < rawClasspath.length; i++) {
					if (!rawClasspath[i].getPath().toOSString().startsWith("org.eclipse")) {
						classpathFolders.add(rawClasspath[i].getPath().toOSString());
					}

				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}

		return classpathFolders;
	}
	
	public ArrayList<File> getClasspathElements() {
		ArrayList<File> jars = new ArrayList<File>();
		IJavaProject jp = getjProject();
		IClasspathEntry[] rawClasspath;
		IPath location = jp.getProject().getLocation();
		try {
			rawClasspath = jp.getRawClasspath();
			for (int i = 0; i < rawClasspath.length; i++) {
				IPath iPath = rawClasspath[i].getPath();
				iPath = iPath.removeFirstSegments(1); //this removes the webapps prefix
				IPath fullPathToJar = location.append(iPath);
				File file = fullPathToJar.toFile();
				jars.add(file);
			}
			IPath outputLocation = jp.getOutputLocation().removeFirstSegments(1);
			File outputFolder = location.append(outputLocation).toFile();
			jars.add(outputFolder);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return jars;
	}

	/**
	 * @param classpaths the classpaths to set
	 */
	public void setClasspaths(ArrayList<String> classpaths) {
		this.classpathFolders = classpaths;
	}

	public static void main(String[] args) {
		new BuilderSettings().setHISSettings();
	}


}
