package kidridicarus.agency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.draodgraph.DRAOD_Graph;
import kidridicarus.agency.tool.draodgraph.DepNode;
import kidridicarus.agency.tool.draodgraph.DepResolver;

/*
 * A list of all Agents in the Agency, and their associated update listeners and draw listeners.
 * Agent removal dependency ordering is supported.
 * In general, "create" changes are immediate and "destroy" changes are queued.
 */
class AgencyIndex {
	private interface AgencyChange { void applyChange(); }

	private HashSet<Agent> allAgents;
	private LinkedBlockingQueue<AgencyChange> removeAgentChangeQ;
	private LinkedBlockingQueue<AgencyChange> removalNodeDestroyQ;
	// Agent removal dependency graph
	private DRAOD_Graph agentRemovalGraph;
	// a "lock" object to prevent concurrent modification errors in Agent removal graph
	private boolean isResolvingRemovalGraph;

	private HashMap<AgentUpdateListener, Float> allUpdateListeners;
	private TreeMap<Float, HashSet<AgentUpdateListener>> orderedUpdateListeners;
	private LinkedBlockingQueue<AgencyChange> updateListenerChangeQ;
	private HashMap<AgentDrawListener, Float> allDrawListeners;
	private TreeMap<Float, HashSet<AgentDrawListener>> orderedDrawListeners;
	private LinkedBlockingQueue<AgencyChange> drawListenerChangeQ;
	// sub-lists of Agents that have properties, indexed by property String
	private HashMap<String, LinkedList<Agent>> globalPropertyKeyAgents;

	AgencyIndex() {
		allAgents = new HashSet<Agent>();
		removeAgentChangeQ = new LinkedBlockingQueue<AgencyChange>();
		removalNodeDestroyQ = new LinkedBlockingQueue<AgencyChange>();
		agentRemovalGraph = new DRAOD_Graph();
		isResolvingRemovalGraph = false;

		allUpdateListeners = new HashMap<AgentUpdateListener, Float>();
		orderedUpdateListeners = new TreeMap<Float, HashSet<AgentUpdateListener>>();
		updateListenerChangeQ = new LinkedBlockingQueue<AgencyChange>();
		allDrawListeners = new HashMap<AgentDrawListener, Float>();
		orderedDrawListeners = new TreeMap<Float, HashSet<AgentDrawListener>>();
		drawListenerChangeQ = new LinkedBlockingQueue<AgencyChange>();
		globalPropertyKeyAgents = new HashMap<String, LinkedList<Agent>>();
	}

	/*
	 * When the Agent is added, a node is created in the Agent removal graph. Resolution of the node causes
	 * Agent to be removed. Since the removal graph allows for removal requirement dependencies, removal of the
	 * Agent may cause removal of other Agents.
	 * Separate from the removal *requirement* dependencies, removal *order* dependencies can be created to
	 * pre-plan how Agent removal order is handled when a cascade of Agent removals is caused by removal of a
	 * single Agent.
	 */
	void addAgent(final Agent agent) {
		// throw error if Agents are currently being removed via resolution of Agent removal graph
		if(isResolvingRemovalGraph)
			throw new IllegalStateException("Cannot add Agent during resolution of Agent removal dependency graph.");
		// before adding agent, create a diabolical plan to remove agent (mwa-ha-ha!)
		agent.removalNode = agentRemovalGraph.createNode(new DepResolver() {
				@Override
				public void resolve() { doAgentRemoval(agent); }
			});
		// add agent
		allAgents.add(agent);
	}

	/*
	 * Queue removal of agent from the index. The agent must be in the All Agents list when it is queued for removal,
	 * but the agent does not need to be in the All Agents list when the queued removal change is processed.
	 * i.e. Agents can independently add themselves to the Agent remove queue, even if they are in a removal
	 *      dependency chain (one is child and other is parent, etc.).
	 * So, agent is checked for All Agents List inclusion when entering the queue, and not checked when leaving the
	 * queue. Extra "remove Agent" change requests are ignored because possible errors are trapped here.
	 * TODO log extra removal requests?
	 */
	void queueRemoveAgent(final Agent agent) {
		if(!allAgents.contains(agent)) {
			throw new IllegalArgumentException(
					"Cannot remove Agent that is not in All Agents List, agent.userData="+agent.getUserData());
		}
		else if(agent.removalNode == null) {
			throw new IllegalArgumentException(
					"Cannot remove Agent that is already removed, agent.userData="+agent.getUserData());
		}
		removeAgentChangeQ.add(new AgencyChange() {
				@Override
				public void applyChange() {
					// if Agent is marked as already removed (due to removal dependency) then exit
					if(agent.removalNode == null)
						return;
					// resolve Agent removal dependencies
					isResolvingRemovalGraph = true;
					agentRemovalGraph.resolveOrder(agent.removalNode);
					isResolvingRemovalGraph = false;
				}
			});
	}

	void removeAllAgents() {
		// safely resolve the Agent removal graph
		isResolvingRemovalGraph = true;
		agentRemovalGraph.resolveOrder();
		isResolvingRemovalGraph = false;
		// destroy the graph as a batch instead of removing dependencies one-by-one
		agentRemovalGraph.destroyGraph();
		// the dependencies were processed as a batch instead of individually, so clear the change queue
		removalNodeDestroyQ.clear();
	}

	private void doAgentRemoval(final Agent agent) {
		invokePreRemovalListeners(agent);

		// Queue destruction of removal dependency node instead of destroying immediately - since this
		// operation is performed during resolution of removal graph, making destruction of node
		// impossible at this time.
		// Be careful with scope of final variables; "agent" is final but "agent.removalNode" is not
		// final within this scope. "nodeToDestroy" is final within this scope, thus it saves our reference
		// to the removal node needing destruction even though agent.removalNode will be set to null.
		final DepNode nodeToDestroy = agent.removalNode;
		// mark agent as removed, to prevent removing agent multiple times
		agent.removalNode = null;
		removalNodeDestroyQ.add(new AgencyChange() {
				@Override
				public void applyChange() { agentRemovalGraph.destroyNode(nodeToDestroy); }
			});
		removeAllUpdateListeners(agent);
		removeAllDrawListeners(agent);
		removeAllAgentPropertyListeners(agent);
		allAgents.remove(agent);

		invokePostRemovalListeners(agent);
		destroyAllRemovalListeners(agent);
	}

	/*
	 * Regarding pre- and post-removal listeners, and the internal/external callback order paradigm:
	 *   External Agents receive the pre-removal callback first, and then agent itself receives pre-removal callback.
	 *   The agent itself receives the post-removal callback first, and then external Agents receive pre-removal
	 *   callback.
	 *   This allows for two Agents to independently schedule things to avoid "who goes first" problems.
	 *   External Agents may need to do things before the internal state of the "to be removed" agent is changed
	 *   (internal state may be changed when agent's own removal listener is invoked), and external Agents may
	 *   need to do things after the "to be removed" agent is removed - since removal of agent may cause changes to
	 *   the state of Box2D World.
	 *   Internally, the agent *should* need only the functionality of an "agentRemoved() {...}" method, and not need
	 *   the functionality of pre- and post-removal methods. From the internal standpoint of the agent, these two
	 *   methods are invoked concurrently - de-listing the agent does not cause internal changes to the
	 *   agent. However, the agent's listeners are removed and the agent is de-listed from the Agency, so it is
	 *   reasonable to include an internal post-removal method. Perhaps the agent uses the pre-removal callback to
	 *   remove it's Box2D bodies, and uses the post-removal callback to cause external state changes based on the
	 *   Box2D contact changes (or lack thereof).
	 */

	private void invokePreRemovalListeners(Agent agent) {
		for(AgentRemovalListener listener : agent.otherExternalRemovalListeners)
			listener.callback.preAgentRemoval();
		for(AgentRemovalListener listener : agent.internalRemovalListeners)
			listener.callback.preAgentRemoval();
	}

	// removalListeners list is not cleared because agent will be de-referenced, which will cause garbage
	// collection of the listeners.
	private void invokePostRemovalListeners(Agent agent) {
		for(AgentRemovalListener listener : agent.internalRemovalListeners)
			listener.callback.postAgentRemoval();
		for(AgentRemovalListener listener : agent.otherExternalRemovalListeners)
			listener.callback.postAgentRemoval();
	}

	private void destroyAllRemovalListeners(Agent agent) {
		// destroy the listeners that this agent added to another Agent
		for(AgentRemovalListener myListener : agent.myExternalRemovalListeners)
			myListener.otherAgent.otherExternalRemovalListeners.remove(myListener);
		// destroy the listeners that another Agent added to this agent
		for(AgentRemovalListener otherListener : agent.otherExternalRemovalListeners)
			otherListener.myAgent.myExternalRemovalListeners.remove(otherListener);
		// destroy the listeners that this agent added to itself
		agent.internalRemovalListeners.clear();
	}

	void processRemoveAgentQueue() {
		while(!removeAgentChangeQ.isEmpty())
			removeAgentChangeQ.poll().applyChange();
		// process "destroy node" changes that were added while Agents were being removed
		processRemovalNodeDestroyQueue();
	}

	void processRemovalNodeDestroyQueue() {
		while(!removalNodeDestroyQ.isEmpty())
			removalNodeDestroyQ.poll().applyChange();
	}

	// childAgent will be removed if parentAgent is removed
	void createAgentRemovalRequirement(final Agent childAgent, final Agent parentAgent) {
		if(isResolvingRemovalGraph) {
			throw new IllegalStateException(
					"Cannot create Agent removal dependency during resolution of Agent removal dependency graph.");
		}
		agentRemovalGraph.createRequireEdge(childAgent.removalNode, parentAgent.removalNode);
	}

	void destroyAgentRemovalRequirement(final Agent firstAgent, final Agent secondAgent) {
		if(isResolvingRemovalGraph) {
			throw new IllegalStateException(
					"Cannot create Agent removal dependency during resolution of Agent removal dependency graph.");
		}
		agentRemovalGraph.destroyRequireEdge(firstAgent.removalNode, secondAgent.removalNode);
	}

	void createAgentRemovalOrder(final Agent childAgent, final Agent parentAgent) {
		if(isResolvingRemovalGraph) {
			throw new IllegalStateException(
					"Cannot create Agent removal dependency during resolution of Agent removal dependency graph.");
		}
		agentRemovalGraph.createOrderEdge(childAgent.removalNode, parentAgent.removalNode);
	}

	void destroyAgentRemovalOrder(final Agent firstAgent, final Agent secondAgent) {
		if(isResolvingRemovalGraph) {
			throw new IllegalStateException(
					"Cannot create Agent removal dependency during resolution of Agent removal dependency graph.");
		}
		agentRemovalGraph.destroyOrderEdge(firstAgent.removalNode, secondAgent.removalNode);
	}

	AgentRemovalListener createInternalRemovalListener(Agent ownerAgent, AgentRemovalCallback callback) {
		AgentRemovalListener listener = new AgentRemovalListener(ownerAgent, null, callback);
		ownerAgent.internalRemovalListeners.add(listener);
		return listener;
	}

	void destroyInternalRemovalListener(AgentRemovalListener removalListener) {
		removalListener.myAgent.internalRemovalListeners.remove(removalListener);
	}

	AgentRemovalListener createExternalRemovalListener(Agent ownerAgent, Agent otherAgent,
			AgentRemovalCallback callback) {
		AgentRemovalListener listener = new AgentRemovalListener(ownerAgent, otherAgent, callback);
		ownerAgent.myExternalRemovalListeners.add(listener);
		otherAgent.otherExternalRemovalListeners.add(listener);
		return listener;
	}

	void destroyExternalRemovalListener(AgentRemovalListener removalListener) {
		removalListener.myAgent.myExternalRemovalListeners.remove(removalListener);
		removalListener.otherAgent.otherExternalRemovalListeners.remove(removalListener);
	}

	// call update listeners with update order < 0
	void doPreStepAgentUpdates(FrameTime frameTime) {
		doAgentUpdateIter(orderedUpdateListeners.headMap(0.0f).entrySet().iterator(), frameTime);
	}

	// call update listeners with update order >= 0
	void doPostStepAgentUpdates(FrameTime frameTime) {
		doAgentUpdateIter(orderedUpdateListeners.tailMap(0.0f).entrySet().iterator(), frameTime);
	}

	private void doAgentUpdateIter(Iterator<Entry<Float, HashSet<AgentUpdateListener>>> iter, FrameTime frameTime) {
		while(iter.hasNext()) {
			// iterate through subset of listeners at this specific update order
			Iterator<AgentUpdateListener> subIter = iter.next().getValue().iterator();
			while(subIter.hasNext())
				subIter.next().update(frameTime);
		}
	}

	void processUpdateListenerQueue() {
		while(!updateListenerChangeQ.isEmpty())
			updateListenerChangeQ.poll().applyChange();
	}

	// add a single update listener and associate it with the given agent
	void queueAddUpdateListener(final Agent agent, final AgentUpdateListener updateListener,
			final Float updateOrder) {
		updateListenerChangeQ.add(new AgencyChange() {
				@Override
				public void applyChange() {
					if(allUpdateListeners.containsKey(updateListener)) {
						throw new IllegalArgumentException(
								"Cannot add update listener; listener has already been added: " + updateListener);
					}
					// add listener to list of all update listeners
					allUpdateListeners.put(updateListener, updateOrder);
					HashSet<AgentUpdateListener> listenerSet = orderedUpdateListeners.get(updateOrder);
					if(listenerSet == null) {
						listenerSet = new HashSet<AgentUpdateListener>();
						orderedUpdateListeners.put(updateOrder, listenerSet);
					}
					listenerSet.add(updateListener);
					// add listener to Agent
					agent.updateListeners.add(updateListener);
				}
			});
	}

	// remove a single update listener associated with the given agent
	void queueRemoveUpdateListener(final Agent agent, final AgentUpdateListener updateListener) {
		updateListenerChangeQ.add(new AgencyChange() {
				@Override
				public void applyChange() {
					if(!allUpdateListeners.containsKey(updateListener)) {
						throw new IllegalArgumentException(
								"Cannot remove update listener; listener was not added: " + updateListener);
					}
					// Get the current update order for the listener...
					float updateOrder = allUpdateListeners.get(updateListener);
					// ... to find and remove the listener from the ordered tree/hashsets.
					HashSet<AgentUpdateListener> listenerSet = orderedUpdateListeners.get(updateOrder);
					listenerSet.remove(updateListener);
					// remove empty sets to avoid wasting memory
					if(listenerSet.isEmpty())
						orderedUpdateListeners.remove(updateOrder);
					// remove the listener from the set of all listeners
					allUpdateListeners.remove(updateListener);
					// and remove the listener from the Agent's list of listeners
					agent.updateListeners.remove(updateListener);
				}
			});
	}

	// remove all update listeners associated with the given agent
	private void removeAllUpdateListeners(Agent agent) {
		for(AgentUpdateListener updateListener : agent.updateListeners) {
			// remove the listener from the ordered treeset/hashsets
			float updateOrder = allUpdateListeners.get(updateListener);
			HashSet<AgentUpdateListener> listenerSet = orderedUpdateListeners.get(updateOrder);
			listenerSet.remove(updateListener);
			// remove empty sets to avoid wasting memory
			if(listenerSet.isEmpty())
				orderedUpdateListeners.remove(updateOrder);
			allUpdateListeners.remove(updateListener);
		}
		agent.updateListeners.clear();
	}

	void doAgentDraws(Eye eye) {
		// iterate through sorted subsets, from lowest draw order to highest draw order
		Iterator<Entry<Float, HashSet<AgentDrawListener>>> iter = orderedDrawListeners.entrySet().iterator();
		while(iter.hasNext()) {
			// iterate through subset of listeners at this specific draw order
			Iterator<AgentDrawListener> subIter = iter.next().getValue().iterator();
			while(subIter.hasNext())
				subIter.next().draw(eye);
		}
	}

	void processDrawListenerQueue() {
		while(!drawListenerChangeQ.isEmpty())
			drawListenerChangeQ.poll().applyChange();
	}

	// add a single draw listener and associate it with the given agent
	void queueAddDrawListener(final Agent agent, final AgentDrawListener drawListener,
			final float drawOrder) {
		drawListenerChangeQ.add(new AgencyChange() {
				@Override
				public void applyChange() {
					if(allDrawListeners.containsKey(drawListener)) {
						throw new IllegalArgumentException(
								"Cannot add draw listener; listener has already been added: " + drawListener);
					}
					// add listener to list of all draw listeners
					allDrawListeners.put(drawListener, drawOrder);
					HashSet<AgentDrawListener> listenerSet = orderedDrawListeners.get(drawOrder);
					if(listenerSet == null) {
						listenerSet = new HashSet<AgentDrawListener>();
						orderedDrawListeners.put(drawOrder, listenerSet);
					}
					listenerSet.add(drawListener);
					// add listener to Agent
					agent.drawListeners.add(drawListener);
				}
			});
	}

	// remove a single draw listener associated with the given agent
	void queueRemoveDrawListener(final Agent agent, final AgentDrawListener drawListener) {
		drawListenerChangeQ.add(new AgencyChange() {
				@Override
				public void applyChange() {
					if(!allDrawListeners.containsKey(drawListener)) {
						throw new IllegalArgumentException("Cannot remove draw listener because listener was not "+
								"added, drawListener=" + drawListener + ", agent=" + agent);
					}
					// Get the current draw order for the listener...
					float drawOrder = allDrawListeners.get(drawListener);
					// ... to find and remove the listener from the ordered tree/hashsets.
					HashSet<AgentDrawListener> listenerSet = orderedDrawListeners.get(drawOrder);
					listenerSet.remove(drawListener);
					// remove empty sets to avoid wasting memory
					if(listenerSet.isEmpty())
						orderedDrawListeners.remove(drawOrder);
					// remove the listener from the list of all listeners
					allDrawListeners.remove(drawListener);
					// and remove the listener from the Agent's list of listeners
					agent.drawListeners.remove(drawListener);
				}
			});
	}

	// remove all draw listeners associated with the given agent
	private void removeAllDrawListeners(Agent agent) {
		for(AgentDrawListener drawListener : agent.drawListeners) {
			// remove the listener from the ordered treeset/hashsets
			float drawOrder = allDrawListeners.get(drawListener);
			HashSet<AgentDrawListener> listenerSet = orderedDrawListeners.get(drawOrder);
			listenerSet.remove(drawListener);
			// remove empty sets to avoid wasting memory
			if(listenerSet.isEmpty())
				orderedDrawListeners.remove(drawOrder);
			// remove the listener from the hash map of listeners and draw orders
			allDrawListeners.remove(drawListener);
		}
		agent.drawListeners.clear();
	}

	void addPropertyListener(final Agent agent, final AgentPropertyListener<?> listener,
			final String propertyKey, final Boolean isGlobal) {
		// if the property is global then add the property key to a global list
		if(isGlobal) {
			LinkedList<Agent> subList;
			// If the property String isn't in the global list, then create an empty sub-list and add
			// the new sub-list to the global list.
			if(!globalPropertyKeyAgents.containsKey(propertyKey)) {
				subList = new LinkedList<Agent>();
				globalPropertyKeyAgents.put(propertyKey, subList);
			}
			// otherwise get existing sub-list
			else
				subList = globalPropertyKeyAgents.get(propertyKey);
			// add agent to sub-list for this property String, to associate the property listener to agent
			subList.add(agent);
			// keep a list of global property keys within Agent, for removal purposes
			agent.globalPropertyKeys.add(propertyKey);
		}
		// add the property listener to the Agent locally
		agent.propertyListeners.put(propertyKey, listener);
	}

	void removePropertyListener(final Agent agent, final String propertyKey) {
		// if the property is a global property then remove it from the global property list
		if(agent.globalPropertyKeys.contains(propertyKey)) {
			// if the property String isn't in the global list, then throw exception
			if(!globalPropertyKeyAgents.containsKey(propertyKey)) {
				throw new IllegalArgumentException("Cannot remove listener for property="+propertyKey+
						", from agent="+agent);
			}
			// remove agent from the property sub-list
			LinkedList<Agent> subList = globalPropertyKeyAgents.get(propertyKey);
			subList.remove(agent);
			// remove the sub-list if it is empty, to prevent accumulating empty lists
			if(subList.isEmpty())
				globalPropertyKeyAgents.remove(propertyKey);
			// remove the key from the Agent's list of global property keys
			agent.globalPropertyKeys.remove(propertyKey);
		}
		// remove property listener from agent locally
		agent.propertyListeners.remove(propertyKey);
	}

	// remove all property listeners associated with agent
	private void removeAllAgentPropertyListeners(Agent agent) {
		// first, remove agent from all the sub-lists that link to it via its global property Strings
		for(String propertyKey : agent.globalPropertyKeys) {
			LinkedList<Agent> subList = globalPropertyKeyAgents.get(propertyKey);
			subList.remove(agent);
			// remove the sub-list if it is empty, to prevent accumulating empty lists
			if(subList.isEmpty())
				globalPropertyKeyAgents.remove(propertyKey);
		}
		// last, remove all property keys and listeners from agent
		agent.globalPropertyKeys.clear();
		agent.propertyListeners.clear();
	}

	/*
	 * Search all Agents with properties for matches against the given properties, return list of matching Agents.
	 * Does not return null, will return empty list if needed.
	 * Note: The Agents in the returned list may have extra properties as well, not just the given properties -
	 * this search is an inclusive search.
	 */
	LinkedList<Agent> getAgentsByProperties(String[] keys, Object[] vals, boolean firstOnly) {
		if(keys.length != vals.length)
			throw new IllegalArgumentException("keys[] and vals[] arrays are not of equal length.");
		// if search keys array is empty then return empty list
		LinkedList<Agent> ret = new LinkedList<Agent>();
		if(keys.length == 0)
			return ret;
		// find the first search property key that exists in the global list
		String matchingKey = null;
		for(String key : keys) {
			if(globalPropertyKeyAgents.containsKey(key)) {
				matchingKey = key;
				break;
			}
		}
		// If a list does not yet exist for any of the search property keys, then return empty list since zero
		// Agents have global property keys matching search criteria.
		if(matchingKey == null)
			return ret;
		// Check the sub-list for Agents that match the given properties (test against only the search properties
		// for matches).
		// TODO Iterate through shortest available sub-list, instead of defaulting to first sub-list.
		for(Agent agent : globalPropertyKeyAgents.get(matchingKey)) {
			if(isPropertiesMatch(agent.propertyListeners, keys, vals)) {
				ret.add(agent);
				if(firstOnly)
					return ret;
			}
		}
		// Return list of Agents whose properties:
		//   -include at least all the given properties
		//   -with values that match all the given search values
		return ret;
	}

	private boolean isPropertiesMatch(HashMap<String, AgentPropertyListener<?>> propertyListeners, String[] keys,
			Object[] vals) {
		for(int i=0; i<keys.length; i++) {
			// if listener doesn't exist for key then return false, because Agent doesn't have one of the properties
			AgentPropertyListener<?> listener = propertyListeners.get(keys[i]);
			if(listener == null) {
				return false;
			}
			// If the given value is null, and the listener returns non-null, then return false due to mismatch, or
			// if the value returned by the listener does not match given value, then return false due to mismatch.
			Object listenerVal = listener.getValue();
			if(listenerVal == null) {
				if(vals[i] != null) {
					return false;
				}
			}
			else if(!listenerVal.equals(vals[i])) {
				return false;
			}
		}
		// return true because all search properties match
		return true;
	}
}
