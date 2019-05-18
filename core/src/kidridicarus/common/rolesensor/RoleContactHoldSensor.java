package kidridicarus.common.rolesensor;

import java.util.LinkedList;
import java.util.List;

import kidridicarus.agency.Agent;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.AgentContactSensor;
import kidridicarus.story.Role;

/*
 * Keep track of agents contacted.
 */
public class RoleContactHoldSensor extends AgentContactSensor {
	private LinkedList<Role> contacts;

	public RoleContactHoldSensor(Role parentRole) {
		super(parentRole == null ? null : parentRole.getAgent());
		contacts = new LinkedList<Role>();
	}

	@Override
	public void onBeginSense(AgentBodyFilter abf) {
		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent != null && agent.getUserData() instanceof Role && !contacts.contains(agent.getUserData()))
			contacts.add((Role) agent.getUserData());
	}

	@Override
	public void onEndSense(AgentBodyFilter abf) {
		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent != null && agent.getUserData() instanceof Role && contacts.contains(agent.getUserData()))
			contacts.remove(agent.getUserData());
	}

	public Role getFirstContact() {
		return contacts.getFirst();
	}

	public List<Role> getContacts() {
		return contacts;
	}

	// ignore unchecked cast because isAssignableFrom method is used to check class
	@SuppressWarnings("unchecked")
	public <T> T getFirstContactByUserDataClass(Class<T> cls) {
		for(Role role : contacts) {
			if(cls.isAssignableFrom(role.getClass()))
				return (T) role;
		}
		return null;
	}

	// ignore unchecked cast because isAssignableFrom method is used to check class
	@SuppressWarnings("unchecked")
	public <T> List<T> getContactsByUserDataClass(Class<T> cls) {
		List<T> cList = new LinkedList<T>();
		for(Role role : contacts) {
			if(cls.isAssignableFrom(role.getClass()))
				cList.add((T) role);
		}
		return cList;
	}
}
