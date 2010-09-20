package piotrrr.thesis.testing;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import piotrrr.thesis.bots.referencebot.ReferenceBot;
import piotrrr.thesis.common.navigation.FuzzyEntityRanking;
import piotrrr.thesis.tools.Dbg;
import soc.qase.state.Entity;

public class RankingTests {

	float maxError = 0.01f;
	
	SmartBotMock bot = new SmartBotMock();
	
	@Before
	public void setUp() throws Exception {
		bot.health = 80;
		bot.armor = 20;
	}
	
	private boolean floatsEqual(float a, float b) {
		if (Math.abs(a-b) <= (a+b)/2f*maxError) return true;
		return false;
	}

	@Test
	public void testGetBotHealthDeficiency() {
		float hd = FuzzyEntityRanking.getBotHealthDeficiency(bot, 0);
		float exp = 1 - bot.health/100f;
		assertTrue( hd == exp);
		
		hd = FuzzyEntityRanking.getBotHealthDeficiency(bot, 20);
		exp = 1 - (bot.health+20)/100f;
		assertTrue( hd == exp);
	}

	@Test
	public void testGetBotArmorDeficiency() {
		float hd = FuzzyEntityRanking.getBotArmorDeficiency(bot, 0);
		float exp = 1 - bot.armor/ReferenceBot.recommendedArmorLevel;
		assertTrue( hd == exp);
		
		hd = FuzzyEntityRanking.getBotArmorDeficiency(bot, 50);
		exp = 1 - (bot.armor+50)/ReferenceBot.recommendedArmorLevel;
		assertTrue( hd == exp);
	}

	@Test
	public void testGetBotWeaponDeficiency() {
		float rv = FuzzyEntityRanking.getBotWeaponDeficiency(bot, 0);
		float ev = 1;
		assertTrue( rv == ev);
		
		
		rv = FuzzyEntityRanking.getBotWeaponDeficiency(bot, 14);
		ev = 0.822f;
		assertTrue( floatsEqual(rv, ev));
		
		rv = FuzzyEntityRanking.getBotWeaponDeficiency(bot, 16);
		ev = 0.6f;
		assertTrue( rv == ev);
		
	}

	@Test
	public void testGetBotAmmoDeficiency() {
		float rv = FuzzyEntityRanking.getBotAmmoDeficiency(bot, 0);
		float ev = 1;
		assertTrue( rv == ev);
		
		rv = FuzzyEntityRanking.getBotAmmoDeficiency(bot, 22);
		ev = 0.8568182f;
		assertTrue( floatsEqual(rv, ev));
		
		rv = FuzzyEntityRanking.getBotAmmoDeficiency(bot, 21);
		ev = 0.936f;
		assertTrue( floatsEqual(rv, ev));
	}

	@Test
	public void testGetItemHealthBenefit() {
		EntityMock e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_HEALTH, Entity.SUBTYPE_LARGE);
		
		float rv = FuzzyEntityRanking.getItemHealthBenefit(bot, e);
//		Dbg.prn(rv);
		float ev = 0.199f;
		assertTrue( floatsEqual(rv, ev));
		
	}

	@Test
	public void testGetItemArmorBenefit() {
		EntityMock e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_ARMOR, Entity.SUBTYPE_COMBATARMOR);
		float rv = FuzzyEntityRanking.getItemArmorBenefit(bot, e);
//		Dbg.prn(rv);
		float ev = 0.5f;
		assertTrue( floatsEqual(rv, ev) );
		
		e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_ARMOR, Entity.SUBTYPE_ARMORSHARD);
		rv = FuzzyEntityRanking.getItemArmorBenefit(bot, e);
//		Dbg.prn(rv);
		ev = 0.009999f;
		assertTrue( floatsEqual(rv, ev) );
		
		e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_CHAINGUN, null);
		rv = FuzzyEntityRanking.getItemArmorBenefit(bot, e);
//		Dbg.prn(rv);
		ev = 0.0f;
		assertTrue( floatsEqual(rv, ev) );
		
	}

	@Test
	public void testGetItemWeaponBenefit() {
		EntityMock e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_RAILGUN, null);
		float rv = FuzzyEntityRanking.getItemWeaponBenefit(bot, e);
//		Dbg.prn(rv);
		float ev = 0.3999f;
		assertTrue( floatsEqual(rv, ev) );
		
	
	}

	@Test
	public void testGetItemAmmoBenefit() {
		EntityMock e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_AMMO, Entity.SUBTYPE_BULLETS);
		float rv = FuzzyEntityRanking.getItemAmmoBenefit(bot, e);
		Dbg.prn(rv);
		float ev = 0.1f;
		assertTrue( floatsEqual(rv, ev) );

	}

}
