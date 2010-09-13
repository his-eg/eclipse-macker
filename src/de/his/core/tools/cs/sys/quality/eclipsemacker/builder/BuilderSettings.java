/**
 * 
 */
package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import net.innig.macker.rule.RulesException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
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
	
	/** projekt settings */
	
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
    	
    	this.setDefaultM(new Boolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.DEFAULT))));
    	this.setError(new Boolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.ERROR))));
    	this.setRunOnFullBuild(new Boolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_FULL_BUILD))));
    	this.setRunOnIncBuild(new Boolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.RUN_ON_INCREMENTAL_BUILD))));
    	this.setFilterContent(getPersistentProperty (new QualifiedName("", PreferenceConstants.CLASSPATH_FILTER)));	
    	this.setUseFilter(new Boolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_CLASSPATH_FILTER))));
    	this.setFilterSourceContent(getPersistentProperty (new QualifiedName("", PreferenceConstants.SOURCE_FILTER)));	
    	this.setUseSourceFilter(new Boolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_SOURCE_FILTER))));
    	this.setCheckContent(new Boolean(getPersistentProperty(new QualifiedName("", PreferenceConstants.CHECK_CONTENT))));
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
			return (String) getProject().getPersistentProperty(qn);
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
				for (int i = 0; i < jp.getRawClasspath().length; i++) {
					
					if (!jp.getRawClasspath()[i].getPath().toOSString().startsWith("org.eclipse")) {
						classpathFolders.add(jp.getRawClasspath()[i].getPath().toOSString());
					}
					
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		} 
		
		return classpathFolders;
	}

	/**
	 * @param classpaths the classpaths to set
	 */
	public void setClasspaths(ArrayList<String> classpaths) {
		this.classpathFolders = classpaths;
	}

	

}
