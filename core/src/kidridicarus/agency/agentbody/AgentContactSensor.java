package kidridicarus.agency.agentbody;

import kidridicarus.agency.Agent;

/*
 * Chainable contact sensor.
 */
public abstract class AgentContactSensor {
	public abstract void onBeginSense(AgentBodyFilter abf);
	public abstract void onEndSense(AgentBodyFilter abf);

	private final Agent parent;
	private AgentContactSensor nextInChain;

	public AgentContactSensor(Agent parent) {
		this.parent = parent;
		nextInChain = null;
	}

	/*
	 * In case a contact sensor contacts another contact sensor, it can use getParent on the other contact sensor
	 * to figure out what was actually contacted.
	 */
	public Agent getParent() {
		return parent;
	}

	/*
	 * Add a sensor to the start of this sensor's list of chained sensors.
	 * The nextInChain sensor will be activated after this sensor.
	 */
	public void chainTo(AgentContactSensor nextInChain) {
		this.nextInChain = nextInChain;
	}

	public void onBeginContact(AgentBodyFilter abf) {
		onBeginSense(abf);
		if(nextInChain != null)
			nextInChain.onBeginContact(abf);
	}

	public void onEndContact(AgentBodyFilter abf) {
		onEndSense(abf);
		if(nextInChain != null)
			nextInChain.onEndContact(abf);
	}
}
