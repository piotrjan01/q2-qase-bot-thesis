package piotrrr.thesis.common.combat;

/**
 * Represents the decision the bot takes about firing
 * @author Piotr Gwizdała
 */
public class FiringDecision {

    /**
     * the enemy at which the bot shall shoot
     */
    public EnemyInfo enemyInfo;
    /**
     * The gun index which it shall use
     */
    public int gunIndex;

    /**
     * the basic constructor
     * @param enemy enemy at which to shoot
     * @param gunIndex which gun to use
     */
    public FiringDecision(EnemyInfo enemy, int gunIndex) {
        this.enemyInfo = enemy;
        this.gunIndex = gunIndex;
    }
}
