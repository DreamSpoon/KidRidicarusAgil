package kidridicarus.agency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;

/*
 * A list of all Agents in the Agency, and their associated update listeners and draw listeners. Agents can have
 * remove listeners, which receive callback when the Agent is removed from Agency.
 * PropertyListeners are not queued, but added immediately.
 */
class AgencyIndex {
	private interface AgencyChange { public void applyChange(); }

	private LinkedBlockingQueue<AgencyChange> generalChangeQ;
	private LinkedBlockingQueue<AgencyChange> updateListenerChangeQ;

	private HashSet<Agent> allAgents;
	private HashMap<AgentUpdateListener, Float> allUpdateListeners;
	private TreeMap<Float, HashSet<AgentUpdateListener>> orderedUpdateListeners;
	private HashMap<AgentDrawListener, Float> allDrawListeners;
	private TreeMap<Float, HashSet<AgentDrawListener>> orderedDrawListeners;
	// sub-lists of Agents that have properties, indexed by property String
	private HashMap<String, LinkedList<Agent>> globalPropertyKeyAgents;

	AgencyIndex() {
		allAgents = new HashSet<Agent>();

		allDrawListeners = new HashMap<AgentDrawListener, Float>();
		orderedDrawListeners = new TreeMap<Float, HashSet<AgentDrawListener>>();
		globalPropertyKeyAgents = new HashMap<String, LinkedList<Agent>>();
		generalChangeQ = new LinkedBlockingQueue<AgencyChange>();

		allUpdateListeners = new HashMap<AgentUpdateListener, Float>();
		orderedUpdateListeners = new TreeMap<Float, HashSet<AgentUpdateListener>>();
		updateListenerChangeQ = new LinkedBlockingQueue<AgencyChange>();
	}

	public void processGeneralQueue() {
		while(!generalChangeQ.isEmpty())
			generalChangeQ.poll().applyChange();
	}

	public void queueAddAgent(final Agent agent) {
		generalChangeQ.add(new AgencyChange() {
				@Override
				public void applyChange() { allAgents.add(agent); }
			});
	}

	/*
	 * Remove an Agent from the index, calling remove listeners if needed.
	 * If remove listeners are present, then they must be called first to prevent altering the rest of the Agent's
	 * state by way of removing draw, update, etc. listeners.
	 */
	public void queueRemoveAgent(final Agent agent) {
		generalChangeQ.add(new AgencyChange() {
				@Override
				public void applyChange() {
					// If other Agents are listening for remove callback then do it, and garbage collect remove
					// listeners.
					removeAllAgentRemoveListeners(agent);
					removeAllUpdateListeners(agent);
					removeAllDrawListeners(agent);
					removeAllAgentPropertyListeners(agent);
					allAgents.remove(agent);
				}
			});
	}

	public void processUpdateListenerQueue() {
		while(!updateListenerChangeQ.isEmpty())
			updateListenerChangeQ.poll().applyChange();
	}

	// add a single update listener and associate it with the given Agent
	public void queueAddUpdateListener(final Agent agent, final AgentUpdateListener updateListener,
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

	// remove a single update listener associated with the given Agent
	public void queueRemoveUpdateListener(final Agent agent, final AgentUpdateListener updateListener) {
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

	/*
	 * Remove all update listeners associated with the given Agent.
	 */
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

	// add a single draw listener and associate it with the given Agent
	public void queueAddDrawListener(final Agent agent, final AgentDrawListener drawListener,
			final float drawOrder) {
		generalChangeQ.add(new AgencyChange() {
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

	// remove a single draw listener associated with the given Agent
	public void queueRemoveDrawListener(final Agent agent, final AgentDrawListener drawListener) {
		generalChangeQ.add(new AgencyChange() {
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

	// remove all draw listeners associated with the given Agent
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

	/*
	 * Associate a single listener with the given agent and other Agent. An Agent is allowed to add a remove
	 * listener to itself. This is a good way to handle "dispose" functionality.
	 * Note: AgencyIndex doesn't directly keep a list of all remove listeners, each Agent keeps their own list.
	 */
	public void queueAddAgentRemoveListener(final Agent agent, final AgentRemoveListener removeListener) {
		generalChangeQ.add(new AgencyChange() {
				@Override
				public void applyChange() {
					// This Agent keeps a ref to the listener, so that this Agent can delete the listener when this
					// Agent is removed (garbage collection).
					agent.myAgentRemoveListeners.add(removeListener);
					// The other Agent keeps a ref to the listener, so that the other Agent can callback this Agent
					// when other Agent is removed (agent removal callback).
					removeListener.otherAgent.otherAgentRemoveListeners.add(removeListener);
				}
			});
	}

	// disassociate a single listener from the given agent and other Agent
	public void queueRemoveAgentRemoveListener(final Agent agent, final AgentRemoveListener removeListener) {
		generalChangeQ.add(new AgencyChange() {
				@Override
				public void applyChange() {
					agent.myAgentRemoveListeners.remove(removeListener);
					removeListener.otherAgent.otherAgentRemoveListeners.remove(removeListener);
				}
			});
	}

	/*
	 * 1) Agent removal callbacks,
	 * 2) Disassociate all listeners associated with the given Agent,
	 * 3) Ensuring other Agent's references to these listeners are also disassociated. 
	 */
	private void removeAllAgentRemoveListeners(Agent agent) {
		// first, do all Agent removal callbacks (the other Agents are listening for this Agent's removal)
		for(AgentRemoveListener otherListener : agent.otherAgentRemoveListeners) {
			otherListener.callback.preRemoveAgent();
			// since the move listener has been called, the listener itself must now be disassociated from other Agent
			otherListener.listeningAgent.myAgentRemoveListeners.remove(otherListener);
		}
		// second, remove all of my listener references held by other agents
		for(AgentRemoveListener myListener : agent.myAgentRemoveListeners)
			myListener.otherAgent.otherAgentRemoveListeners.remove(myListener);
		// third, remove all of my listeners
		agent.myAgentRemoveListeners.clear();
	}

	public void addPropertyListener(final Agent agent, final AgentPropertyListener<?> listener,
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

	public void removePropertyListener(final Agent agent, final String propertyKey) {
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
	public LinkedList<Agent> getAgentsByProperties(String[] keys, Object[] vals, boolean firstOnly) {
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

	public void doPreStepAgentUpdates(FrameTime frameTime) {
		Iterator<Entry<Float, HashSet<AgentUpdateListener>>> iter =
				orderedUpdateListeners.headMap(0.0f).entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Float, HashSet<AgentUpdateListener>> pair = iter.next();
			for(AgentUpdateListener updateListener : pair.getValue())
				updateListener.update(frameTime);
		}
	}

	public void doPostStepAgentUpdates(FrameTime frameTime) {
		Iterator<Entry<Float, HashSet<AgentUpdateListener>>> iter =
				orderedUpdateListeners.tailMap(0.0f).entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Float, HashSet<AgentUpdateListener>> pair = iter.next();
			for(AgentUpdateListener updateListener : pair.getValue())
				updateListener.update(frameTime);
		}
	}

	public void doAgentDraws(Eye eye) {
		// iterate through sorted subsets, from lowest draw order to highest draw order
		Iterator<Entry<Float, HashSet<AgentDrawListener>>> iter = orderedDrawListeners.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Float, HashSet<AgentDrawListener>> pair = iter.next();
			// iterate through subset of listeners at this specific draw order
			for(AgentDrawListener drawListener : pair.getValue())
				drawListener.draw(eye);
		}
	}

	/*
	 * Batch remove all Agents from Agency, calling Agent remove listeners as needed.
	 * Remove listeners are called first to prevent changing Agent's state until all remove listeners are called.
	 * See:
	 * https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
	 */
	public void removeAllAgents() {
		for(Agent agent : allAgents) {
			removeAllAgentRemoveListeners(agent);
			agent.updateListeners.clear();
			agent.drawListeners.clear();
			agent.globalPropertyKeys.clear();
			agent.propertyListeners.clear();
		}
		orderedUpdateListeners.clear();
		allUpdateListeners.clear();
		orderedDrawListeners.clear();
		allDrawListeners.clear();
		// clear all sub-lists within allPropertyAgents
		Iterator<Entry<String, LinkedList<Agent>>> iter = globalPropertyKeyAgents.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, LinkedList<Agent>> pair = iter.next();
			LinkedList<Agent> subList = pair.getValue();
			subList.clear();
		}
		globalPropertyKeyAgents.clear();
		allAgents.clear();
	}
}
