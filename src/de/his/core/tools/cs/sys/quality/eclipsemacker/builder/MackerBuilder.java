package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.JavaCore;

import de.andrena.tools.macker.event.AccessRuleViolation;
import de.andrena.tools.macker.structure.ClassParseException;
import de.his.core.tools.cs.sys.quality.eclipsemacker.Activator;
import de.his.core.tools.cs.sys.quality.eclipsemacker.custommacker.CustomMacker;
import de.his.core.tools.cs.sys.quality.eclipsemacker.custommacker.ShowAs;
import de.his.core.tools.cs.sys.quality.eclipsemacker.gui.PreferenceConstants;
import de.his.core.tools.cs.sys.quality.eclipsemacker.preferences.MackerGlobalPreferenceConstants;
import de.his.core.tools.cs.sys.quality.eclipsemacker.util.ConsoleLogger;

/**
 * Der MackerBuilder ruft bei jedem Speichervorgang oder bei einem Neuaufbau des
 * Projektes die "build" Methode auf.
 *
 * Bei einem Incremental/Full Build Aufruf werden die Ressourcen anahnd von
 * definierten Architekturreglen mit einem CustomMacker Objekt ueberprueft.
 *
 * Gefundene Regelverstosse werden als Eclipse-Marker angezeigt.
 *
 * @author Bender
 */
public class MackerBuilder extends IncrementalProjectBuilder {

    private static final boolean DEBUG = false;

    /**
     * Zaehlvariable der bereits geprueften Klassen.
     */
    private int count = 0;

    /**
     * Enthaelt alle in der Property Page definierten Einstellungen.
     */
    private AbstractBuilderSettings builderSettings;

    /**
     * CustomMacker Objekt, erhealt alle zu pruefenden Klassen, sowie
     * Instanzen der definierten Architekturregeln.
     */
    private CustomMacker customMacker;

    /**
     * Speichert geworfene Exceptions und zeigt sie in der MackerView an.
     */
    public static ArrayList<String> builderErrors = new ArrayList<String>();

    /**
     * Create a new MackerBuilder
     */
    public MackerBuilder() {
        this.builderSettings = new BuilderGlobalSettings();
        this.customMacker = new CustomMacker();
    }

    /**
     * Der Delta Visitor wird bei einem Incremetal Build aufgerufen.
     * Die Ressource Delta beeinhaltet die Veraenderungen zum letzten Speicherzeitpunkt
     * einer Ressource.
     *
     * Beinhaltet die Ressource Delta Aenderungen oder Neuerungen wird die checkMacker
     * Methoe aufgerufen.
     *
     * @author Bender
     */
    class MackerDeltaVisitor implements IResourceDeltaVisitor {

        private final IProgressMonitor monitor;

        /**
         * @param monitor
         */
        public MackerDeltaVisitor(final IProgressMonitor monitor) {
            this.monitor = monitor;
        }


        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
         */
        @Override
        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();
            switch (delta.getKind()) {

            case IResourceDelta.ADDED:
                cleanMarkersForResourceDelta(resource);
                // handle added resource
                checkMacker(resource, monitor);
                break;
            case IResourceDelta.REMOVED:
                // handle removed resource
                break;
            case IResourceDelta.CHANGED:
                cleanMarkersForResourceDelta(resource);
                // handle changed resource
                checkMacker(resource, monitor);
                break;
            }

            //return true to continue visiting children.
            return true;
        }


        private void cleanMarkersForResourceDelta(IResource resource) throws CoreException {
            IMarker[] markers = resource.findMarkers(MackerBuilder.MARKER_TYPE, true, IResource.DEPTH_ZERO);
            for (IMarker marker : markers) {
                marker.delete();
            }
        }
    }

    /**
     * Der MackerResourceVisitor wird bei einem FullBuild aufgerufen.
     * Es werden somit alle Dateien im Workspace zunaechst instanziiert.
     * @author Bender
     */
    class MackerResourceVisitor implements IResourceVisitor {
        IProgressMonitor monitor;

        /**
         * @param monitor
         */
        public MackerResourceVisitor(final IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public boolean visit(IResource resource) {
            IMarker[] markers;
            try {
                markers = resource.findMarkers(MackerBuilder.MARKER_TYPE, true, IResource.DEPTH_ZERO);
                for (IMarker marker : markers) {
                    marker.delete();
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
            checkMacker(resource, monitor);
            //return true to continue visiting children.
            return true;
        }
    }


    /**
     * Feste Builder-ID.
     */
    public static final String BUILDER_ID = "de.his.core.tools.cs.sys.quality.eclipsemacker.mackerBuilder";

    /**
     * Feste Marker-ID.
     */
    public static final String MARKER_TYPE = "de.his.core.tools.cs.sys.quality.eclipsemacker.mackerEvent";



    /**
     * Bei jedem Buildvorgang wird zunaechst geprueft ob die Einstellungen aus
     * der PropertyPage bereits geladen wurden.
     * Danach wird das BuilderSettings Objekt aktualisert.
     *
     * Nachdem ein Buildvorgang abgeschlossen ist, wird die
     * Methode checkRessources aufgerufen, um u.a. die Eclipse-Marker zu setzen.
     */
    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
        customMacker = new CustomMacker();
        count = 0;
        this.configureMacker();
        if(!this.getBuilderSettings().getRulesFull().isEmpty()) {
            // collect resources
            collectResources(kind, monitor);
            //check collected resources
            checkResources(monitor);
        } else {
            ConsoleLogger log = new ConsoleLogger(JavaCore.create(getProject()).getElementName(), "Macker", Activator.isLoggingEnabled());
            log.logToConsole("Skipping further checks as no rules were found.");
        }
        return null;
    }

    /**
     * This method collects all resources that should be checked depending on the kind of build
     * @param kind
     * @param monitor
     * @throws CoreException
     */
    private void collectResources(int kind, IProgressMonitor monitor) throws CoreException {
        if (kind == FULL_BUILD) {
            fullBuild(monitor);
        } else {
            IResourceDelta delta = getDelta(getProject());
            if (delta == null) {
                fullBuild(monitor);
            } else {
                incrementalBuild(delta, monitor);
            }

        }
    }

    /**
     * Prepare the macker configuration depending on settings
     * @throws CoreException
     */
    private void configureMacker() throws CoreException {
        customMacker = new CustomMacker();
        count = 0;
        ConsoleLogger log = new ConsoleLogger(JavaCore.create(getProject()).getElementName(), "Macker", Activator.isLoggingEnabled());
        String useGlobalSettingsPref = getProject().getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_GLOBAL_SETTINGS));
        if ("false".equals(useGlobalSettingsPref)) {
            // use local settings
            this.builderSettings = new BuilderProjectSpecificSettings();
            log.logToConsole("Using project specific settings");
        } else {
            // use global settings
            this.builderSettings = new BuilderGlobalSettings();
            log.logToConsole("Using global settings");
        }

        //dem builder eine referenz auf das aktuelel projekt uebergeben
        this.getBuilderSettings().setProject(getProject());
        this.builderSettings.initSettings();

        //log if no rules found
        if (this.builderSettings.getRulesFull().isEmpty()) {
            log.logToConsole("No Rules found for configuration of Project '" + getProject().getName() + "'");
            log.logToConsole("Rules configured in project '" + this.builderSettings.getProject().getName() + "'.");
            log.logToConsole("Rules configured in folder '" + this.builderSettings.getRulesDir() + "'");
        }

        //einmaliges hinzufuegen der definierten Regeln
        this.getBuilderSettings().addRulesToMacker(customMacker);

        addJarsToClasspath(log);
    }

    /**
     * Compute the classpath that should be used by macker
     */
    private void addJarsToClasspath(ConsoleLogger log) {
        ArrayList<File> jarsInClasspath = builderSettings.getClasspathElements();
        ArrayList<URL> jarUrls = new ArrayList<URL>();
        for (File jar : jarsInClasspath) {
            try {
            	URL entry = jar.toURI().toURL();
                jarUrls.add(entry);
                if (DEBUG) log.logToConsole("Added classpath entry: " + entry);
            } catch (MalformedURLException e) {
                log.logToConsole("ERROR: Failed to add classpath entry: " + jar);
                e.printStackTrace();
            }
        }
        URLClassLoader urlClassLoader = new URLClassLoader(jarUrls.toArray(new URL[0]), getClass().getClassLoader());
        customMacker.setClassLoader(urlClassLoader);
    }

    /**
     * Eine veraenderte/neue java-Datei wird zunaechst instanziiert,
     * danach wird die entsprechende Class-Datei gesucht und ebenfalls
     * instanziiert.
     *
     * Das Class-File wird der Macker-Instanz zum pruefen uebergeben,
     * und die Class-Location samt JavaFile Instanz in einer Map gespeichert.
     *
     * Monitor: "Macker, Lade Klassen: ... ".
     *
     * @param resource veraenderte Ressource.
     */
    void checkMacker(IResource resource, IProgressMonitor monitor) {

        //Nur java Dateien sind fuer diesen Builder relevant.
        if ((resource instanceof IFile) && resource.getName().endsWith(".java")) {

            //Die veranderte Ressource instanziieren.

            IFile javaFile = (IFile) resource;
            String fullP = javaFile.getFullPath().toString();

            //Filteraufruf falls aktiviert
            boolean run = getBuilderSettings().isUseFilter();
            boolean checkFilter = true;
            if (run) {
                checkFilter = toCheck(fullP);
            }

            if (checkFilter) {
                String projectName = resource.getProject().getName();
                monitor.beginTask(javaFile.getName(), 8000);
                deleteMarkers(javaFile);
                File classFile = null;
                //Src Folder aus dem Dateipfad ermitteln
                String src = getSourceFolder(fullP);
                //aus dem Javafile das Class-File ableiten
                try {
                    classFile = new File(javaFile.getLocation().toString().replace(src, getBuilderSettings().getjProject().getOutputLocation().toOSString().replace(projectName, "")).replace(".java", ".class").replace("\\", "/"));
                } catch (CoreException e2) {
                    e2.printStackTrace();
                }

                if (classFile != null) {
                	String exists = classFile.exists() ? "exists" : "not existing";
                    try {
                        // Class-Location und javaFile in einer Map speichern
                        String classLoc = fullP.replace(src, "").replace("/" + projectName + "/", "").replace(".java", "").replace("/", ".");
                        customMacker.getJavaMap().put(classLoc, javaFile);
                        count++;
                        // Macker die Class-Datei zum ueberpruefen uebergeben
                        customMacker.addClass(classFile);
                    } catch (ClassParseException e) {
                        builderErrors.add("E/1 " + fullP + " (" + exists + "): " + e.getMessage());
                    } catch (IOException e) {
                        builderErrors.add("E/2 " + fullP + " (" + exists + "): " + e.getMessage());
                    } catch (IllegalStateException e) {
                        builderErrors.add("E/3 " + fullP + " (" + exists + "): " + e.getMessage());
                    }
                    monitor.subTask("Macker, lade Klassen: " + count + " " + javaFile.getName());
                }
            }
        }
    }

    /**
     * Prueft anhand eines uebergebenen Pfades die Filterbedingungen.
     *
     * @param path File Speicherpfad.
     * @return true falls Filterbedingung erfuellt.
     */
    private boolean toCheck(String path) {
        String filterContent = getBuilderSettings().getFilterContent();
        StringTokenizer st = new StringTokenizer(filterContent, "\t");
        ArrayList<String> sources = getBuilderSettings().getSources();
        boolean useSourceFilter = getBuilderSettings().isUseSourceFilter();
        while (st.hasMoreTokens()) {
            String check = st.nextToken();
            int indexOf = path.indexOf(check);
            if (indexOf > -1) {
                if (useSourceFilter) {
                    for (String s : sources) {
                        int indexOfSrcFolder = path.indexOf(s);
                        if (indexOfSrcFolder > -1) {
                            return true;
                        }
                    }
                } else {
                    return true;
                }


            }
        }
        return false;
    }



    /**
     * Ermittelt den aktuell verwendeten Classpath.
     *
     * @param path File Speicherpfad.
     * @return den aktuell verwendeten source ordner.
     */
    private String getSourceFolder(String path) {
        String src = "";
        for (int i = 0; i < getBuilderSettings().getClasspaths().size(); i++) {
            String vgl = getBuilderSettings().getClasspaths().get(i).replace("\\", "/");
            //den aktuellen classpath ermitteln, durch vergleich mit java file path.
            if (path.indexOf(vgl) > -1) {
                src = getBuilderSettings().getClasspaths().get(i).replace("\\", "/").replace("/" + getProject().getName(), "");
            }
        }
        return src;
    }


    /**
     * Nachdem die Klassen geladen sind, wird die Macker "checkClass" Methode
     * aufgerufen.
     * Dabei werden geworfene MackerEvents vom Typ AccessRuleViolation geworfen und im
     * MackerListener in einer Map gespeichert.
     *
     * Monitor: "Macker, pruefe Klassen"
     *
     * Nachdem alle Klassen von Macker geprueft wurden, werden die Marker gesetzt(importCheck).
     *
     * @param monitor Monitor.
     */

    private void checkResources(IProgressMonitor monitor) {
        if (customMacker.hasRules() && customMacker.hasClasses()) {
            monitor.subTask("Macker, pruefe Klassen: ");
            monitor.worked(1);
            //Macker Classfile check
            if (customMacker.checkClass()) {
                try {
                    importCheck(monitor);
                } catch (CoreException e) {
                    e.printStackTrace();
                }

                if (monitor.isCanceled()) {
                    return;
                }
            }

        } else {
            monitor.subTask("Keine Klassen und/oder Regeln vorhanden.");
        }

        monitor.done();
    }


    /**
     * Der MackerListener hat alle Klassen samt Verstoessen in einer
     * Map gespeichert, welche nun als Basis zum Marker setzen dient.
     *
     * Unterschieden wird zunächste ob der ganze Inhalt einer Klasse oder
     * nur die Import-Tags markiert werden sollen.
     *
     * Monitor: "Macker, Setze Warnungen: ..."
     *
     * @throws CoreException
     */
    private boolean importCheck(IProgressMonitor monitor) throws CoreException {

        boolean erfolg = false;
        int size = customMacker.getListener().getViolation().size();

        InputStream in = null;
        LineNumberReader reader = null;
        for (Map.Entry<String, ArrayList<AccessRuleViolation>> entry : customMacker.getListener().getViolation().entrySet()) {
            monitor.subTask("Macker, Setze Warnungen: " + count + "/" + size);
            monitor.worked(1);

            in = customMacker.getJavaMap().get(entry.getKey()).getContents();
            reader = new LineNumberReader(new InputStreamReader(in));

            try {
                erfolg = checkFullContent(reader, entry);
                in.close();
                reader.close();

            } catch (IOException e) {
                erfolg = false;
                e.printStackTrace();
            }
        }
        return erfolg;
    }

    /**
     * Setzt innerhlab der gesamten Klasse Eclipse-Marker.
     *
     * @param reader vom typ LineNumberReader
     * @param entry Map Entry mit Classlocation und AccessRuleViolation Objekten.
     * @return true.
     * @throws IOException
     */
    private boolean checkFullContent(LineNumberReader reader, Map.Entry<String, ArrayList<AccessRuleViolation>> entry) throws IOException {
        String line = "";

        while (reader.ready()) {
            line = reader.readLine().trim();

            if (!line.startsWith("package") && !line.startsWith("//") && !line.startsWith("/*") && !line.startsWith("*") && !line.startsWith("/**")) {

                for (int i = 0; i < customMacker.getListener().getViolation().get(entry.getKey()).size(); i++) {
                    String to = customMacker.getListener().getViolation().get(entry.getKey()).get(i).getTo().toString().replace("$", ".");
                    int start = to.lastIndexOf(".") + 1;

                    if (line.startsWith("import") && checkImportLineViolation(line, customMacker.getListener().getViolation().get(entry.getKey()).get(i).getTo().toString())) {
                        setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());

                    } else if (line.indexOf(to.substring(start)) > -1) {

                        StringTokenizer st = new StringTokenizer(line, " ");
                        boolean gefunden = false;

                        while (st.hasMoreTokens() && !gefunden) {
                            String t = st.nextToken();
                            //direkt zuzuordnen
                            if (t.equals(to.substring(start))) {
                                setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
                                gefunden = true;
                            }
                            //statischer zugriff auf die klasse
                            if (t.indexOf(".") > -1) {
                                if ((t.substring(0, t.indexOf(".")).equals(to.substring(start)))) {
                                    setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
                                    gefunden = true;
                                }
                            }
                            //vollqualifizierter Zugriff
                            if (t.startsWith(to)) {
                                setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
                                gefunden = true;

                            }
                            //verwendung der klasse als exception (im ty/catch block)
                            if (t.startsWith("(")) {
                                if (((t.replace("(", ""))).equals(to.substring(start))) {
                                    setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
                                    gefunden = true;
                                }
                            }

                            //verwendung der klasse als parameter
                            if (line.startsWith("public") || line.startsWith("private") || line.startsWith("protected")) {
                                //erster parameter der methode
                                if (line.indexOf("(" + to.substring(start) + " ") > -1) {
                                    setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
                                    gefunden = true;
                                    //folgender parameter
                                } else if (line.indexOf(", " + to.substring(start) + " ") > -1) {
                                    setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
                                    gefunden = true;
                                }
                                //instanziieren der klasse
                            } else if ((line.indexOf("new " + to.substring(start) + "(")) > -1) {
                                setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
                                gefunden = true;
                                //verketteter aufruf der klasse
                            } else if ((line.indexOf("(" + to.substring(start) + ".")) > -1) {
                                setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
                                gefunden = true;
                                //cast der klasse
                            } else if ((line.indexOf("(" + to.substring(start) + ")") > -1)) {
                                setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
                                gefunden = true;
                                //verwenung der klasse in einer liste
                            } else if ((line.indexOf("<" + to.substring(start) + ">") > -1)) {
                                setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
                                gefunden = true;
                            }

                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * SetMarker Methode setzt ein Eclipse-Marker, wobei geprueft wird welche Art
     * von Marker gesetzt werden soll.
     *
     * @param cm CustomMacker Instanz.
     * @param line Zeilennummer.
     * @param index der Liste mit AccessRuleViolations der Klasse.
     * @param className Paketpfad der aktuellen Klasse.
     */
    private void setMarker(CustomMacker cm, int line, int index, String className) {

        String severity = "";
        if (getBuilderSettings().isDefaultM()) {
            severity = "DEFAULT";
        } else if (getBuilderSettings().isError()) {
            severity = "ERROR";
        } else {
            severity = "WARNING";
        }

        // ignore messages with severity 'debug'
        if (cm.getListener().getViolation().get(className).get(index).getRule().getSeverity().getName().toUpperCase().equals("DEBUG")) {
            return;
        }

        //Warnungen setzen, anhand der default Einstellungen oder angepasst.
        String message = cm.getListener().getViolation().get(className).get(index).getTo().toString();
        String source = message.substring(message.lastIndexOf(".") + 1);

        //Severity direkt vom Event holen
        if (ShowAs.valueOf(severity) == ShowAs.DEFAULT) {
            severity = cm.getListener().getViolation().get(className).get(index).getRule().getSeverity().getName().toUpperCase();
            addMarker(cm.getJavaMap().get(className), "(" + source + ") " + cm.getListener().getViolation().get(className).get(index).getMessages().get(0).toString(), line, setSeverity(ShowAs.valueOf(severity)));
            //Severity anhand der angepassten Einstellung
        } else {
            addMarker(cm.getJavaMap().get(className), "(" + source + ") " + cm.getListener().getViolation().get(className).get(index).getMessages().get(0).toString(), line, setSeverity(ShowAs.valueOf(severity)));
        }
    }



    /**MarkerTyp setzen.
     * @param choice ShowAs Enum
     * @return Severity Typ.
     */
    private int setSeverity(ShowAs choice) {
        int i = 0;
        switch (choice) {
        case INFO:
            i = IMarker.SEVERITY_INFO;
            break;
        case WARNING:
            i = IMarker.SEVERITY_WARNING;
            break;
        case ERROR:
            i = IMarker.SEVERITY_ERROR;
            break;

        default:
            i = IMarker.SEVERITY_ERROR;
            break;

        }
        return i;
    }



    /**
     * Prueft ein Import-Tag auf Uebereinstimmung mit einem Mackerevent, indem
     * der Import und die Meldung des Events (getTo()) verglichen werden.
     *
     * @param importline import Anweisung in einer Java-Datei.
     * @param to getTo() Meldung des Macker-Events.
     *
     * @return true falls Mackermeldung auf Import-tag zutrifft.
     */
    private boolean checkImportLineViolation(String importline, String to) {
        boolean violation = false;
        String temp = to.replace("$", ".");
        String importlinedCleaned = importline;
        if (importline.endsWith("*;")) {
            //importline = db.* | to = db.DB
            importlinedCleaned = importline.replace("*", "");
            temp = temp.substring(0, temp.lastIndexOf("."));
        } else {
            temp = temp + ";";
        }
        violation = importlinedCleaned.indexOf(temp) >= 0;

        return violation;
    }


    /**
     * Setzt ein Marker.
     *
     * @param file java Resource
     * @param message
     * @param lineNumber
     * @param severity
     */
    private void addMarker(IFile file, String message, int lineNumber, int severity) {

        try {
            IMarker marker = file.createMarker(MARKER_TYPE);
            marker.setAttribute(IMarker.MESSAGE, message);
            marker.setAttribute(IMarker.SEVERITY, severity);

            int myLineNumber = lineNumber;
            if (myLineNumber == -1) {
                myLineNumber = 1;
            }

            marker.setAttribute(IMarker.LINE_NUMBER, myLineNumber);

        } catch (CoreException e) {
            e.printStackTrace();
        }
    }


    /**
     * Entfernt Marker.
     *
     * @param file
     */
    private void deleteMarkers(IFile file) {
        try {
            file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prueft das gesamte Projekt.
     *
     * @param monitor
     * @throws CoreException
     */
    protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
        if (getBuilderSettings().isRunOnFullBuild()) {
            try {
                IMarker[] markers = getProject().findMarkers(MackerBuilder.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
                for (IMarker marker : markers) {
                    marker.delete();
                }
                getProject().accept(new MackerResourceVisitor(monitor));
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Prueft nur die neuen/veraenderten Resourcen des Projekts.
     *
     * @param delta
     * @param monitor
     * @throws CoreException
     */
    protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
        // the visitor does the work.
        if (getBuilderSettings().isRunOnIncBuild()) {
            delta.accept(new MackerDeltaVisitor(monitor));
        }
    }


    //Getter und Setter

    /** CustomMacker Objekt wird zurueck gegeben.
     * @return the customMacker.
     */
    public CustomMacker getCustomMacker() {
        return customMacker;
    }

    /**
     * CustomMacker objekt setzen.
     *
     * @param customMacker the cutomMacker to set.
     */
    public void setCustomMacker(CustomMacker customMacker) {
        this.customMacker = customMacker;
    }


    /**
     * @return the builderSettings
     */
    public AbstractBuilderSettings getBuilderSettings() {
        return builderSettings;
    }

    /**
     * @param builderSettings the builderSettings to set
     */
    public void setBuilderSettings(AbstractBuilderSettings builderSettings) {
        this.builderSettings = builderSettings;
    }

}