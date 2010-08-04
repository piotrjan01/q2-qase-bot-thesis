package piotrrr.thesis.common.fsm;

import piotrrr.thesis.bots.referencebot.*;
import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.common.entities.EntityType;
import piotrrr.thesis.common.entities.EntityTypeDoublePair;

/**
 * This state should be used by bot when it searches for some health packages.
 * @author Piotr Gwizda�a
 */
class HealingState extends State {
	
	BotBase bot;
	
	
	
	HealingState(BotBase bot) {
		this.bot = bot;
	}

	@Override
	EntityTypeDoublePair []  getDesiredEntities() {
		EntityTypeDoublePair []  ret = { 
				new EntityTypeDoublePair(EntityType.HEALTH, 0.9),
				new EntityTypeDoublePair(EntityType.ARMOR, 0.6),
				new EntityTypeDoublePair(EntityType.WEAPON, 0.2),
				new EntityTypeDoublePair(EntityType.AMMO, 0.2)
		};
		return ret;
	}

	@Override
	State getNextState() {
		float wellness = NeedsFSM.getBotWellness(bot);
		float firepower = NeedsFSM.getBotFirePower(bot);
		if (wellness >= NeedsFSM.GOOD_WELLNESS 
				&& firepower >= NeedsFSM.GOOD_FIRE_POWER) return new FightingState(bot);
		if (wellness >= NeedsFSM.GOOD_WELLNESS) return new ArmingState(bot);
		return this;
	}
	
}
