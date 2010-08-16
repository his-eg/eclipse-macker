package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import net.innig.macker.event.AccessRuleViolation;
import net.innig.macker.structure.ClassParseException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;


import de.his.core.tools.cs.sys.quality.eclipsemacker.gui.PreferenceConstants;
import de.his.core.tools.cs.sys.quality.eclipsemacker.gui.Property;


public class MackerBuilder extends IncrementalProjectBuilder {
	

    
    
private int count = 0;
private BuilderSettings builderSettings;
private CustomMacker cMa;


	public MackerBuilder() {
		this.builderSettings = new BuilderSettings();
		this.cMa = new CustomMacker();
	}

	
	/**
	 * @return the cMa
	 */
	public CustomMacker getcMa() {
		return cMa;
	}


	/**
	 * @param cMa the cMa to set
	 */
	public void setcMa(CustomMacker cMa) {
		this.cMa = cMa;
	}


	/**
	 * @return the builderSettings
	 */
	public BuilderSettings getBuilderSettings() {
		return builderSettings;
	}
	
	
	/**
	 * @param builderSettings the builderSettings to set
	 */
	public void setBuilderSettings(BuilderSettings builderSettings) {
		this.builderSettings = builderSettings;
	}
	
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

	class MackerResourceVisitor implements IResourceVisitor {
		IProgressMonitor monitor;

        /**
         * @param monitor
         */
        public MackerResourceVisitor(final IProgressMonitor monitor) {
            this.monitor = monitor;
        }
        
		public boolean visit(IResource resource) {
			checkMacker(resource, monitor);
		
			//return true to continue visiting children.
			return true;
		}
	}



	public static final String BUILDER_ID = "de.his.core.tools.cs.sys.quality.eclipsemacker.mackerBuilder";

	private static final String MARKER_TYPE = "de.his.core.tools.cs.sys.quality.eclipsemacker.mackerEvent";

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {

		cMa = new CustomMacker();
		
		if (getProject().getPersistentProperty(new QualifiedName("", PreferenceConstants.RULES_PATH)) == null) {
			new Property().init(getProject());
		}
		
		this.getBuilderSettings().setProject(getProject());
		this.getBuilderSettings().setProjectSettings();
		getBuilderSettings().addRulesToMacker(cMa);

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
		//gesammelten resourcen vom builder pruefen
		checkResources(monitor);
		
		return null;
	}

	
	private void checkResources (IProgressMonitor monitor) {
		
		if (cMa.hasRules() && cMa.hasClasses()) {
			monitor.subTask("Macker: Check Classes: ");
			monitor.worked(1);
			//Macker Classfile check
            if (cMa.checkClass()) {

            		try {
						importCheck(cMa, monitor);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

    				if (monitor.isCanceled()) {
    					return;
    				}
    				if (getBuilderSettings().isCheckContent()) {
    					//checkClassContent(cMa);
    				}
           }
		
		}

		monitor.done();
} 
	
	
	
	
	
	private boolean toCheck(IPath path) {
		boolean toCheck = false;

		StringTokenizer st = new StringTokenizer(getBuilderSettings().getFilterContent(), "\t");
	     //TODO property in gui um aussnahmen zu definieren
		if (path.toString().replace("\\", "/").indexOf("src/test/") > -1) {
			return false;
		}
		
		while (st.hasMoreTokens()) { 
			if (path.toString().indexOf(st.nextToken()) > -1 ) {
				return true;
			}
	     }
		return toCheck;
	}
	
	
	/**
	 * Eine veraenderte/neue java-Datei wird anahnd vorgegebner Macker-Rules
	 * auf Korrektheit geprueft.
	 * 
	 * 
	 * @param resource veraenderte Ressource.
	 */
	void checkMacker(IResource resource, IProgressMonitor monitor)  {
		


		/*
		 * Nur java Dateien sind fuer diesen Builder relevant.
		 */
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			count++;
			/*
			 * Die veranderte Ressource instanziieren.
			 */
			IFile javaFile = (IFile) resource;
			boolean run = getBuilderSettings().isUseFilter();
			
			boolean checkFilter = true;
			
			if (run) {
				checkFilter = toCheck(javaFile.getFullPath());
			} 
			
			if (checkFilter) {
				
				//IJavaProject javaProject = JavaCore.create(project);
				String projectName = resource.getProject().getName();
				//TEST monitor
				monitor.beginTask(javaFile.getName(), 8000);
				
				deleteMarkers(javaFile);
				File classFile = null;
				
				/*
				 *Bin file (*.Class) instanziieren.
				 */
				String src = getSourceFolder(javaFile, getBuilderSettings().getjProject());
				try {
				
					classFile = new File(javaFile.getLocation().toString().replace(src,
							getBuilderSettings().getjProject().getOutputLocation().toOSString().replace(projectName, ""))
							.replace("java", "class").replace("\\", "/"));

				} catch (CoreException e2) {
					e2.printStackTrace();
				}

				if (classFile != null) {	
					
					String classLoc = javaFile.getFullPath().toString().replace(src, "")
						.replace("/"+projectName+"/","").replace("/", ".").replace(".java", "");
					
					
					//cMa.getJavaMap().put(classFile.getName().replace(".class", ""), javaFile);
					cMa.getJavaMap().put(classLoc, javaFile);
					try {
						cMa.addClass(classFile);
					} catch (ClassParseException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					monitor.subTask("Pruefen: " +count+" "+ javaFile.getName());
				

				}
			}
		}
		
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

		for (int i = 0; i < getBuilderSettings().getClasspaths().size(); i++) {
			String vgl = getBuilderSettings().getClasspaths().get(i).replace("/"+projectName, "");
			//den aktuellen classpath ermitteln, durch vergleich mit java file path.
			if (javaFile.getFullPath().toOSString().indexOf(vgl) > -1) {
				src = getBuilderSettings().getClasspaths().get(i).replace("\\", "/").replace("/"+projectName, "");
			}
			
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
	private boolean importCheck(CustomMacker cm, IProgressMonitor monitor) throws CoreException {
		boolean erfolg = false;
		int c = 1;
		int s = cMa.getListener().getViolation().size();
		for (Map.Entry entry : cMa.getListener().getViolation().entrySet()) {
	    	monitor.subTask("Macker: Setze Marker: " + c+"/"+s);
			//marker setzen
			monitor.worked(1);
			InputStream in = null;
	
			in = ((IFile)cm.getJavaMap().get(entry.getKey())).getContents();
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
			
			try {
				
				String line = "";
				@SuppressWarnings("unchecked")
				ArrayList<AccessRuleViolation> tmp = (ArrayList<AccessRuleViolation>) cm.getListener().getViolation().get(entry.getKey());
				
			if (!getBuilderSettings().isCheckContent()) {
				
			
				while (reader.ready() && !line.startsWith("publicclass") && !line.startsWith("abstractclass") && cm.getListener().getViolation().get(entry.getKey()).size() > 0) {
					line = reader.readLine().replaceAll("\t", "").replaceAll(" ", "");
					if (line.startsWith("import")) {
						/*
						 * Ein Import-Tag wird auf Uebereinstimmung mit den gefundenen Macker-Events geprueft.
						 */
	
						//eine line kann mehrere marker besitzen
						for (int i = 0; i < cm.getListener().getViolation().get(entry.getKey()).size(); i++) {
	
							if (checkImportViolation(line, cm.getListener().getViolation().get(entry.getKey()).get(i).getTo().toString())) {
								
								setMarker(cm, reader.getLineNumber(), i, entry.getKey().toString());
								/*
								 * bei Uebereinstimmung betreffenden Event aus Liste entfernen.
								 */
								cm.getListener().getViolation().get(entry.getKey()).remove(i);
							}
						}
					}
				}
			} else {
				while (reader.ready()) {
					line = reader.readLine().replaceAll("\t", "").replaceAll(" ", "");
					if (!line.startsWith("package") && !line.startsWith("//") && !line.startsWith("/*")&& !line.startsWith("*") && !line.startsWith("/**")) {
						
						for (int i = 0; i < cm.getListener().getViolation().get(entry.getKey()).size(); i++) {
							String to = cm.getListener().getViolation().get(entry.getKey()).get(i).getTo().toString();
							int start = to.lastIndexOf(".") + 1;
							
							if (line.indexOf(to.substring(start)) > -1) {
								setMarker(cm, reader.getLineNumber(), i, entry.getKey().toString());
							}
						}

					}
				}
			}
				in.close();
				reader.close();
				//cm.getListener().getViolation().get(entry.getKey()).setViolation(tmp);
			} catch (IOException e) {
				erfolg = false;
				e.printStackTrace();
			}
			c++;
		}
		return erfolg;
		
		
		} 
	
	
	
	private void setMarker(CustomMacker cm, int line, int index, String className) {
		
		
		String severity = "";

		if (getBuilderSettings().isDefaultM()) {
			severity = "DEFAULT";
		} else if (getBuilderSettings().isError()) {
			severity = "ERROR";
		} else {
			severity = "WARNING";
		}
		
		/*
		 * Warnungen setzen, anhand der default Einstellungen oder angepasst. 
		 */
		String message = cm.getListener().getViolation().get(className).get(index)
			.getTo().toString();
		String source = message.substring(message.lastIndexOf(".")+1);
		
		if (ShowAs.valueOf(severity) == ShowAs.DEFAULT) {
			//Severity direkt vom Event holen
			severity = cm.getListener().getViolation().get(className).get(index).getRule().getSeverity().getName().toUpperCase();
			addMarker(cm.getJavaMap().get(className), "(" + source + ") " + cm.getListener().getViolation().get(className).get(index)
					.getMessages().get(0).toString(), line, setSeverity(ShowAs.valueOf(severity)));

		
		} else {
			//Severity anhand der angepassten Einstellung
			addMarker(cm.getJavaMap().get(className), "(" + source + ") " + cm.getListener().getViolation().get(className).get(index)
					.getMessages().get(0).toString(), line, setSeverity(ShowAs.valueOf(severity)));
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


		if (getBuilderSettings().isRunOnFullBuild()) {
			
			try {
				getProject().accept(new MackerResourceVisitor(monitor));
			} catch (CoreException e) {
			} finally {
				
				System.out.println("fertigF");
			}
		}
	}

	private void init() {
		
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
		
		if (getBuilderSettings().isRunOnIncBuild()) {
			delta.accept(new MackerDeltaVisitor(monitor));
		}

	}
	
	


	
	
	
	public static void main(String[] args) {

        
//new MackerBuilder().getImportClassName("import de.his.ap.se.Name;");
	
	}
	
}
