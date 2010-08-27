package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Date;
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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import de.his.core.tools.cs.sys.quality.eclipsemacker.custommacker.CustomMacker;
import de.his.core.tools.cs.sys.quality.eclipsemacker.custommacker.ShowAs;
import de.his.core.tools.cs.sys.quality.eclipsemacker.gui.PreferenceConstants;
import de.his.core.tools.cs.sys.quality.eclipsemacker.gui.Property;


public class MackerBuilder extends IncrementalProjectBuilder {
	

    
    
private int count = 0;
private int count2 = 0;
private BuilderSettings builderSettings;
private CustomMacker customMacker;
public static ArrayList<String> errorsB = new ArrayList<String>();

	public MackerBuilder() {
		this.builderSettings = new BuilderSettings();
		this.customMacker = new CustomMacker();
	}

	
	/**
	 * @return the cMa
	 */
	public CustomMacker getcMa() {
		return customMacker;
	}


	/**
	 * @param cMa the cMa to set
	 */
	public void setcMa(CustomMacker cMa) {
		this.customMacker = cMa;
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
		Date start = new Date();
		customMacker = new CustomMacker();
		count = 0;
		
		if (getProject().getPersistentProperty(new QualifiedName("", PreferenceConstants.SOURCE_FILTER)) == null) {
			new Property().init(getProject());
		}
		
		
		this.getBuilderSettings().setProject(getProject());
		this.getBuilderSettings().setProjectSettings();
		getBuilderSettings().addRulesToMacker(customMacker);

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
		Date end = new Date();
		errorsB.add("#7 : " + ((end.getTime() - start.getTime())/1000));

		return null;
	}

	
	private void checkResources (IProgressMonitor monitor) {
		
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
		
		}

		monitor.done();
} 
	
	
	
	
	
	private boolean toCheck(String path) {
		boolean toCheck = false;

		StringTokenizer st = new StringTokenizer(getBuilderSettings().getFilterContent(), "\t");
		
		while (st.hasMoreTokens()) { 
			if (path.indexOf(st.nextToken()) > -1 ) {
				
				if (getBuilderSettings().isUseSourceFilter()) {
					for (String s : getBuilderSettings().getSources()) {
						
						if (path.indexOf(s) > -1) {
							return true;
						}
					}
				} else {
					return true;
				}
				
				
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
			
			/*
			 * Die veranderte Ressource instanziieren.
			 */
			IFile javaFile = (IFile) resource;
			String fullP = javaFile.getFullPath().toString();
			
			boolean run = getBuilderSettings().isUseFilter();
			
			boolean checkFilter = true;
			//TEST1 
			if (run) {
				checkFilter = toCheck(fullP);
			} 
			
			if (checkFilter) {
				
				//test direkter zugriff auf class files, er findet jedoch nicht alle.
				
//				IRegion region = JavaCore.newRegion();
//				IJavaElement je = JavaCore.create(javaFile);
//				region.add(je);
//				File classFile = null;
//				IResource[] resources = null;
//				try {
//					resources = JavaCore.getGeneratedResources(region, false);
//
//					classFile = new File(resources[0].getLocation().toString());
//					count++;
//				} catch(Exception e) {
//					errorsB.add("#8 " + fullP);
//					count = count-1;
//				}
				//test ende
				
				
				//test2 imports direkt holen, jedoch keine zeilenenummer
//				ICompilationUnit javaCompU = JavaCore.createCompilationUnitFrom(javaFile);
//				try {
//					ISourceRange sr = javaCompU.getImports()[0].getSourceRange();
//					System.out.println(sr.toString());
//					System.out.println(javaCompU.getImports()[0].));
//				} catch (JavaModelException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				//test2 ende
				
				//IJavaProject javaProject = JavaCore.create(project);
				String projectName = resource.getProject().getName();
				//TEST monitor
				monitor.beginTask(javaFile.getName(), 8000);
				
				deleteMarkers(javaFile);
				//File classFile = null;

				//Bin file (*.Class) instanziieren.
				File classFile = null;
				String src = getSourceFolder(fullP, projectName);
				try {
				
					classFile = new File(javaFile.getLocation().toString().replace(src,
							getBuilderSettings().getjProject().getOutputLocation().toOSString().replace(projectName, ""))
							.replace(".java", ".class").replace("\\", "/"));
					
				} catch (CoreException e2) {
					e2.printStackTrace();
				}

				if (classFile != null) {	
					//String i = resources[0].getFullPath().toString();
					
					String classLoc = fullP.replace(src, "")
						.replace("/"+projectName+"/","").replace(".java", "").replace("/", ".");
//					String classLoc = i.substring(i.replace("/", ".")
//							.indexOf("de."), i.length()).replace("/"+projectName+"/","")
//							.replace(".class", "").replace("/", ".");
					count++;
					customMacker.getJavaMap().put(classLoc, javaFile);
					try {
						customMacker.addClass(classFile);
						count2++;
					} catch (ClassParseException e) {
						errorsB.add("#12 " + fullP + " e");
					} catch (IOException e) {
						errorsB.add("#11 " + classLoc + " " + classFile.exists() + " " + e.getMessage());
						}
					monitor.subTask("Macker, Lade Klassen: " +count+" "+ javaFile.getName());
				

				} else {
						errorsB.add("#9 " + fullP + " resource null");
					
					
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
	
	private String getSourceFolder(String path, String pName) {
		String src = "";

		for (int i = 0; i < getBuilderSettings().getClasspaths().size(); i++) {
			String vgl = getBuilderSettings().getClasspaths().get(i).replace("\\", "/");
			//den aktuellen classpath ermitteln, durch vergleich mit java file path.
			if (path.indexOf(vgl) > -1) {
				src = getBuilderSettings().getClasspaths().get(i).replace("\\", "/").replace("/"+getProject().getName(), "");
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
	private boolean importCheck(IProgressMonitor monitor) throws CoreException {
		boolean erfolg = false;
		int c = 1;
		int s = customMacker.getListener().getViolation().size();
		InputStream in = null;
		LineNumberReader reader = null;
		
		for (Map.Entry entry : customMacker.getListener().getViolation().entrySet()) {
	    	monitor.subTask("Macker, Setze Warnungen: " + c+"/"+s);
			//marker setzen
			monitor.worked(1);
			
	
			in = ((IFile)customMacker.getJavaMap().get(entry.getKey())).getContents();
			reader = new LineNumberReader(new InputStreamReader(in));
			
			try {
				
				String line = "";
				@SuppressWarnings("unchecked")
				ArrayList<AccessRuleViolation> tmp = (ArrayList<AccessRuleViolation>) customMacker.getListener().getViolation().get(entry.getKey());
				
			if (!getBuilderSettings().isCheckContent()) {
				erfolg = checkImports(reader, entry);
			} else {
				erfolg = checkFullContent(reader, entry);
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
	
	
	private boolean checkImports(LineNumberReader reader, Map.Entry entry) throws IOException {
		String line = "";
		while (reader.ready() && !line.startsWith("public class") && !line.startsWith("abstract class") && customMacker.getListener().getViolation().get(entry.getKey()).size() > 0) {
			line = reader.readLine().trim();
			if (line.startsWith("import")) {
				// Ein Import-Tag wird auf Uebereinstimmung mit den gefundenen Macker-Events geprueft.
				//eine line kann mehrere marker besitzen
				for (int i = 0; i < customMacker.getListener().getViolation().get(entry.getKey()).size(); i++) {

					if (checkImportLineViolation(line, customMacker.getListener().getViolation().get(entry.getKey()).get(i).getTo().toString())) {
						
						setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());			
						  //bei Uebereinstimmung betreffenden Event aus Liste entfernen.
						customMacker.getListener().getViolation().get(entry.getKey()).remove(i);
					}
				}
			}
		}
		return true;
	}
	

	private boolean checkFullContent(LineNumberReader reader, Map.Entry entry) throws IOException {
		String line = "";
		
		while (reader.ready()) {
			line = reader.readLine().trim();
			
			if (!line.startsWith("package") && !line.startsWith("//") && !line.startsWith("/*")&& !line.startsWith("*") && !line.startsWith("/**")) {
				
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
							//statischer zugriff auf die klasse	
							} else if (t.indexOf(".") > -1 ) {
								if ((t.substring(0, t.indexOf(".")).equals(to.substring(start)))) {
									setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
									gefunden = true;
								}
							//verwendung der klasse als exception (im ty/catch block)
							}  else if (t.startsWith("(")) {
								if (((t.replace("(", ""))).equals(to.substring(start))) {
									setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
									gefunden = true;
								}
								
							}  
							//verwendung der klasse als parameter
							if (line.startsWith("public") || line.startsWith("private") || line.startsWith("protected")) {
								//erster parameter der methode
								if (line.indexOf("("+to.substring(start)+" ") > -1) {
									setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
									gefunden = true;
								//folgender parameter
								} else if (line.indexOf(", "+to.substring(start)+" ") > -1) {
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
							} else if ((line.indexOf("(" + to.substring(start) + ")") > -1 )) {
								setMarker(customMacker, reader.getLineNumber(), i, entry.getKey().toString());
								gefunden = true;
							//verwenung der klasse in einer liste	
							} else if ((line.indexOf("<" + to.substring(start) + ">") > -1 )) {
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
	private boolean checkImportLineViolation (String importline, String to) {
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
			
				
				errorsB.add("#5 : " + count);
				errorsB.add("#6 : " + count2);
				
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
		
		if (getBuilderSettings().isRunOnIncBuild()) {
			delta.accept(new MackerDeltaVisitor(monitor));
		}

	}
	
	


	
	
	
	public static void main(String[] args) {

        
//new MackerBuilder().getImportClassName("import de.his.ap.se.Name;");
	
	}
	
}