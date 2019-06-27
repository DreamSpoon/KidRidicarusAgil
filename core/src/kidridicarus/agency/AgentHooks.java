package kidridicarus.agency;

import java.util.Collection;

import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;

/*
 * Even though this class is inside Agency, it is called AgentHooks and not AgencyHooks because the Object is
 * "bound" to each Agent, not just this Agency. In future code, an AgencyHooks class will also be created, for
 * external hooks usage.
 * Side-Note: See class AgentScriptHooks.
 */
public class AgentHooks {
	private final Agency myAgency;
	private AgencyIndex myAgencyIndex;
	private Agent ownerAgent;

	AgentHooks(Agency agency, AgencyIndex agencyIndex, Agent ownerAgent) {
		this.myAgency = agency;
		this.myAgencyIndex = agencyIndex;
		this.ownerAgent = ownerAgent;
	}

	// Agent can only remove itself, if a sub-Agent needs removal then the sub-Agent must remove itself
	public void removeThisAgent() {
		myAgencyIndex.queueRemoveAgent(ownerAgent);
	}

	public void createAgentRemovalRequirement(Agent otherAgent, boolean isOtherParent) {
		if(isOtherParent) {
			// ownerAgent will be removed if otherAgent is removed
			myAgencyIndex.createAgentRemovalRequirement(ownerAgent, otherAgent);
		}
		else {
			// otherAgent will be removed if ownerAgent is removed
			myAgencyIndex.createAgentRemovalRequirement(otherAgent, ownerAgent);
		}
	}

	public void destroyAgentRemovalRequirement(Agent otherAgent) {
		myAgencyIndex.destroyAgentRemovalRequirement(ownerAgent, otherAgent);
	}

	public void createAgentRemovalOrder(Agent otherAgent, boolean isOtherParent) {
		if(isOtherParent) {
			// ownerAgent will be removed before otherAgent
			myAgencyIndex.createAgentRemovalOrder(ownerAgent, otherAgent);
		}
		else {
			// otherAgent will be removed before ownerAgent
			myAgencyIndex.createAgentRemovalOrder(otherAgent, ownerAgent);
		}
	}

	public void destroyAgentRemovalOrder(Agent otherAgent) {
		myAgencyIndex.destroyAgentRemovalOrder(ownerAgent, otherAgent);
	}

	public AgentRemovalListener createInternalRemovalListener(AgentRemovalCallback callback) {
		return myAgencyIndex.createInternalRemovalListener(ownerAgent, callback);
	}

	public void destroyInternalRemovalListener(AgentRemovalListener removalListener) {
		myAgencyIndex.destroyInternalRemovalListener(removalListener);
	}

	public AgentRemovalListener createExternalRemovalListener(Agent otherAgent, AgentRemovalCallback callback) {
		return myAgencyIndex.createExternalRemovalListener(ownerAgent, otherAgent, callback);
	}

	public void destroyExternalRemovalListener(AgentRemovalListener removalListener) {
		myAgencyIndex.destroyExternalRemovalListener(removalListener);
	}

	public void addPropertyListener(boolean isGlobal, String propertyKey,
			AgentPropertyListener<?> propertyListener) {
		// method order of arguments differs from the changeQ method, for inline listener creation convenience
		myAgencyIndex.addPropertyListener(ownerAgent, propertyListener, propertyKey, isGlobal);
	}

	public void removePropertyListener(String propertyKey) {
		myAgencyIndex.removePropertyListener(ownerAgent, propertyKey);
	}

	public void addUpdateListener(float updateOrder, AgentUpdateListener updateListener) {
		// method order of arguments differs from the changeQ method, for inline listener creation convenience
		myAgencyIndex.queueAddUpdateListener(ownerAgent, updateListener, updateOrder);
	}

	public void removeUpdateListener(AgentUpdateListener updateListener) {
		myAgencyIndex.queueRemoveUpdateListener(ownerAgent, updateListener);
	}

	public void addDrawListener(float drawOrder, AgentDrawListener drawListener) {
		// method order of arguments differs from the changeQ method, for inline listener creation convenience
		myAgencyIndex.addDrawListener(ownerAgent, drawListener, drawOrder);
	}

	public void removeDrawListener(AgentDrawListener drawListener) {
		myAgencyIndex.removeDrawListener(ownerAgent, drawListener);
	}

	public Collection<Agent> getAgentsByProperties(String[] keys, Object[] vals) {
		return myAgency.hookGetAgentsByProperties(keys, vals);
	}

	public Collection<Agent> getAgentsByProperty(String key, Object val) {
		return myAgency.hookGetAgentsByProperties(new String[] { key }, new Object[] { val });
	}

	public Agent getFirstAgentByProperties(String[] keys, Object[] vals) {
		return myAgency.hookGetFirstAgentByProperties(keys, vals);
	}

	public Agent getFirstAgentByProperty(String key, Object val) {
		return myAgency.hookGetFirstAgentByProperties(new String[] { key }, new Object[] { val });
	}

	public float getAbsTime() {
		return myAgency.getAbsTime();
	}

	public void setUserData(Object userData) {
		ownerAgent.userData = userData;
	}
}
