package piotrrr.thesis.testing;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import piotrrr.thesis.bots.smartbot.SmartBot;
import piotrrr.thesis.bots.smartbot.SmartBotEntityRanking;
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
		float hd = SmartBotEntityRanking.getBotHealthDeficiency(bot, 0);
		float exp = 1 - bot.health/100f;
		assertTrue( hd == exp);
		
		hd = SmartBotEntityRanking.getBotHealthDeficiency(bot, 20);
		exp = 1 - (bot.health+20)/100f;
		assertTrue( hd == exp);
	}

	@Test
	public void testGetBotArmorDeficiency() {
		float hd = SmartBotEntityRanking.getBotArmorDeficiency(bot, 0);
		float exp = 1 - bot.armor/SmartBot.maxArmor;
		assertTrue( hd == exp);
		
		hd = SmartBotEntityRanking.getBotArmorDeficiency(bot, 50);
		exp = 1 - (bot.armor+50)/SmartBot.maxArmor;
		assertTrue( hd == exp);
	}

	@Test
	public void testGetBotWeaponDeficiency() {
		float rv = SmartBotEntityRanking.getBotWeaponDeficiency(bot, 0);
		float ev = 1;
		assertTrue( rv == ev);
		
		
		rv = SmartBotEntityRanking.getBotWeaponDeficiency(bot, 14);
		ev = 0.822f;
		assertTrue( floatsEqual(rv, ev));
		
		rv = SmartBotEntityRanking.getBotWeaponDeficiency(bot, 16);
		ev = 0.6f;
		assertTrue( rv == ev);
		
	}

	@Test
	public void testGetBotAmmoDeficiency() {
		float rv = SmartBotEntityRanking.getBotAmmoDeficiency(bot, 0);
		float ev = 1;
		assertTrue( rv == ev);
		
		rv = SmartBotEntityRanking.getBotAmmoDeficiency(bot, 22);
		ev = 0.8568182f;
		assertTrue( floatsEqual(rv, ev));
		
		rv = SmartBotEntityRanking.getBotAmmoDeficiency(bot, 21);
		ev = 0.936f;
		assertTrue( floatsEqual(rv, ev));
	}

	@Test
	public void testGetItemHealthBenefit() {
		EntityMock e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_HEALTH, Entity.SUBTYPE_LARGE);
		
		float rv = SmartBotEntityRanking.getItemHealthBenefit(bot, e);
//		Dbg.prn(rv);
		float ev = 0.199f;
		assertTrue( floatsEqual(rv, ev));
		
	}

	@Test
	public void testGetItemArmorBenefit() {
		EntityMock e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_ARMOR, Entity.SUBTYPE_COMBATARMOR);
		float rv = SmartBotEntityRanking.getItemArmorBenefit(bot, e);
//		Dbg.prn(rv);
		float ev = 0.5f;
		assertTrue( floatsEqual(rv, ev) );
		
		e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_ARMOR, Entity.SUBTYPE_ARMORSHARD);
		rv = SmartBotEntityRanking.getItemArmorBenefit(bot, e);
//		Dbg.prn(rv);
		ev = 0.009999f;
		assertTrue( floatsEqual(rv, ev) );
		
		e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_CHAINGUN, null);
		rv = SmartBotEntityRanking.getItemArmorBenefit(bot, e);
//		Dbg.prn(rv);
		ev = 0.0f;
		assertTrue( floatsEqual(rv, ev) );
		
	}

	@Test
	public void testGetItemWeaponBenefit() {
		EntityMock e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_RAILGUN, null);
		float rv = SmartBotEntityRanking.getItemWeaponBenefit(bot, e);
//		Dbg.prn(rv);
		float ev = 0.3999f;
		assertTrue( floatsEqual(rv, ev) );
		
	
	}

	@Test
	public void testGetItemAmmoBenefit() {
		EntityMock e = new EntityMock(Entity.CAT_ITEMS, Entity.TYPE_AMMO, Entity.SUBTYPE_BULLETS);
		float rv = SmartBotEntityRanking.getItemAmmoBenefit(bot, e);
		Dbg.prn(rv);
		float ev = 0.1f;
		assertTrue( floatsEqual(rv, ev) );

	}

}
