package kidridicarus.story;

import java.util.Collection;
import java.util.LinkedList;

import kidridicarus.agency.AgentContactSensor;
import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;

public class RoleSensor {
	private final AgentContactSensor mySensor;

	public RoleSensor(AgentFixture parentFixture, AgentFilter sensorFilter) {
		this.mySensor = parentFixture.createSensor(sensorFilter);
	}

	public boolean isContact() {
		return !mySensor.getCurrentContacts().isEmpty();
	}

	public <T> boolean isBeginContactRoleClass(Class<T> roleCls) {
		return isContactRoleClass(roleCls, this.mySensor.getBeginContacts());
	}

	public <T> boolean isCurrentContactRoleClass(Class<T> roleCls) {
		return isContactRoleClass(roleCls, this.mySensor.getCurrentContacts());
	}

	private <T> boolean isContactRoleClass(Class<T> roleCls, Collection<AgentFixture> fixtureContacts) {
		for(AgentFixture otherFixture : fixtureContacts) {
			// is the contacted role's class a match for the search class?
			Role contactedRole = (Role) otherFixture.getAgent().getUserData();
			if(roleCls.isAssignableFrom(contactedRole.getClass()))
				return true;
		}
		return false;
	}

	public <T> Collection<T> getBeginContactsByRoleClass(Class<T> roleCls) {
		return getContactsByRoleClass(roleCls, this.mySensor.getBeginContacts());
	}

	public <T> Collection<T> getCurrentContactsByRoleClass(Class<T> roleCls) {
		return getContactsByRoleClass(roleCls, this.mySensor.getCurrentContacts());
	}

	// unchecked cast warning ignored because class is checked by roleCls.isAssignableFrom( ... 
	@SuppressWarnings("unchecked")
	private <T> Collection<T> getContactsByRoleClass(Class<T> roleCls, Collection<AgentFixture> fixtureContacts) {
		Collection<T> result = new LinkedList<T>();
		for(AgentFixture otherFixture : fixtureContacts) {
			// is the contacted role's class a match for the search class?
			Role contactedRole = (Role) otherFixture.getAgent().getUserData();
			if(roleCls.isAssignableFrom(contactedRole.getClass()))
				result.add((T) contactedRole);
		}
		return result;
	}

	public <T> T getFirstBeginContactByRoleClass(Class<T> roleCls) {
		return getFirstContactByRoleClass(roleCls, this.mySensor.getBeginContacts());
	}

	public <T> T getFirstCurrentContactByRoleClass(Class<T> roleCls) {
		return getFirstContactByRoleClass(roleCls, this.mySensor.getCurrentContacts());
	}

	// unchecked cast warning ignored because class is checked by roleCls.isAssignableFrom( ... 
	@SuppressWarnings("unchecked")
	private <T> T getFirstContactByRoleClass(Class<T> roleCls, Collection<AgentFixture> fixtureContacts) {
		for(AgentFixture otherFixture : fixtureContacts) {
			// is the contacted role's class a match for the search class?
			Role contactedRole = (Role) otherFixture.getAgent().getUserData();
			if(roleCls.isAssignableFrom(contactedRole.getClass()))
				return (T) contactedRole;
		}
		// all contacts were searched and given role class was not found, so return null
		return null;
	}

	public boolean isCurrentContactRole(Role searchRole) {
		for(AgentFixture otherFixture : this.mySensor.getCurrentContacts()) {
			// does the search role match the contacted fixture's role?
			if(searchRole == otherFixture.getAgent().getUserData())
				return true;
		}
		return false;
	}

	public <T> Collection<AgentFixture> getBeginContactsByFixtureDataClass(Class<T> userDataCls) {
		return getContactsByFixtureDataClass(userDataCls, this.mySensor.getBeginContacts());
	}

	public <T> Collection<AgentFixture> getCurrentContactsByFixtureDataClass(Class<T> userDataCls) {
		return getContactsByFixtureDataClass(userDataCls, this.mySensor.getCurrentContacts());
	}

	// get other fixtures that have user data class equal to userDataCls
	private <T> Collection<AgentFixture> getContactsByFixtureDataClass(Class<T> userDataCls,
			Collection<AgentFixture> fixtureContacts) {
		Collection<AgentFixture> result = new LinkedList<AgentFixture>();
		for(AgentFixture otherFixture : fixtureContacts) {
			// is the contacted roleFixture's user data class a match for the search class?
			if(userDataCls.isAssignableFrom(otherFixture.getUserData().getClass()))
				result.add(otherFixture);
		}
		return result;
	}
}
