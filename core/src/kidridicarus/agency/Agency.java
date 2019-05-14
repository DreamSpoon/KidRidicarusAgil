package kidridicarus.agency;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.agencychange.AgentPlaceholder;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentbody.AgentContactFilter;
import kidridicarus.agency.agentbody.AgentContactListener;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.AgentClassList;
import kidridicarus.agency.tool.AllowOrder;
import kidridicarus.agency.tool.AllowOrderList.AllowOrderListIter;
import kidridicarus.agency.tool.Ear;
import kidridicarus.agency.tool.EarPlug;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.tool.QQ;

/*
 * Desc:
 *   Agency contains Agents and organizes interactions between Agents (Agent-to-Agent), and between Agents and
 *   Agency (Agent-to-Agency, and Agency-to-Agent).
 * Wikipedia definitions for some context:
 *   "Agency - Psychology"
 *     https://en.wikipedia.org/wiki/Agency_(psychology)
 *     "In psychology, agents are goal-directed entities that are able to monitor their environment to select and
 *     perform efficient means-ends actions that are available in a given situation to achieve an intended goal."
 *   "Structure and agency"
 *     https://en.wikipedia.org/wiki/Structure_and_agency
 *     "Agency is the capacity of individuals to act independently and to make their own free choices."
 *   "Government Agency"
 *     https://en.wikipedia.org/wiki/Government_agency
 *     "A government or state agency, sometimes an appointed commission, is a permanent or semi-permanent organization
 *     in the machinery of government that is responsible for the oversight and administration of specific functions,
 *     such as an intelligence agency."
 * And a Google dictionary definition of "Agency" to complete the picture:
 *   http://www.google.com/search?q=agency+definition
 *   "a business or organization established to provide a particular service, typically one that involves organizing
 *   transactions between two other parties."
 *
 * Notes regarding Box2D World and Agency:
 *   In contrast with Box2D's World.step method, Agency has an Agency.update and Agency.draw method. These
 *   two methods could be combined into a single "step" method, but this option would reduce flexibility.
 *   Reasons for keeping update and draw methods separate:
 *     1) Multiple updates can be run before a draw is called, and
 *     2) Multiple draws can be called between updates.
 *   Why do it this way?
 *     1) Map loading may require running multiple updates to completely load map and sub-Agents - do not draw
 *        during this period in order to avoid showing "error" visuals.
 *     2) "Forced framerate" may require drawing of Agency multiple times between updates
 *        (e.g. 1 update per 3 draw frames to simulate 20 fps - update method is called 20 times per second
 *        while draw method is called 60 times per second).
 */
public class Agency implements Disposable {
	private AgentClassList allAgentsClassList;
	private TextureAtlas panAtlas;
	private AgencyIndex agencyIndex;
	// Agency needs an earplug because it looks cool... and lets Agents exchange audio info
	private EarPlug earplug;
	private Eye myEye;
	// how much time has passed (via updates) since this Agency was constructed?
	private float globalTimer;
	private World panWorld;
	private PhysicsHooks panPhysHooks;

	public Agency(AgentClassList allAgentsClassList, TextureAtlas atlas) {
		this.allAgentsClassList = allAgentsClassList;
		this.panAtlas = atlas;
		agencyIndex = new AgencyIndex();
		globalTimer = 0f;
		earplug = new EarPlug();
		myEye = null;

		panWorld = new World(new Vector2(0, -10f), true);
		panWorld.setContactListener(new AgentContactListener());
		panWorld.setContactFilter(new AgentContactFilter());
		panPhysHooks = new PhysicsHooks(panWorld);
	}

	public void update(final float timeDelta) {
		panWorld.step(timeDelta, 6, 2);
		globalTimer += timeDelta;

		// loop through list of agents receiving updates, calling each agent's update method
		agencyIndex.iterateThroughUpdateListeners(new AllowOrderListIter() {
				@Override
				public boolean iterate(Object obj) {
					if(obj instanceof AgentUpdateListener)
						((AgentUpdateListener) obj).update(new FrameTime(timeDelta, globalTimer));
					// continue iterating
					return false;
				}
			});
		// apply changes
		agencyIndex.processQueue();
	}

	/*
	 * Create many agents from a collection of agent properties, and return a list of the created Agents.
	 */
	private List<Agent> hookCreateAgents(Collection<ObjectProperties> agentProps) {
		LinkedList<Agent> aList = new LinkedList<Agent>();
		Iterator<ObjectProperties> apIter = agentProps.iterator();
		while(apIter.hasNext())
			aList.add(hookCreateAgent(apIter.next()));
		return aList;
	}

	public Agent externalCreateAgent(ObjectProperties properties) {
		return hookCreateAgent(properties);
	}

	/*
	 * Create an agent from the given agent properties.
	 * See website:
	 * http://www.avajava.com/tutorials/lessons/how-do-i-create-an-object-via-its-multiparameter-constructor-using-reflection.html
	 */
	private Agent hookCreateAgent(ObjectProperties properties) {
		String agentClassAlias = properties.getString(AgencyKV.KEY_AGENT_CLASS, null);
		if(agentClassAlias == null)
			throw new IllegalArgumentException(AgencyKV.KEY_AGENT_CLASS + " key not found in agent definition.");

		Class<?> agentClass = allAgentsClassList.get(agentClassAlias);
		if(agentClass == null)
			return null;

		Agent newlyCreatedAgent = null;
		// When the Agent object is constructed, it may invoke calls to enable Agent updates or set Agent draw order,
		// so a placeholder must be inserted before creating the object. The Agent's real reference is put into the
		// placeholder after the Agent constructor code is finished executing. All changes tied to the placeholder will
		// not be executed until the change queue is processed.
		AgentPlaceholder agentPlaceholder = new AgentPlaceholder(null);
		agencyIndex.queueAddAgent(agentPlaceholder);
		// Agency hooks are unique to each Agent, for disposal coordination. e.g. Automatically disposing all
		// bodies created by Agent when Agent is disposed. Only an AgentPlaceholder is needed for this purpose,
		// since the hooks will only need an Agent ref when Agency enters the changeQ processing phase, which occurs
		// after all Agent constructors are finished processing.
		AgentHooks newInternalHooks = new AgentHooks(agentPlaceholder, panPhysHooks, panAtlas);
		try {
			Constructor<?> constructor =
					agentClass.getConstructor(new Class[] { AgentHooks.class, ObjectProperties.class });
			newlyCreatedAgent = (Agent) constructor.newInstance(new Object[] { newInternalHooks, properties });
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Unable to create Agent.");
		}

		agentPlaceholder.agent = newlyCreatedAgent;
		return newlyCreatedAgent;
	}

	public void setEar(Ear ear) {
		this.earplug.setEar(ear);
	}

	public void setEye(Eye eye) {
		this.myEye = eye;
	}

	public void draw() {
		if(myEye == null)
			return;
		myEye.begin();
		agencyIndex.iterateThroughDrawListeners(new AllowOrderListIter() {
			@Override
			public boolean iterate(Object obj) {
				if(obj instanceof AgentDrawListener)
					((AgentDrawListener) obj).draw(myEye);
				else
					QQ.pr("unknown object in draw list iteration object: " + obj);
				// return false to continue iterating
				return false;
			}
		});
		myEye.end();
	}

	private LinkedList<Agent> hookGetAgentsByProperties(String[] keys, Object[] vals) {
		return agencyIndex.getAgentsByProperties(keys, vals, false);
	}

//	private Agent hookGetFirstAgentByProperties(String[] keys, Object[] vals) {
//		LinkedList<Agent> aList = agencyIndex.getAgentsByProperties(keys, vals, true);
//		if(aList.isEmpty())
//			return null;
//		return aList.getFirst();
//	}

//	private LinkedList<Agent> hookGetAgentsByProperty(String key, Object val) {
//		return agencyIndex.getAgentsByProperties(new String[] { key }, new Object[] { val }, false);
//	}

	private Agent hookGetFirstAgentByProperty(String key, Object val) {
		LinkedList<Agent> aList = agencyIndex.getAgentsByProperties(new String[] { key }, new Object[] { val }, true);
		if(aList.isEmpty())
			return null;
		return aList.getFirst();
	}

	public World externalGetWorld() {
		return panWorld;
	}

	// remove all Agents from Agency, but do not dispose Agency
	public void removeAllAgents() {
		agencyIndex.removeAllAgents();
	}

	/*
	 * Dispose and remove all Agents and dispose Agency.
	 * Note: Agency must not be disposed during update frame.
	 */
	@Override
	public void dispose() {
		removeAllAgents();
		panWorld.dispose();
	}

	/*
	 * Even though this class is inside Agency, it is called AgentHooks and not AgencyHooks because the Object is
	 * "bound" to each Agent, not just this Agency. In future code, an AgencyHooks class will also be created, for
	 * external hooks usage.
	 * Side-Note: See class AgentScriptHooks.
	 */
	public class AgentHooks {
		private AgentPlaceholder ownerAP;
		public final PhysicsHooks physHooks;
		public final TextureAtlas atlas;

		private AgentHooks(AgentPlaceholder ownerAP, PhysicsHooks physHooks, TextureAtlas atlas) {
			this.ownerAP = ownerAP;
			this.physHooks = physHooks;
			this.atlas = atlas;
		}

		public Agent createAgent(ObjectProperties properties) {
			return hookCreateAgent(properties);
		}

		public List<Agent> createAgents(LinkedList<ObjectProperties> propertiesList) {
			return hookCreateAgents(propertiesList);
		}

		// Agent can only remove itself, if a sub-Agent needs removal then the sub-Agent must remove itself
		public void removeThisAgent() {
			agencyIndex.queueRemoveAgent(ownerAP);
		}

		public void addPropertyListener(boolean isGlobal, String propertyKey,
				AgentPropertyListener<?> propertyListener) {
			// method order of arguments differs from the changeQ method, for inline listener creation convenience
			agencyIndex.queueAddPropertyListener(ownerAP, propertyListener, propertyKey, isGlobal);
		}

		public void removePropertyListener(String propertyKey) {
			agencyIndex.queueRemovePropertyListener(ownerAP, propertyKey);
		}

		public void addUpdateListener(AllowOrder updateOrder, AgentUpdateListener updateListener) {
			// method order of arguments differs from the changeQ method, for inline listener creation convenience
			agencyIndex.queueAddUpdateListener(ownerAP, updateListener, updateOrder);
		}

		public void removeUpdateListener(AgentUpdateListener updateListener) {
			agencyIndex.queueRemoveUpdateListener(ownerAP, updateListener);
		}

		public void addDrawListener(AllowOrder drawOrder, AgentDrawListener drawListener) {
			// method order of arguments differs from the changeQ method, for inline listener creation convenience
			agencyIndex.queueAddDrawListener(ownerAP, drawListener, drawOrder);
		}

		public void removeDrawListener(AgentDrawListener drawListener) {
			agencyIndex.queueRemoveDrawListener(ownerAP, drawListener);
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
			AgentRemoveListener removeListener = new AgentRemoveListener(ownerAP, otherAgent, callback);
			agencyIndex.queueAddAgentRemoveListener(ownerAP, removeListener);
			return removeListener;
		}

		public void removeAgentRemoveListener(AgentRemoveListener removeListener) {
			agencyIndex.queueRemoveAgentRemoveListener(ownerAP, removeListener);
		}

		public Agent getFirstAgentByProperty(String key, Object val) {
			return hookGetFirstAgentByProperty(key, val);
		}

		public LinkedList<Agent> getAgentsByProperties(String[] keys, Object[] vals) {
			return hookGetAgentsByProperties(keys, vals);
		}

		public boolean isValidAgentClassAlias(String strClassAlias) {
			return allAgentsClassList.get(strClassAlias) != null;
		}

		public Ear getEar() {
			return earplug.getEar();
		}

		public Eye getEye() {
			return myEye;
		}
	}

	public class PhysicsHooks {
		private World physHooksWorld;

		public PhysicsHooks(World physHooksWorld) {
			this.physHooksWorld = physHooksWorld;
		}

		public Body createBody(BodyDef bdef) {
			return physHooksWorld.createBody(bdef);
		}

		public void destroyBody(Body body) {
			physHooksWorld.destroyBody(body);
		}

		public Joint createJoint(MouseJointDef mjdef) {
			return physHooksWorld.createJoint(mjdef);
		}
	}
}