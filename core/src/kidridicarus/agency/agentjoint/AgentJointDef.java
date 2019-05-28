package kidridicarus.agency.agentjoint;

import kidridicarus.agency.AgentBody;

public class AgentJointDef {
	public enum AgentJointType { UNKNOWN, MOUSE_JOINT }

	public AgentJointType type = AgentJointType.UNKNOWN;
	public AgentBody agentBodyA = null;
	public AgentBody agentBodyB = null;
}
