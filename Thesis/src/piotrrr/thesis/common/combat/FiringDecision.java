package piotrrr.thesis.common.combat;


public class FiringDecision {
	
	public EnemyInfo enemyInfo;
	
	public int gunIndex;
	
	public FiringDecision(EnemyInfo enemy, int gunIndex) {
		this.enemyInfo = enemy;
		this.gunIndex = gunIndex;
	}
	
}
