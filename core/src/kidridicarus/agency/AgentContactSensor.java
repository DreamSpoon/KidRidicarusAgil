package kidridicarus.agency;

import java.util.Collection;
import java.util.HashSet;

/*
 * This class filters incoming contacts, and keeps lists of contacts based on this class's internal AgentFilter.
 * This class's internal AgentFilter is separate from the AgentFilter of the parent AgentFixture - for flexibility.
 * This sensor's contact filter bits should be a subset of (or exactly the same as) the parent AgentFixture's
 * contact filter bits. Essentially, the contact filter bits allow for organization of contact(s) between Agents,
 * and sub-parts of Agents (e.g. fixture user data). One fixture can act as a "sensor hub" to reduce the number of
 * fixtures needing creation, and allow for a hierarchy for "enabling" a single sensor or groups of sensors.
 *
 * "Enabling" a single sensor or group of sensors?
 * In a sense, it is possible to make a mistake and create a sensor with an AgentFilter that would never allow
 * for contact listing based on the parent Fixture's contact filter bits setup. That's okay, because the parent
 * AgentFixture's contact filter bits can change on a frame-by-frame basis, so that a sensor may be created and
 * destroyed only once, but can change its "enabled" status on a frame-by-frame basis.
 * Example:
 *   The above description might use this bits setup (based on the give/receive contact filter bits paradigm):
 *   Frame 1) Contact filter bits setup at begin
 *     Parent AgentFixture
 *       GiveBits = (empty)
 *       ReceiveBits = SOLID_BIT
 *     AgentSensor
 *       GiveBits = (empty)
 *       ReceiveBits = SOLID_BIT
 *   Frame 2) Contact filter bits setup at begin 
 *     Parent AgentFixture
 *       GiveBits = (empty)
 *       ReceiveBits = (empty)
 *     AgentSensor
 *       GiveBits = (empty)
 *       ReceiveBits = SOLID_BIT
 *
 *   In frame 1 the filters on the AgentFixture and AgentSensor exactly overlap, so all contacts between the fixture
 *   and other fixtures will be included in the sensor's list of contacts.
 *   In frame 2 the filters on the AgentFixture and AgentSensor do not exactly overlap; the AgentFixture will not
 *   contact anything, but the AgentSensor will still allow for contacts to be added (although no contacts will be
 *   added!).
 */
public class AgentContactSensor {
	private final AgentFixture parentFixture;
	private final AgentFilter sensorFilter;
	// "begin" is on a per-frame basis: a contact is in this map only during the first frame of contact
	private final HashSet<AgentFixture> beginContacts;
	// "current" is on a continuous basis: a contact is in this map for each frame of contact
	private final HashSet<AgentFixture> currentContacts;

	AgentContactSensor(AgentFixture parentFixture, AgentFilter sensorFilter,
			HashSet<AgentFixture> beginContacts, HashSet<AgentFixture> currentContacts) {
		this.parentFixture = parentFixture;
		this.sensorFilter = sensorFilter;
		this.beginContacts = newFilteredSet(beginContacts);
		this.currentContacts = newFilteredSet(currentContacts);
	}

	private HashSet<AgentFixture> newFilteredSet(HashSet<AgentFixture> incomingSet) {
		HashSet<AgentFixture> filteredSet = new HashSet<AgentFixture>();
		for(AgentFixture otherFixture : incomingSet) {
			if(AgentFilter.shouldCollide(this.sensorFilter, otherFixture.agentFilter))
				filteredSet.add(otherFixture);
		}
		return filteredSet;
	}

	void beginContact(AgentFixture otherFixture) {
		// if the contact is filtered "out" then exit
		if(!AgentFilter.shouldCollide(this.sensorFilter, otherFixture.agentFilter))
			return;
		// the contact is filtered "in", so add to list(s)
		this.beginContacts.add(otherFixture);
		this.currentContacts.add(otherFixture);
	}

	void endContact(AgentFixture otherFixture) {
		// if the contact is filtered "out" then exit
		if(!AgentFilter.shouldCollide(this.sensorFilter, otherFixture.agentFilter))
			return;
		// the contact is filtered "in", so remove from list(s)
		this.currentContacts.remove(otherFixture);
	}

	void endFrameCleanSensor() {
		beginContacts.clear();
	}

	public Collection<AgentFixture> getBeginContacts() {
		return beginContacts;
	}

	public Collection<AgentFixture> getCurrentContacts() {
		return currentContacts;
	}

	/*
	 * Instead of using an endContact list, use this method to check if a previously "current contact" has ended.
	 * This is done to avoid the alternative implementation of an endContact list mirroring the beginContact list.
	 * Agent removal (i.e. destruction) would make it difficult to implement endContact list mirroring the
	 * beginContact list implementation.
	 */

	public boolean isCurrentContact(AgentFixture otherFixture) {
		return currentContacts.contains(otherFixture);
	}

	public AgentFixture getFixture() {
		return parentFixture;
	}
}
