/**
 * 
 */
package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.util.ArrayList;
import java.util.HashMap;

import net.innig.macker.event.AccessRuleViolation;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerEvent;
import net.innig.macker.event.MackerEventListener;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.rule.RuleSet;

/**
 * Erweiterter MackerEventListener speichert Macker RegleverstoeÃŸe in einer
 * Liste.
 * 
 * @author Bender
 */
public class MackerListener implements  MackerEventListener {

	
	private HashMap<String, ArrayList<AccessRuleViolation>> violation;
	

	public MackerListener() {
		this.violation = new HashMap<String, ArrayList<AccessRuleViolation>>();
		
	}
	
	/**
	 * Handler speichert die relevanten Events in einer Liste.
	 */
	 public void handleMackerEvent(RuleSet ruleSet, MackerEvent event) {

		 if (event instanceof AccessRuleViolation) {
			 AccessRuleViolation e = (AccessRuleViolation) event;
			 
			 
			 if (violation.get(e.getFrom().getClassName()) == null) {
				 violation.put(e.getFrom().getClassName(), new ArrayList<AccessRuleViolation>()); 
			 } 
			
			 violation.get(e.getFrom().getClassName()).add(e);
			 
			
			 
		 }
	 }



	/**
	 * @return the violation
	 */
	public HashMap<String, ArrayList<AccessRuleViolation>> getViolation() {
		return violation;
	}

	/**
	 * @param violation the violation to set
	 */
	public void setViolation(HashMap<String, ArrayList<AccessRuleViolation>> violation) {
		this.violation = violation;
	}

	@Override
	public void mackerAborted(RuleSet arg0) {
		
	}

	@Override
	public void mackerFinished(RuleSet arg0) throws MackerIsMadException,
			ListenerException {
		
	}

	@Override
	public void mackerStarted(RuleSet arg0) throws ListenerException {
		
	}




}
