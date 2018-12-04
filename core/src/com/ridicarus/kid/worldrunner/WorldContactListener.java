package com.ridicarus.kid.worldrunner;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.MobileRobotRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.robot.GroundCheckBot;
import com.ridicarus.kid.roles.robot.SMB.PipeEntrance;
import com.ridicarus.kid.tiles.InteractiveTileObject;
import com.ridicarus.kid.bodies.PlayerBody;

public class WorldContactListener implements ContactListener {
	public void beginContact(Contact contact) {
		Fixture fixA, fixB;
		int cdef;
		fixA = contact.getFixtureA();
		fixB = contact.getFixtureB();
		cdef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
		switch(cdef) {
			// mario's head started touching an interactive tile
			case (GameInfo.MARIOHEAD_BIT | GameInfo.BANGABLE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOHEAD_BIT)
					((PlayerBody) fixA.getUserData()).onHeadTileContactStart((InteractiveTileObject) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onHeadTileContactStart((InteractiveTileObject) fixA.getUserData());
				break;
			// mario touched a despawn box
			case (GameInfo.MARIO_BIT | GameInfo.DESPAWN_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIO_BIT)
					((PlayerBody) fixA.getUserData()).onTouchDespawn();
				else
					((PlayerBody) fixB.getUserData()).onTouchDespawn();
				break;
			// mario's foot hit a pipe
			case (GameInfo.MARIOHEAD_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOHEAD_BIT)
					((PlayerBody) fixA.getUserData()).onStartTouchPipe((PipeEntrance) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onStartTouchPipe((PipeEntrance) fixA.getUserData());
				break;
			// mario's side hit a pipe
			case (GameInfo.MARIOSIDE_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOSIDE_BIT)
					((PlayerBody) fixA.getUserData()).onStartTouchPipe((PipeEntrance) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onStartTouchPipe((PipeEntrance) fixA.getUserData());
				break;
			// mario's foot hit a pipe
			case (GameInfo.MARIOFOOT_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOFOOT_BIT)
					((PlayerBody) fixA.getUserData()).onStartTouchPipe((PipeEntrance) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onStartTouchPipe((PipeEntrance) fixA.getUserData());
				break;
			// mario's foot hit a horizontal or vertical bound
			case (GameInfo.MARIOFOOT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOFOOT_BIT)
					((PlayerBody) fixA.getUserData()).onFootTouchBound((LineSeg) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onFootTouchBound((LineSeg) fixA.getUserData());
				break;
			// mario touched a robot
			case (GameInfo.MARIO_ROBOSENSOR_BIT | GameInfo.ROBOT_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIO_ROBOSENSOR_BIT)
					((PlayerBody) fixA.getUserData()).onTouchRobot((RobotRole) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onTouchRobot((RobotRole) fixA.getUserData());
				break;
			// robot touched horizontal or vertical bound
			case (GameInfo.ROBOT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.ROBOT_BIT && fixA.getUserData() instanceof MobileRobotRole)
					((MobileRobotRole) fixA.getUserData()).onTouchBoundLine((LineSeg) fixB.getUserData());
				else if(fixB.getUserData() instanceof MobileRobotRole)
					((MobileRobotRole) fixB.getUserData()).onTouchBoundLine((LineSeg) fixA.getUserData());
				break;
			case (GameInfo.ROBOTFOOT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.ROBOT_BIT)
					((GroundCheckBot) fixA.getUserData()).onTouchGround();
				else
					((GroundCheckBot) fixB.getUserData()).onTouchGround();
				break;
			// robot touched another robot
			case (GameInfo.ROBOT_BIT):
				((RobotRole) fixA.getUserData()).onTouchRobot((RobotRole) fixB.getUserData());
				((RobotRole) fixB.getUserData()).onTouchRobot((RobotRole) fixA.getUserData());
				break;
			// item touched horizontal or vertical bound
			case (GameInfo.ITEM_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.ITEM_BIT && fixA.getUserData() instanceof MobileRobotRole)
					((MobileRobotRole) fixA.getUserData()).onTouchBoundLine((LineSeg) fixB.getUserData());
				else if(fixB.getUserData() instanceof MobileRobotRole)
					((MobileRobotRole) fixB.getUserData()).onTouchBoundLine((LineSeg) fixA.getUserData());
				break;
			// an item touched mario
			case (GameInfo.MARIO_ROBOSENSOR_BIT | GameInfo.ITEM_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIO_ROBOSENSOR_BIT)
					((PlayerBody) fixA.getUserData()).onTouchItem((RobotRole) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onTouchItem((RobotRole) fixA.getUserData());
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
			// mario's head stopped touching an interactive tile
			case (GameInfo.MARIOHEAD_BIT | GameInfo.BANGABLE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOHEAD_BIT)
					((PlayerBody) fixA.getUserData()).onHeadTileContactEnd((InteractiveTileObject) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onHeadTileContactEnd((InteractiveTileObject) fixA.getUserData());
				break;
			// mario's side stopped touching a pipe
			case (GameInfo.MARIOSIDE_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOSIDE_BIT)
					((PlayerBody) fixA.getUserData()).onEndTouchPipe((PipeEntrance) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onEndTouchPipe((PipeEntrance) fixA.getUserData());
				break;
			// mario's foot stopped touching a pipe
			case (GameInfo.MARIOFOOT_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOFOOT_BIT)
					((PlayerBody) fixA.getUserData()).onEndTouchPipe((PipeEntrance) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onEndTouchPipe((PipeEntrance) fixA.getUserData());
				break;
			case (GameInfo.MARIOFOOT_BIT | GameInfo.BOUNDARY_BIT):
				// invoke mario's foot hit method
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOFOOT_BIT)
					((PlayerBody) fixA.getUserData()).onFootLeaveBound((LineSeg) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onFootLeaveBound((LineSeg) fixA.getUserData());
				break;
			case (GameInfo.ROBOTFOOT_BIT | GameInfo.BOUNDARY_BIT):
				// invoke robot 's foot hit method
				if(fixA.getFilterData().categoryBits == GameInfo.ROBOTFOOT_BIT)
					((GroundCheckBot) fixA.getUserData()).onLeaveGround();
				else
					((GroundCheckBot) fixB.getUserData()).onLeaveGround();
				break;
			default:
				break;
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}
}
