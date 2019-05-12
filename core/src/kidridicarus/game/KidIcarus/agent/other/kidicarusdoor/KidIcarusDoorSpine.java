package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import kidridicarus.agency.Agent;
import kidridicarus.common.agentspine.BasicAgentSpine;

public class KidIcarusDoorSpine extends BasicAgentSpine {
	public KidIcarusDoorSpine(Agent parentAgent) {
		super(parentAgent);
	}

	public void setOpened(boolean isOpened) {
		((KidIcarusDoorBody) body).setOpened(isOpened);
	}
}
