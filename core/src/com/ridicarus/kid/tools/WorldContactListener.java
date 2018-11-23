package com.ridicarus.kid.tools;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.tiles.InteractiveTileObject;

public class WorldContactListener implements ContactListener {
	public void beginContact(Contact contact) {
		Fixture fixA, fixB;
		int cdef;

		fixA = contact.getFixtureA();
		fixB = contact.getFixtureB();
		cdef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
		switch(cdef) {
			// mario hit his head on something
			case (GameInfo.MARIOHEAD_BIT | GameInfo.BANGABLE_BIT):
				// invoke mario's head hit method on the tile, so we can hit one tile at a time
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOHEAD_BIT)
					((PlayerRole) fixA.getUserData()).onHeadHit((InteractiveTileObject) fixB.getUserData());
				else
					((PlayerRole) fixB.getUserData()).onHeadHit((InteractiveTileObject) fixA.getUserData());
				break;
			// mario's foot hit a horizontal or vertical bound
			case (GameInfo.MARIOFOOT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOFOOT_BIT)
					((PlayerRole) fixA.getUserData()).onFootTouchBound((LineSeg) fixB.getUserData());
				else
					((PlayerRole) fixB.getUserData()).onFootTouchBound((LineSeg) fixA.getUserData());
				break;
			// mario touched a robot
			case (GameInfo.MARIO_ROBOT_SENSOR_BIT | GameInfo.ROBOT_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIO_ROBOT_SENSOR_BIT)
					((PlayerRole) fixA.getUserData()).onTouchRobot((RobotRole) fixB.getUserData());
				else
					((PlayerRole) fixB.getUserData()).onTouchRobot((RobotRole) fixA.getUserData());
				break;
			// robot touched horizontal or vertical bound
			case (GameInfo.ROBOT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.ROBOT_BIT)
					((RobotRole) fixA.getUserData()).onTouchBoundLine((LineSeg) fixB.getUserData());
				else
					((RobotRole) fixB.getUserData()).onTouchBoundLine((LineSeg) fixA.getUserData());
				break;
			// robot's foot touched horizontal or vertical bound
			case (GameInfo.ROBOTFOOT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.ROBOT_BIT)
					((RobotRole) fixA.getUserData()).onTouchGround();
				else
					((RobotRole) fixB.getUserData()).onTouchGround();
				break;
			// robot touched another robot
			case (GameInfo.ROBOT_BIT):
				((RobotRole) fixA.getUserData()).onTouchRobot((RobotRole) fixB.getUserData());
				((RobotRole) fixB.getUserData()).onTouchRobot((RobotRole) fixA.getUserData());
				break;
			// item touched horizontal or vertical bound
			case (GameInfo.ITEM_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.ITEM_BIT)
					((RobotRole) fixA.getUserData()).onTouchBoundLine((LineSeg) fixB.getUserData());
				else
					((RobotRole) fixB.getUserData()).onTouchBoundLine((LineSeg) fixA.getUserData());
				break;
			// an item touched mario
			case (GameInfo.MARIO_ROBOT_SENSOR_BIT | GameInfo.ITEM_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIO_ROBOT_SENSOR_BIT)
					((PlayerRole) fixA.getUserData()).onTouchItem((RobotRole) fixB.getUserData());
				else
					((PlayerRole) fixB.getUserData()).onTouchItem((RobotRole) fixA.getUserData());
				break;
			default:
				break;
		}
	}

	@Override
	public void endContact(Contact contact) {
		Fixture fixA, fixB;
		int cdef;

		fixA = contact.getFixtureA();
		fixB = contact.getFixtureB();
		cdef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
		switch(cdef) {
			case (GameInfo.MARIOFOOT_BIT | GameInfo.BOUNDARY_BIT):
				// invoke mario's foot hit method
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOFOOT_BIT)
					((PlayerRole) fixA.getUserData()).onFootLeaveBound((LineSeg) fixB.getUserData());
				else
					((PlayerRole) fixB.getUserData()).onFootLeaveBound((LineSeg) fixA.getUserData());
				break;
			case (GameInfo.ROBOTFOOT_BIT | GameInfo.BOUNDARY_BIT):
				// invoke robot 's foot hit method
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOFOOT_BIT)
					((RobotRole) fixA.getUserData()).onLeaveGround();
				else
					((RobotRole) fixB.getUserData()).onLeaveGround();
				break;
			default:
				break;
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
	}
}