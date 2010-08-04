package piotrrr.thesis.common.fsm;


import piotrrr.thesis.common.entities.EntityTypeDoublePair;

/**
 * The interface of the state that can be used by NeedsFSM.
 * @author Piotr Gwizda�a
 * @see NeedsFSM
 */
public class State {
	
	/**
	 * Returns the array of entity types desired by bot in current state
	 * along with how much the bot desires those entities.
	 * @return
	 */
	EntityTypeDoublePair [] getDesiredEntities() {
		return null;
	}
	
	/**
	 * Returns the next state basing on some values.
	 * @return
	 */
	State getNextState() {
		return null;
	}

}
