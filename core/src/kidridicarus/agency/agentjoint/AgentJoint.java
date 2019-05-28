package kidridicarus.agency.agentjoint;

import com.badlogic.gdx.physics.box2d.Joint;

import kidridicarus.agency.Agent;
import kidridicarus.agency.AgentBody;

public class AgentJoint {
	protected final Agent parentAgent;
	protected final Joint b2joint;
	protected final AgentBody agentBodyA;
	protected final AgentBody agentBodyB;

	public AgentJoint(Agent parentAgent, Joint b2joint, AgentJointDef ajDef) {
		this.parentAgent = parentAgent;
		this.b2joint = b2joint;
		this.agentBodyA = ajDef.agentBodyA;
		this.agentBodyB = ajDef.agentBodyB;
	}

	public AgentBody getBodyA() {
		return agentBodyA;
	}

	public AgentBody getBodyB() {
		return agentBodyB;
	}
}
