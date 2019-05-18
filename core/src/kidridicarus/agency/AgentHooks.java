package kidridicarus.agency;

import java.util.LinkedList;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.AllowOrder;

/*
 * Even though this class is inside Agency, it is called AgentHooks and not AgencyHooks because the Object is
 * "bound" to each Agent, not just this Agency. In future code, an AgencyHooks class will also be created, for
 * external hooks usage.
 * Side-Note: See class AgentScriptHooks.
 */
public class AgentHooks {
	private final Agency myAgency;
	private Agent ownerAgent;

	AgentHooks(Agency agency, Agent ownerAgent) {
		this.myAgency = agency;
		this.ownerAgent = ownerAgent;
	}

	public void setUserData(Object userData) {
		this.ownerAgent.userData = userData;
	}

	// Agent can only remove itself, if a sub-Agent needs removal then the sub-Agent must remove itself
	public void removeThisAgent() {
		myAgency.agencyIndex.queueRemoveAgent(ownerAgent);
	}

	public void addPropertyListener(boolean isGlobal, String propertyKey,
			AgentPropertyListener<?> propertyListener) {
		// method order of arguments differs from the changeQ method, for inline listener creation convenience
		myAgency.agencyIndex.addPropertyListener(ownerAgent, propertyListener, propertyKey, isGlobal);
	}

	public void removePropertyListener(String propertyKey) {
		myAgency.agencyIndex.removePropertyListener(ownerAgent, propertyKey);
	}

	public void addUpdateListener(AllowOrder updateOrder, AgentUpdateListener updateListener) {
		// method order of arguments differs from the changeQ method, for inline listener creation convenience
		myAgency.agencyIndex.queueAddUpdateListener(ownerAgent, updateListener, updateOrder);
	}

	public void removeUpdateListener(AgentUpdateListener updateListener) {
		myAgency.agencyIndex.queueRemoveUpdateListener(ownerAgent, updateListener);
	}

	public void addDrawListener(AllowOrder drawOrder, AgentDrawListener drawListener) {
		// method order of arguments differs from the changeQ method, for inline listener creation convenience
		myAgency.agencyIndex.queueAddDrawListener(ownerAgent, drawListener, drawOrder);
	}

	public void removeDrawListener(AgentDrawListener drawListener) {
		myAgency.agencyIndex.queueRemoveDrawListener(ownerAgent, drawListener);
	}

	/*
	 * Returns a reference to the listener created so that the Agent can remove the listener later using the
	 * reference.
	 * For flexibility, each AgentRemoveListener is unique to its combination of
	 * ( listeningAgent, otherAgent, callback ), so removal of the AgentRemoveListener requires either a
	 * reference to the listener, or references to the 3 things mentioned above - it's just easier to return the
	 * AgentRemoveListener, instead of requiring removeAgentRemoveListener to lookup the AgentRemoveListener
	 * based on ( listeningAgent, otherAgent, callback ).
	 */
	public AgentRemoveListener createAgentRemoveListener(Agent otherAgent, AgentRemoveCallback callback) {
		AgentRemoveListener removeListener = new AgentRemoveListener(ownerAgent, otherAgent, callback);
		myAgency.agencyIndex.queueAddAgentRemoveListener(ownerAgent, removeListener);
		return removeListener;
	}

	public void removeAgentRemoveListener(AgentRemoveListener removeListener) {
		myAgency.agencyIndex.queueRemoveAgentRemoveListener(ownerAgent, removeListener);
	}

	public Agent getFirstAgentByProperty(String key, Object val) {
		return myAgency.hookGetFirstAgentByProperty(key, val);
	}

	public LinkedList<Agent> getAgentsByProperties(String[] keys, Object[] vals) {
		return myAgency.hookGetAgentsByProperties(keys, vals);
	}
}
