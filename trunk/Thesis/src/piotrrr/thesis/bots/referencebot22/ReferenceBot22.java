package piotrrr.thesis.bots.referencebot22;

import piotrrr.thesis.common.fsm.NeedsFSM;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.combat.SimpleAimingModule;
import piotrrr.thesis.common.combat.SimpleCombatModule;
import piotrrr.thesis.common.fsm.StateBot;
import piotrrr.thesis.common.jobs.StateReporter;
import piotrrr.thesis.common.navigation.NavInstructions;


public class ReferenceBot22 extends MapBotBase implements StateBot {
	
	/**
	 * Finite state machine - used to determine bot's needs.
	 */
	NeedsFSM fsm;

	/**
	 * The job that reports the bot's state and state changes.
	 */
	public StateReporter stateReporter;

	public ReferenceBot22(String botName, String skinName) {
		super(botName, skinName);
		
		fsm = new NeedsFSM(this);
		
		stateReporter = new StateReporter(this, this);
		addBotJob(stateReporter);
		
		globalNav = new ReferenceBotGlobalNav222();
		localNav = new ReferenceBotLocalNav22();
	}
	
	
	@Override
	protected void botLogic() {
		super.botLogic();
	
		NavInstructions ni = null;
		if ( ! noMove) {
			plan = globalNav.establishNewPlan(this, plan);
			if (plan == null) {
				// ??
				return;
			}
			assert plan != null;
			ni = localNav.getNavigationInstructions(this);
		}
		
		FiringDecision fd =  null;
		if ( ! noFire ) {
			fd = SimpleCombatModule.getFiringDecision(this);
			if (fd != null && getWeaponIndex() != fd.gunIndex) changeWeaponByInventoryIndex(fd.gunIndex);
			else {
				int justInCaseWeaponIndex = SimpleCombatModule.chooseWeapon(this, cConfig.maxShortDistance4WpChoice+0.1f);
				if (getWeaponIndex() != justInCaseWeaponIndex)
					changeWeaponByInventoryIndex(justInCaseWeaponIndex);
			}
		}
		
		FiringInstructions fi = SimpleAimingModule.getFiringInstructions(fd, this);
		
		executeInstructions(ni,	fi);
	}
	
	/**
	 * Returns the current name of the state of bot's finite state machine.
	 * @return state name
	 */
	public String getCurrentStateName() {
		String stateName =  fsm.getCurrentStateName();
		return stateName.substring(stateName.lastIndexOf(".")+1);
	}
	
	@Override
	public String toDetailedString() {
		
		return "Bot name: "+getBotName()+"\n"+
				"health: "+getBotHealth()+"\n"+
				"armor: "+getBotArmor()+"\n"+
				"state name: "+getCurrentStateName()+"\n"+
				"frame nr: "+getFrameNumber()+"\n"+
				"position: "+getBotPosition()+"\n"+
				kb.toString();
	}
	
	

}
