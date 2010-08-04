package piotrrr.thesis.testing;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import piotrrr.thesis.bots.tuning.CombatConfig;

public class CombatConfigTest {
	
	CombatConfig cnf;
	
	@Before
	public void init() {
		cnf = new CombatConfig();
	}

	@Test
	public void testGetParameter1() {
		boolean ex = false;
		float par1 = ((CombatConfig)cnf).maxPredictionError;
		float par2 = 0;
		
		try {
			par2 = cnf.getParameter("maxPredictionError");
		} catch (Exception e) {
			e.printStackTrace();
			ex = true;
		}
		
		assertTrue(par1 == par2);
		assertTrue( ! ex );
		
	}
	
	@Test
	public void testGetParameter2() {
		boolean ex = false;
		
		try {
			cnf.getParameter("nic");
		} catch (Exception e) {
			ex = true;
		}
		
		assertTrue( ex );
	}
	
	@Test
	public void getParameterMin() {
		boolean ex = false;
		float par1 = ((CombatConfig)cnf).maxPredictionError_MIN;
		float par2 = 0;
		
		try {
			par2 = cnf.getParameterMin("maxPredictionError");
		} catch (Exception e) {
			e.printStackTrace();
			ex = true;
		}
		
		assertTrue(par1 == par2);
		assertTrue( ! ex );
	}
	
	@Test
	public void getParameterMax() {
		boolean ex = false;
		float par1 = ((CombatConfig)cnf).maxPredictionError_MAX;
		float par2 = 0;
		
		try {
			par2 = cnf.getParameterMax("maxPredictionError");
		} catch (Exception e) {
			e.printStackTrace();
			ex = true;
		}
		
		assertTrue(par1 == par2);
		assertTrue( ! ex );
	}
	
	@Test
	public void isParameterInteger() {
		boolean ex = false;
		try {
			assertTrue( cnf.isParameterInteger("maxEnemyInfoAge4Firing"));
			assertTrue( ! cnf.isParameterInteger("maxPredictionError"));
		} catch (Exception e) {
			ex = true;
			e.printStackTrace();
		}
		
		assertTrue( ! ex );
		
	}

	


}
