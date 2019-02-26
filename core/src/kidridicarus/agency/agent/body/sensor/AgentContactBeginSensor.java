package kidridicarus.agency.agent.body.sensor;

import java.util.LinkedList;
import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.contact.AgentBodyFilter;

public class AgentContactBeginSensor extends ContactSensor {
	private LinkedList<Agent> contacts;

	public AgentContactBeginSensor(Object parent) {
		super(parent);
		contacts = new LinkedList<Agent>();
	}

	@Override
	public void onBeginSense(AgentBodyFilter abf) {
		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent != null && !contacts.contains(agent))
			contacts.add(agent);
	}

	/*
	 * Only begin sense is needed.
	 */
	@Override
	public void onEndSense(AgentBodyFilter abf) {
	}

	public List<Agent> getAndResetContacts() {
		List<Agent> aList = new LinkedList<Agent>();
		aList.addAll(contacts);
		contacts.clear();
		return aList;
	}
}