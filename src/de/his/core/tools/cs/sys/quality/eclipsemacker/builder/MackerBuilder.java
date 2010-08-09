package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Map;
import net.innig.macker.event.AccessRuleViolation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import de.his.core.tools.cs.sys.quality.eclipsemacker.Activator;
import de.his.core.tools.cs.sys.quality.eclipsemacker.gui.PreferenceConstants;


public class MackerBuilder extends IncrementalProjectBuilder {
	
    
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
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				checkMacker(resource, monitor);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				checkMacker(resource, monitor);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
	}

	class SampleResourceVisitor implements IResourceVisitor {
		IProgressMonitor monitor;

        /**
         * @param monitor
         */
        public SampleResourceVisitor(final IProgressMonitor monitor) {
            this.monitor = monitor;
        }
        
		public boolean visit(IResource resource) {
			checkMacker(resource, monitor);
			//return true to continue visiting children.
			return true;
		}
	}



	public static final String BUILDER_ID = "de.his.core.tools.cs.sys.quality.eclipsemacker.mackerBuilder";

	private static final String MARKER_TYPE = "de.his.core.tools.cs.sys.quality.eclipsemacker.xmlProblem";

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
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
		return null;
	}

	/**
	 * Eine veraenderte/neue java-Datei wird anahnd vorgegebner Macker-Rules
	 * auf Korrektheit geprueft.
	 * 
	 * 
	 * @param resource veraenderte Ressource.
	 */
	void checkMacker(IResource resource, IProgressMonitor monitor)  {
		

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		/*
		 * Nur java Dateien sind fuer diesen Builder relevant.
		 */
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {

			/*
			 * Die veranderte Ressource instanziieren.
			 */
			IFile javaFile = (IFile) resource;
			IProject project = getProject();
			IJavaProject javaProject = JavaCore.create(project);
			String projectName = resource.getProject().getName();
			//TEST monitor
			monitor.beginTask(javaFile.getName(), 100);
			
			deleteMarkers(javaFile);

			
			System.out.println(project.getLocation().toString());

			/*
			 *Bin file (*.Class) und Macker-Rules (*.XML) instanziieren.
			 */

			File classFile = new File("");
			
			
			try {
				classFile = new File(javaFile.getLocation().toString().replace(getSourceFolder(javaFile, javaProject), javaProject.getOutputLocation().toOSString().replace(projectName, "")).replace("java", "class").replace("\\", "/"));
			} catch (JavaModelException e1) {
				e1.printStackTrace();
			}

			//TODO mackerRules File auf korrektheit pruefen.
			if (classFile.exists()) {
				monitor.worked(25);
	            /*
	             * Falls Macker-Test erfolgreich, ordne den Macker-Events
	             * die richtigen Zeilennummern zu.
	             */
				//TODO store.getString(PreferenceConstants.RULES_PATH
				System.out.println(project.getLocation().toString()+store.getString(PreferenceConstants.RULES_PATH));
				CustomMacker cm = new CustomMacker(classFile, javaFile, project.getLocation().toString()+store.getString(PreferenceConstants.RULES_PATH));
				monitor.subTask("Lade Macker Rules");
				if (cm.getRuleFiles().size() > 0) {
					//Macker Classfile check
		            if (cm.checkClass()) {
		            	monitor.worked(50);
		            	//pruefen ob macker events gefunden
		            	if (cm.getListener().getViolationList().size() > 0) {
			            	try {
			            		monitor.subTask("Setze Marker");
			            		//marker setzen
			    				importCheck(cm);
			    				
			    				if (store.getBoolean(PreferenceConstants.CHECK_CONTENT)) {
			    					checkClassContent(cm);
			    				}
			    				monitor.worked(24);
			    			} catch (CoreException e) {
			    				e.printStackTrace();
			    			}
		            	}
		            }
				}
		
		} else {
			System.out.println("xml oder class datei nicht gefunden");
		}
		}
		monitor.done();
	}

	
	/**
	 * Ermittelt den aktuellen verwendeten classpath.
	 * @param javaFile zu pruefende java datei.
	 * @param javaProject aktuelles java projekt.
	 * 
	 * @return aktuellen classpath
	 */
	
	private String getSourceFolder(IFile javaFile, IJavaProject javaProject) {
		String src = "";
		String projectName = javaProject.getProject().getName();

		try {
			
			for (int i = 0; i < javaProject.getRawClasspath().length; i++) {
				String vgl = javaProject.getRawClasspath()[i].getPath().toOSString().replace("/"+projectName, "");
				//den aktuellen classpath ermitteln, durch vergleich mit java file path.
				if (javaFile.getFullPath().toOSString().indexOf(vgl) > -1) {
					src = javaProject.getRawClasspath()[i].getPath().toString().replace("/"+projectName, "");
				}
			}

			} catch (JavaModelException e) {
			
				e.printStackTrace();
			}
		
			
		return src;
	}
	
	
	/**
	 * Liesst die import-Tags einer Java Datei und prueft dabei ob ein gefundener
	 * Macker-Event zutrifft.
	 * 
	 * @param cm Macker-Instanz mit den src/bin files und den Macker-Events 
	 * der betreffenden Ressource.
	 * 
	 * 
	 * @throws CoreException
	 */
	private boolean importCheck(CustomMacker cm) throws CoreException {
		boolean erfolg = false;
		
		InputStream in = null;
		in = cm.getJavaIFile().getContents();
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));

		try {
			
			String line = "";
			ArrayList<AccessRuleViolation> tmp = (ArrayList<AccessRuleViolation>) cm.getListener().getViolationList().clone();
			
			while (reader.ready() && !line.startsWith("public class") && cm.getListener().getViolationList().size() > 0) {
				line = reader.readLine();
				
				if (line.startsWith("import")) {
					/*
					 * Ein Import-Tag wird auf Uebereinstimmung mit den gefundenen Macker-Events geprueft.
					 */
					
					//TODO abbrechen wenn gefunden
					for (int i = 0; i < cm.getListener().getViolationList().size(); i++) {

						if (checkImportViolation(line, cm.getListener().getViolationList().get(i).getTo().toString())) {

							setMarker(cm, reader.getLineNumber(), i);
							/*
							 * bei Uebereinstimmung betreffenden Event aus Liste entfernen.
							 */
							cm.getListener().getViolationList().remove(i);

						}
					}

					
				}
			}
			
			reader.close();
			cm.getListener().setV(tmp);
		} catch (IOException e) {
			erfolg = false;
			e.printStackTrace();
		}
		return erfolg;
		
		
		} 
	
	
	
	private void setMarker(CustomMacker cm, int line, int index) {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String severity = store.getString("CHOICE");
		
		if (store.getString("CHOICE").equals("")) {
			severity = "ERROR";
		}
		/*
		 * Warnungen setzen, anhand der default Einstellungen oder angepasst. 
		 */
		if (ShowAs.valueOf(severity) == ShowAs.DEFAULT) {
			//Severity direkt vom Event holen
			severity = cm.getListener().getViolationList().get(index).getRule().getSeverity().getName().toUpperCase();
			
			addMarker(cm.getJavaIFile(), cm.getListener().getViolationList().get(index)
					.getMessages().toString(), line, setSeverity(ShowAs.valueOf(severity)));
		} else {
			//Severity anhand der angepassten Einstellung
			addMarker(cm.getJavaIFile(), cm.getListener().getViolationList().get(index)
					.getMessages().toString(), line, setSeverity(ShowAs.valueOf(severity)));
		}
	}
	
	
	private void checkClassContent(CustomMacker cm) {
	
		InputStream in = null;
		try {
			in = cm.getJavaIFile().getContents();
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
			String line = "";
			
			while (reader.ready()) {
				line = reader.readLine();
				line = line.replaceAll("\t", "").replaceAll(" ", "");
				if (!line.startsWith("import") && !line.startsWith("package") && !line.startsWith("//") && !line.startsWith("/*")&& !line.startsWith("*")) {
					
					for (int i = 0; i < cm.getListener().getViolationList().size(); i++) {
						String to = cm.getListener().getViolationList().get(i).getTo().toString();
						int start = to.lastIndexOf(".") + 1;
						
						if (line.indexOf(to.substring(start)) > -1) {
							setMarker(cm, reader.getLineNumber(), i);
						}
					}

				}
			}
				

		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}
	

	/**
	 * @param choice ShowAs Enum
	 * @return Severity Typ.
	 */
	private int setSeverity (ShowAs choice) {
		int i = 0;
		switch (choice) {
		case INFO:
			i =  IMarker.SEVERITY_INFO;
			break;
		case WARNING:
			i =  IMarker.SEVERITY_WARNING;
			break;
		case ERROR:
			i =  IMarker.SEVERITY_ERROR;
			break;
			
		default:
			i =  IMarker.SEVERITY_ERROR;
			break;

		}
	return i;
	}
	
	
	
	/**
	 * Prueft ein Import-Tag auf Uebereinstimmung mit einem Mackerevent, indem
	 * der Import und die Meldung des Events (getTo()) verglichen werden.
	 * 
	 * @param importline import Anweisung in einer Java-Datei.
	 * @param to getTo() Meldung des Macker-Evtns
	 * 
	 * @return true falls Mackermeldung auf Import-tag zutrifft.
	 */
	private boolean checkImportViolation (String importline, String to) {
		boolean violation = false;
		to = to.replace("$", ".");
		if (importline.endsWith("*;")) {
			//importline = db.* | to = db.DB
			importline = importline.replace("*", "");
			to = to.substring(0, to.lastIndexOf("."));
		} else {
			to = to + ";";
		}
			violation = importline.indexOf(to) >= 0;
			
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
			
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			
		} catch (CoreException e) {
		}
	}
	
	
	/**
	 * Entfernt Marker.
	 * @param file
	 */
	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
			
		} catch (CoreException ce) {
		}
	}
	
	/**
	 * Speichert das gesamte Projekt.
	 * @param monitor
	 * @throws CoreException
	 */
	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean run = store.getBoolean(PreferenceConstants.RUN_ON_FULL_BUILD);
		
		if (run) {
			try {
				getProject().accept(new SampleResourceVisitor(monitor));
			} catch (CoreException e) {
			}
		}
	}

	
	/**
	 * Speichert nur die neuen/veraenderten Resourcen des Projekts.
	 * 
	 * @param delta
	 * @param monitor
	 * @throws CoreException
	 */
	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean run = store.getBoolean(PreferenceConstants.RUN_ON_INCREMENTAL_BUILD);
		
		if (run) {
			delta.accept(new MackerDeltaVisitor(monitor));
		}

	}
	
	

	
	public static void main(String[] args) {

        
//new MackerBuilder().getImportClassName("import de.his.ap.se.Name;");
	
	}
	
}
