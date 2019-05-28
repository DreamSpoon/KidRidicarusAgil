package kidridicarus.common.rolesensor;

import java.util.LinkedList;
import java.util.List;

import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.AgentContactSensor;
import kidridicarus.story.Role;

/*
 * Keep a list of either begin contacts, or end contacts, but not both.
 */
public class OneWayContactSensor extends AgentContactSensor {
	private LinkedList<Role> contacts;
	private boolean isBeginSensor;

	public OneWayContactSensor(Role parentRole, boolean isBeginSensor) {
		super(parentRole == null ? null : parentRole.getAgent());
		this.isBeginSensor = isBeginSensor;
		contacts = new LinkedList<Role>();
	}

	@Override
	public void onBeginSense(AgentBodyFilter abf) {
		if(!isBeginSensor)
			return;
		Role otherRole = Role.getRoleFromABF(abf);
		if(otherRole != null && !contacts.contains(otherRole))
			contacts.add(otherRole);
	}

	@Override
	public void onEndSense(AgentBodyFilter abf) {
		if(isBeginSensor)
			return;
		Role otherRole = Role.getRoleFromABF(abf);
		if(otherRole != null && !contacts.contains(otherRole))
			contacts.add(otherRole);
	}

	public List<Role> getAndResetContacts() {
		List<Role> aList = new LinkedList<Role>();
		aList.addAll(contacts);
		contacts.clear();
		return aList;
	}

	// ignore unchecked cast because isAssignableFrom method is used to check class
	@SuppressWarnings("unchecked")
	public <T> List<T> getOnlyAndResetContacts(Class<T> cls) {
		List<T> cList = new LinkedList<T>();
		for(Role role : contacts) {
			if(cls.isAssignableFrom(role.getClass()))
				cList.add((T) role);
		}
		contacts.clear();
		return cList;
	}
}
