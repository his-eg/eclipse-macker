package de.his.core.tools.cs.sys.quality.eclipsemacker.custommacker;


import java.util.HashMap;

import org.eclipse.core.resources.IFile;

import net.innig.macker.Macker;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.rule.RulesException;

/**
 * 
 * @author Bender
 *
 */

public class CustomMacker extends Macker{

	/**
	 * MackerEventListener, speichert Macker "AccessRuleViolation Events"
	 * in einer Map.
	 */
	private MackerListener listener;
	
	
	/**
	 * Diese Map enthaelt als Key einen PaketPfad zu einer CLASS Datei
	 * und als Value ein File Objekt der dazu gehoerigen JAVA Datei.
	 */
	private HashMap<String, IFile> javaMap;
	

	/**
	 * Erweitertes Macker Objekt um eine Map (javaMap) und einem
	 * MackerListener (listener).
	 * 
	 */

	public CustomMacker() {
		this.javaMap = new HashMap<String, IFile>();
		listener = new MackerListener();
		this.addListener(listener);
	}

	
	/**
	 * Class Datei wird anahnd der definierten Macker-Rules geprueft.
	 * 
	 */
	public boolean checkClass() {
		boolean erfolg = true;
		
		if (this.hasRules()) {
			
			try {
				this.check();

			} catch (RulesException e) {
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
	
	/**
	 * @return the javaMap
	 */
	public HashMap<String, IFile> getJavaMap() {
		return javaMap;
	}


	/**
	 * @param javaMap the javaMap to set
	 */
	public void setJavaMap(HashMap<String, IFile> javaMap) {
		this.javaMap = javaMap;
	}


	/**
	 * @return the MackerListener
	 */
	public MackerListener getListener() {
		return listener;
	}

	/**
	 * @param MackerListener the MackerListener to set
	 */
	public void setListener(MackerListener lis) {
		this.listener = lis;
	}

	
	
}
