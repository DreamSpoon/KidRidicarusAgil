package kidridicarus.agency.agentjoint;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;

import kidridicarus.agency.Agent;

public class AgentMouseJoint extends AgentJoint {
	public AgentMouseJoint(Agent parentAgent, MouseJoint b2joint, AgentMouseJointDef amjDef) {
		super(parentAgent, b2joint, amjDef);
	}

	public void setTarget(Vector2 position) {
		((MouseJoint) b2joint).setTarget(position);
	}
}
