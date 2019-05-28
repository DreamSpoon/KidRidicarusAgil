package kidridicarus.agency;

import java.util.LinkedList;

import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;
import kidridicarus.agency.agent.AgentPropertyListener;

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

	// Agent can only remove itself, if a sub-Agent needs removal then the sub-Agent must remove itself
	public void removeThisAgent() {
		myAgency.agencyIndex.queueRemoveAgent(ownerAgent);
	}

	public void createAgentRemovalRequirement(Agent otherAgent, boolean isOtherParent) {
		if(isOtherParent) {
			// ownerAgent will be removed if otherAgent is removed
			myAgency.agencyIndex.createAgentRemovalRequirement(ownerAgent, otherAgent);
		}
		else {
			// otherAgent will be removed if ownerAgent is removed
			myAgency.agencyIndex.createAgentRemovalRequirement(otherAgent, ownerAgent);
		}
	}

	public void destroyAgentRemovalRequirement(Agent otherAgent) {
		myAgency.agencyIndex.destroyAgentRemovalRequirement(ownerAgent, otherAgent);
	}

	public void createAgentRemovalOrder(Agent otherAgent, boolean isOtherParent) {
		if(isOtherParent) {
			// ownerAgent will be removed before otherAgent
			myAgency.agencyIndex.createAgentRemovalOrder(ownerAgent, otherAgent);
		}
		else {
			// otherAgent will be removed before ownerAgent
			myAgency.agencyIndex.createAgentRemovalOrder(otherAgent, ownerAgent);
		}
	}

	public void destroyAgentRemovalOrder(Agent otherAgent) {
		myAgency.agencyIndex.destroyAgentRemovalOrder(ownerAgent, otherAgent);
	}

	public AgentRemovalListener createInternalRemovalListener(AgentRemovalCallback callback) {
		return myAgency.agencyIndex.createInternalRemovalListener(ownerAgent, callback);
	}

	public void destroyInternalRemovalListener(AgentRemovalListener removalListener) {
		myAgency.agencyIndex.destroyInternalRemovalListener(removalListener);
	}

	public AgentRemovalListener createExternalRemovalListener(Agent otherAgent, AgentRemovalCallback callback) {
		return myAgency.agencyIndex.createExternalRemovalListener(ownerAgent, otherAgent, callback);
	}

	public void destroyExternalRemovalListener(AgentRemovalListener removalListener) {
		myAgency.agencyIndex.destroyExternalRemovalListener(removalListener);
	}

	public void addPropertyListener(boolean isGlobal, String propertyKey,
			AgentPropertyListener<?> propertyListener) {
		// method order of arguments differs from the changeQ method, for inline listener creation convenience
		myAgency.agencyIndex.addPropertyListener(ownerAgent, propertyListener, propertyKey, isGlobal);
	}

	public void removePropertyListener(String propertyKey) {
		myAgency.agencyIndex.removePropertyListener(ownerAgent, propertyKey);
	}

	public void addUpdateListener(float updateOrder, AgentUpdateListener updateListener) {
		// method order of arguments differs from the changeQ method, for inline listener creation convenience
		myAgency.agencyIndex.queueAddUpdateListener(ownerAgent, updateListener, updateOrder);
	}

	public void removeUpdateListener(AgentUpdateListener updateListener) {
		myAgency.agencyIndex.queueRemoveUpdateListener(ownerAgent, updateListener);
	}

	public void addDrawListener(float drawOrder, AgentDrawListener drawListener) {
		// method order of arguments differs from the changeQ method, for inline listener creation convenience
		myAgency.agencyIndex.queueAddDrawListener(ownerAgent, drawListener, drawOrder);
	}

	public void removeDrawListener(AgentDrawListener drawListener) {
		myAgency.agencyIndex.queueRemoveDrawListener(ownerAgent, drawListener);
	}

	public Agent getFirstAgentByProperty(String key, Object val) {
		return myAgency.hookGetFirstAgentByProperty(key, val);
	}

	public LinkedList<Agent> getAgentsByProperties(String[] keys, Object[] vals) {
		return myAgency.hookGetAgentsByProperties(keys, vals);
	}

	public void setUserData(Object userData) {
		ownerAgent.userData = userData;
	}
}
