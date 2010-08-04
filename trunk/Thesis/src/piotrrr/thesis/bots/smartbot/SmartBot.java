package piotrrr.thesis.bots.smartbot;


import piotrrr.thesis.bots.tuning.NavConfig;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.combat.SimpleAimingModule;
import piotrrr.thesis.common.combat.SimpleCombatModule;
import piotrrr.thesis.common.navigation.NavInstructions;
import piotrrr.thesis.tools.Dbg;


public class SmartBot extends MapBotBase {
	
	public NavConfig nConfig = new NavConfig();

	public SmartBot(String botName, String skinName) {
		super(botName, skinName);
		globalNav = new SmartBotGlobalNav();
		localNav = new SmartBotLocalNav();
	}
	
	@Override
	protected void botLogic() {
		super.botLogic();
		
		NavInstructions ni = null;
		if ( ! noMove) {
			plan = globalNav.establishNewPlan(this, plan);
			if (plan == null) {
				Dbg.prn("plan is null....");
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
		
		executeInstructions(ni, fi);
	}
		
}
