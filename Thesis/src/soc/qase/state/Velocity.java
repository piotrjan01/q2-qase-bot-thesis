//---------------------------------------------------------------------
// Name:			Velocity.java
// Author:			Martin.Fredriksson@bth.se
// Author:			Bernard.Gorman@computing.dcu.ie
//---------------------------------------------------------------------

package soc.qase.state;

import piotrrr.thesis.gui.AppConfig;
import soc.qase.tools.Utils;

/*-------------------------------------------------------------------*/
/**	Wrapper class for velocity attributes. The Velocity class is used
 *	when a QASE agent is trying to change its self state in a simulated
 *	environment. The class is implemented as a wrapper of functionality
 *	related to Quake2 client control, i.e. the desired velocity along
 *	the X axis, Y axis, and Z axis. The velocity values can range between
 *	-400 and 400. */
/*-------------------------------------------------------------------*/
public class Velocity
{
	private int forward = -1;
	private int right = -1;
	private int up = -1;

        
	public static final int FORWARD = 0, RIGHT = 1, UP = 2;
	public static final int STOP = 0, WALK = (int)(110), RUN = (int)(400);

/*-------------------------------------------------------------------*/
/**	Constructor. Negative values correspond to the opposite 
 *	direction.
 *	@param forward forward velocity.
 *	@param right right velocity.
 *	@param up up velocity. */
/*-------------------------------------------------------------------*/
	public Velocity(int forward, int right, int up)
	{
		setForward(forward);
		setRight(right);
		setUp(up);
	}
	
/*-------------------------------------------------------------------*/
/**	Constructor. Set this Velocity object to the same values as an
 *	existing Velocity object
 *	@param velocity source. */
/*-------------------------------------------------------------------*/
	public Velocity(Velocity velocity)
	{
		setForward(velocity.getForward());
		setRight(velocity.getRight());
		setUp(velocity.getUp());
	}
	
/*-------------------------------------------------------------------*/
/**	Default constructor. */
/*-------------------------------------------------------------------*/
	public Velocity()
	{	}

/*-------------------------------------------------------------------*/
/**	Get the velocity in a particular direction.
 *	@param velocityType the direction whose velocity should be returned,
 *	as defined by the constants listed above */
/*-------------------------------------------------------------------*/
	public int get(int velocityType)
	{
		if(velocityType == FORWARD)
			return forward;
		else if(velocityType == RIGHT)
			return right;
		else if(velocityType == UP)
			return up;

		return Integer.MIN_VALUE;
	}

/*-------------------------------------------------------------------*/
/**	Get forward velocity.
 *	@return forward velocity. */
/*-------------------------------------------------------------------*/
	public int getForward()
	{
		return forward;
	}

/*-------------------------------------------------------------------*/
/**	Get right velocity.
 *	@return right velocity. */
/*-------------------------------------------------------------------*/
	public int getRight()
	{
		return right;
	}

/*-------------------------------------------------------------------*/
/**	Get up velocity.
 *	@return up velocity. */
/*-------------------------------------------------------------------*/
	public int getUp()
	{
		return up;
	}

/*-------------------------------------------------------------------*/
/**	Get byte array representation of object. Places the representation
 *	at the appropriate offset in the argument array. Called by the Move
 *	class when compiling a byte representation of the client's movement
 *	for transmission to the server.
 *	@see Move#getBytes */
/*-------------------------------------------------------------------*/
	public void getBytes(byte[] moveBytes, int offset)
	{
		Utils.shortToByteArray((short)forward, moveBytes, offset + 7);
		Utils.shortToByteArray((short)right, moveBytes, offset + 9);
		Utils.shortToByteArray((short)up, moveBytes, offset + 11);
	}

/*-------------------------------------------------------------------*/
/**	Set velocity in all directions simultaneously.
 *	@param forward forward velocity.
 *	@param right right velocity.
 *	@param up up velocity. */
/*-------------------------------------------------------------------*/
	public void set(int forward, int right, int up)
	{
		this.up = up;
		this.right = right;
		this.forward = forward;
	}

/*-------------------------------------------------------------------*/
/**	Set velocity in a specific directions.
 *	@param velocityType the direction to which the velocity value should
 *	be applied, as defined by the constants listed above
 *	@param value the velocity to be applied in the given direction */
/*-------------------------------------------------------------------*/
	public void set(int velocityType, int value)
	{
		if(velocityType == FORWARD)
			forward = value;
		else if(velocityType == RIGHT)
			right = value;
		else if(velocityType == UP)
			up = value;
	}

/*-------------------------------------------------------------------*/
/**	Set forward velocity.
 *	@param forward forward velocity. */
/*-------------------------------------------------------------------*/
	public void setForward(int forward)
	{
		this.forward = forward;
	}

/*-------------------------------------------------------------------*/
/**	Set right velocity.
 *	@param right right velocity. */
/*-------------------------------------------------------------------*/
	public void setRight(int right)
	{
		this.right = right;
	}

/*-------------------------------------------------------------------*/
/**	Set up velocity.
 *	@param up up velocity. */
/*-------------------------------------------------------------------*/
	public void setUp(int up)
	{
		this.up = up;
	}

/*-------------------------------------------------------------------*/
/**	Merge Velocity properties from an existing Velocity object into the
 *	current Velocity object. Used when assimilating cumulative updates
 *	from the Quake 2 server into the gamestate.
 *	@param velocity source Velocity whose attributes should be merged
 *	into the current Velocity
 *	@see soc.qase.state.World#setEntity(Entity, boolean) */
/*-------------------------------------------------------------------*/
	public void merge(Velocity velocity)
	{
		if(velocity != null)
		{
			if(forward == -1) forward = velocity.getForward();
			if(right == -1) right = velocity.getRight();
			if(up == -1) up = velocity.getUp();
		}
	}
}

