package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import net.innig.macker.Macker;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.rule.RulesException;
import net.innig.macker.structure.ClassParseException;

/**
 * 
 * @author Bender
 *
 */

public class CustomMacker extends Macker{
	public static ArrayList<CustomMacker> all = new ArrayList<CustomMacker>();

	/**
	 * MackerEventListener, speichert Macker "AccessRuleViolation Events"
	 * in einer ArrayList.
	 */
	private MackerListener listener;
	
	/**
	 * Zu pruefende Class Datei.
	 * Bin File.
	 */
	private File javaClass;
	
	/**
	 * Zu pruefendes IFile Objekt.
	 * Src File.
	 */
	
	private IFile javaIFile;
	
	
	private ArrayList<File> ruleFiles; 
	/**
	 * Erweitertes Macker Objekt.
	 * 
	 * @param fClass bin file.
	 * @param fJava src file.
	 * @param rules Macker Rules.
	 */
	public CustomMacker(File fClass, IFile fJava, String rulePath) {
		
		this.javaClass = fClass;
		this.javaIFile = fJava;
		this.ruleFiles = getRulesFromDirectory(rulePath);
		/*
		 * Listener speichert von Macker identifzierte
		 * Regelverstoesse vom Typ AccessRuleViolation.
		 */
		listener = new MackerListener();
		this.addListener(listener);
		
		all.add(this);
	}
	
	
	public CustomMacker() {
		
	}

	
	public ArrayList<File> getRulesFromDirectory(String path) {
		
		File dir = new File(path);
		ArrayList<File> ruleFiles = new ArrayList<File>();
		
		if (dir.exists() && dir.isDirectory()) {
			File[] fileList = dir.listFiles();
		
			for(File f : fileList) {
				if (f.getName().endsWith(".xml")) {
					ruleFiles.add(f);
				}
			}
		}
		return ruleFiles;
		
	}
	

	/**
	 * @return the javaClass
	 */
	public File getJavaClass() {
		return javaClass;
	}

	/**
	 * @param javaClass the javaClass to set
	 */
	public void setJavaClass(File javaClass) {
		this.javaClass = javaClass;
	}

	/**
	 * @return the javaIFile
	 */
	public IFile getJavaIFile() {
		return javaIFile;
	}

	/**
	 * @param javaIFile the javaIFile to set
	 */
	public void setJavaIFile(IFile javaIFile) {
		this.javaIFile = javaIFile;
	}

	/**
	 * @return the lis
	 */
	public MackerListener getListener() {
		return listener;
	}

	/**
	 * @param lis the lis to set
	 */
	public void setListener(MackerListener lis) {
		this.listener = lis;
	}


	public ArrayList<File> getRuleFiles() {
		return ruleFiles;
	}


	public void setRuleFiles(ArrayList<File> ruleFiles) {
		this.ruleFiles = ruleFiles;
	}

	/**
	 * Class Datei wird anahnd der definierten Macker-Rules geprueft.
	 * 
	 * TODO MackerIsMadException ?!
	 */
	public boolean checkClass() {
		boolean erfolg = true;
		
		if (getJavaClass().exists() && getRuleFiles().size() > 0) {
			
			try {
				this.addClass(getJavaClass());
				
				for (File f: getRuleFiles()) {
					this.addRulesFile(f);
				}

				this.check();
				
			} catch (RulesException e) {
				erfolg = false;
				e.printStackTrace();
			} catch (IOException e) {
				erfolg = false;
				e.printStackTrace();
			} catch (ClassParseException e) {
				erfolg = false;
				e.printStackTrace();
			} catch (ListenerException e) {
				erfolg = false;
				e.printStackTrace();
			} catch (MackerIsMadException e) {
				//
			}
			
		} else {
			erfolg = false;
		}
		
		return erfolg;

	}
	
	
}
