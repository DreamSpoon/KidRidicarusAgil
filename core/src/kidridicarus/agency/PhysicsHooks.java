package kidridicarus.agency;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

import kidridicarus.agency.agentjoint.AgentJoint;
import kidridicarus.agency.agentjoint.AgentJointDef;
import kidridicarus.agency.agentjoint.AgentMouseJoint;
import kidridicarus.agency.agentjoint.AgentMouseJointDef;

public class PhysicsHooks {
	private final Agency agency;
	private final World world;
	final Agent myAgent;

	PhysicsHooks(Agency agency, World world, Agent agent) {
		this.agency = agency;
		this.world = world;
		this.myAgent = agent;
	}

	public AgentBody createAgentBody(BodyDef bdef) {
		AgentBody newBody = new AgentBody(this, world.createBody(bdef));
		myAgent.agentBodies.add(newBody);
		return newBody;
	}

	public void queueDestroyAgentBody(final AgentBody agentBody) {
		if(!myAgent.agentBodies.contains(agentBody)) {
			throw new IllegalArgumentException("Cannot destroy AgentBody; AgentBody is not attached to Agent. "+
					"myAgent.userData="+myAgent.userData);
		}
		agency.queueDestroyAgentBody(myAgent, agentBody);
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
				return new AgentMouseJoint(myAgent, (MouseJoint) world.createJoint(mjdef),
						(AgentMouseJointDef) ajDef);
			default:
				throw new IllegalArgumentException("Cannot create Agent Joint with unkown joint type.");
		}
	}

	public void queueDestroyFixture(AgentFixture agentFixture) {
		agency.queueDestroyAgentFixture(agentFixture);
	}
}
