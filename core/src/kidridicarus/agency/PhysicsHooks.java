package kidridicarus.agency;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

import kidridicarus.agency.agentjoint.AgentJoint;
import kidridicarus.agency.agentjoint.AgentJointDef;
import kidridicarus.agency.agentjoint.AgentMouseJoint;
import kidridicarus.agency.agentjoint.AgentMouseJointDef;

public class PhysicsHooks {
	private final Agency myAgency;
	private final Agent myAgent;

	PhysicsHooks(Agency agency, Agent agent) {
		this.myAgency = agency;
		this.myAgent = agent;
	}

	public AgentBody createBody(BodyDef bdef) {
		return myAgency.createAgentBody(myAgent, bdef);
	}

	public void destroyBody(AgentBody agentBody) {
		myAgency.destroyAgentBody(agentBody);
	}

	public AgentJoint createJoint(AgentJointDef ajDef) {
		switch(ajDef.type) {
			case MOUSE_JOINT:
				MouseJointDef mjdef = new MouseJointDef();
				mjdef.bodyA = ajDef.agentBodyA.b2body;
				mjdef.bodyB = ajDef.agentBodyB.b2body;
				mjdef.target.set(((AgentMouseJointDef) ajDef).target);
				mjdef.maxForce = ((AgentMouseJointDef) ajDef).maxForce;
				mjdef.frequencyHz = ((AgentMouseJointDef) ajDef).frequencyHz;
				mjdef.dampingRatio = ((AgentMouseJointDef) ajDef).dampingRatio;
				return new AgentMouseJoint(myAgent, (MouseJoint) myAgency.panWorld.createJoint(mjdef),
						(AgentMouseJointDef) ajDef);
			default:
				throw new IllegalArgumentException("Cannot create Agent Joint with unkown joint type.");
		}
	}
}
