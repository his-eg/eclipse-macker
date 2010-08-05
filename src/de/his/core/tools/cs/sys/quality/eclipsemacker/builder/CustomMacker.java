package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import net.innig.macker.Macker;
import net.innig.macker.event.AccessRuleViolation;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerEvent;
import net.innig.macker.event.MackerEventListener;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.rule.RuleSet;
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
	
	/**
	 * Macker Rules.
	 */
	private File rulesLay;
	
	/**
	 * Macker modRules.
	 */
	private File rulesMod;
	
	/**
	 * Erweitertes Macker Objekt.
	 * 
	 * @param fClass bin file.
	 * @param fJava src file.
	 * @param rules Macker Rules.
	 */
	public CustomMacker(File fClass, IFile fJava, File rules, File modRules) {
		
		this.javaClass = fClass;
		this.javaIFile = fJava;
		this.rulesLay = rules;
		this.rulesMod = modRules;
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


	public File getRulesXml() {
		return rulesLay;
	}


	public void setRulesXml(File rulesXml) {
		this.rulesLay = rulesXml;
	}


	public File getModularityRules() {
		return rulesMod;
	}


	public void setRulesMod(File rulesMod) {
		this.rulesMod = rulesMod;
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

	/**
	 * @return the s
	 */
	public File getLayeringRules() {
		return rulesLay;
	}

	/**
	 * @param s the s to set
	 */
	public void setLayeringRules(File s) {
		this.rulesLay = s;
	}


	/**
	 * Class Datei wird anahnd der definierten Macker-Rules geprueft.
	 * 
	 * TODO MackerIsMadException ?!
	 */
	public boolean checkClass() {
		boolean erfolg = true;
		
		System.out.println("Class datei gefunden: " + getJavaClass().exists());
		System.out.println("Rules.xml gefunden: " + getLayeringRules().exists());
		
		if (getJavaClass().exists() && getLayeringRules().exists() && getModularityRules().exists()) {
			
			try {
				this.addClass(getJavaClass());
				this.addRulesFile(getLayeringRules());
				this.addRulesFile(getModularityRules());
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
