package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import de.andrena.tools.macker.rule.RulesException;
import de.his.core.tools.cs.sys.quality.eclipsemacker.custommacker.CustomMacker;
import de.his.core.tools.cs.sys.quality.eclipsemacker.util.ConsoleLoggingHelper;


/**
 *
 * @author keunecke
 */
public abstract class AbstractBuilderSettings {

    /**
     * default folder for macker rule files
     */
    protected static final String SETTINGS_MACKER = ".settings/macker";

    /** default project containing macker rule files */
    protected static final String WEBAPPS = "webapps";
    /** projekt settings (HIS1)*/
    private static final String PROPERTIES_FILE = ".settings/macker/macker_properties.txt";
    private boolean warnung = false;
    private boolean error = false;

    private boolean defaultM = true;
    private String filterClassContent = "";
    private String filterSourceContent = "";
    private boolean useClassFilter = false;
    private boolean useSourceFilter = false;
    private boolean checkContent = false;
    private boolean runOnFullBuild = false;
    private boolean runOnIncBuild = false;
    private String ruleDir = "";

    /**
     * Project containing the rules
     */
    protected String ruleProject = "";
    private ArrayList<File> ruleFiles = new ArrayList<File>();
    private ArrayList<String> classpathFolders = new ArrayList<String>();
    private ArrayList<String> sourceFolders = new ArrayList<String>();
    /** Das Aktuelle Java Projekt*/
    protected IProject project;
    private IJavaProject jProject;

    /**
     * Create a new AbstractBuilderSettings instance
     */
    public AbstractBuilderSettings() {
        super();
    }

    /**
     * HIS Spezifische settings laden
     */
    protected LinkedHashMap<String, String> loadDispatcherProp() {
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IProject rp = ws.getRoot().getProject(ruleProject);
        IFile file = rp.getFile(getRulesDir() + "/" + PROPERTIES_FILE);
        String s = "";
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        String separatorDispProp = "=";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(file.getContents()));
            while ((s = in.readLine()) != null) {
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
        } catch (CoreException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * Die im angegebenen "Macker Rules Dir." befindlichen Dateien (*.xml)
     * werden instanziiert und in einer Liste gespeichert.
     *
     * Die Macker Rules Dir ist als relativ zum Projekt zu verstehen, wobei sie mit einem Slash beginnen muss. Mit .. kann
     * man aber auch auf andere Projekte verweisen.
     *
     */
    public void setRulesFromDirectory() {
        ConsoleLoggingHelper log = new ConsoleLoggingHelper(getjProject(), "Macker");
        ArrayList<File> r = new ArrayList<File>();
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IProject ruleIProject = ws.getRoot().getProject(ruleProject);
        String pathname = ruleIProject.getLocationURI().toString() + "/" + getRulesDir();
        log.logToConsole("Effective rules directory: " + pathname);
        IFile ruleDirectory = ruleIProject.getFile(getRulesDir());
        File dir = new File(ruleDirectory.getRawLocationURI());
        boolean exists = dir.exists();
        boolean directory = dir.isDirectory();
        if (exists && directory) {
            addRuleFiles(r, dir);
        }
    }

    private void addRuleFiles(ArrayList<File> r, File dir) {
        ConsoleLoggingHelper log = new ConsoleLoggingHelper(getjProject(), "Macker");
        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            if (f.getName().endsWith(".xml")) {
                r.add(f);
                log.logToConsole("Found rule file: " + f.getAbsolutePath());
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
     * Getter um Projekt Einstellungen aus der Property Page zu laden.
     * @param qn QualifiedName Objekt.
     * @return String Value.
     */
    protected String getPersistentProperty(QualifiedName qn) {
        try {
            return getProject().getPersistentProperty(qn);
        } catch (CoreException e) {
            return "";
        }
    }

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

    /**
     * @return all classpath elements
     */
    public ArrayList<File> getClasspathElements() {
        ArrayList<File> jars = new ArrayList<File>();
        IJavaProject jp = getjProject();
        IClasspathEntry[] rawClasspath;
        IPath location = jp.getProject().getLocation();
        try {
            rawClasspath = jp.getRawClasspath();
            for (IClasspathEntry iClasspathEntry : rawClasspath) {
                if (iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE || iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                    IPath iPath = iClasspathEntry.getPath();
                    iPath = iPath.removeFirstSegments(1); //this removes the webapps prefix
                    IPath fullPathToJar = location.append(iPath);
                    File file = fullPathToJar.toFile();
                    jars.add(file);
                }
                if (iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                    handleReferencedProject(jars, iClasspathEntry);
                }
            }
            IPath outputLocation = jp.getOutputLocation().removeFirstSegments(1);
            File outputFolder = location.append(outputLocation).toFile();
            jars.add(outputFolder);
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        return jars;
    }

    private void handleReferencedProject(ArrayList<File> jars, IClasspathEntry iClasspathEntry) throws JavaModelException {
        // add default output folder
        IPath path = iClasspathEntry.getPath();
        IProject refProject = ResourcesPlugin.getWorkspace().getRoot().getProject(path.lastSegment());
        IJavaProject referencedProject = JavaCore.create(refProject);
        IPath projectLocation = refProject.getLocation();
        File file = projectLocation.append(referencedProject.getOutputLocation().removeFirstSegments(1)).toFile();
        jars.add(file);
        // also add libraries
        IClasspathEntry[] entries = referencedProject.getResolvedClasspath(true);
        for (IClasspathEntry referencedClassPathElement : entries) {
            if (referencedClassPathElement.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                IPath libPath = referencedClassPathElement.getPath();
                File lib = projectLocation.append(libPath.removeFirstSegments(1)).toFile();
                jars.add(lib);
            }
        }
    }

    /**
     * @param classpaths the classpaths to set
     */
    public void setClasspaths(ArrayList<String> classpaths) {
        this.classpathFolders = classpaths;
    }

    /**
     * Initialize settings
     * @return the initialized settings object
     */
    public abstract AbstractBuilderSettings initSettings();

}
