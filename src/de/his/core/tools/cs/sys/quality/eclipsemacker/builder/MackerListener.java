/**
 * 
 */
package de.his.core.tools.cs.sys.quality.eclipsemacker.builder;

import java.util.ArrayList;

import net.innig.macker.event.AccessRuleViolation;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerEvent;
import net.innig.macker.event.MackerEventListener;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.rule.RuleSet;

/**
 * @author Bender
 *
 */
public /**
 * Erweiterter MackerEventListener speichert Macker RegleverstoeÃŸe in einer
 * Liste.
 * 
 * @author Bender
 */

class MackerListener implements  MackerEventListener {

	
	private ArrayList<AccessRuleViolation> violation;
	

	public MackerListener() {
		this.violation = new ArrayList<AccessRuleViolation>();
	}
	
	/**
	 * Handler speichert die relevanten Events in einer Liste.
	 */
	 public void handleMackerEvent(RuleSet ruleSet, MackerEvent event) {
		 
		 if (event instanceof AccessRuleViolation) {
			 AccessRuleViolation e = (AccessRuleViolation) event;
			 this.getViolationList().add(e);
		 }
	 }

	 
	/**
	 * @return the ViolationList
	 */
	public ArrayList<AccessRuleViolation> getViolationList() {
		return violation;
	}

	/**
	 * @param ViolationList the ViolationList to set
	 */
	public void setV(ArrayList<AccessRuleViolation> v) {
		this.violation = v;
	}

	@Override
	public void mackerAborted(RuleSet arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mackerFinished(RuleSet arg0) throws MackerIsMadException,
			ListenerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mackerStarted(RuleSet arg0) throws ListenerException {
		// TODO Auto-generated method stub
		
	}




}
