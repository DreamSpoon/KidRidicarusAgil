package kidridicarus.game.agent.SMB.player.mariofireball;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;

public class MarioFireballSpine {
	private MarioFireballBody body;
	private SolidBoundSensor hmSensor;
	private AgentContactHoldSensor acSensor;

	public MarioFireballSpine(MarioFireballBody body) {
		this.body = body;
	}

	public SolidBoundSensor createHMSensor() {
		hmSensor = new SolidBoundSensor(body);
		return hmSensor;
	}

	public AgentContactHoldSensor createAgentContactSensor() {
		acSensor = new AgentContactHoldSensor(this);
		return acSensor;
	}

	public List<Agent> getContactAgents() {
		return acSensor.getContacts();
	}

	public List<ContactDmgTakeAgent> getContactDmgTakeAgents() {
		return acSensor.getContactsByClass(ContactDmgTakeAgent.class);
	}

	public boolean isHitBoundary(boolean facingRight) {
		if(hmSensor.isHMoveBlocked(body.getBounds(), facingRight) || (body.getVelocity().x <= 0f && facingRight) ||
				(body.getVelocity().x >= 0f && !facingRight))
			return true;
		return false;
	}

	public void startExplode() {
		body.disableAllContacts();
		body.setVelocity(0f, 0f);
		body.setGravityScale(0f);
	}
}