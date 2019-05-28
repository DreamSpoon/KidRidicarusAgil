package kidridicarus.agency;

import java.util.HashSet;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.agentbody.AgentContactFilter;
import kidridicarus.agency.agentbody.AgentContactListener;
import kidridicarus.agency.tool.Ear;
import kidridicarus.agency.tool.EarPlug;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;

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
	World panWorld;
	TextureAtlas panAtlas;
	Eye myEye;
	EarPlug earplug;
	AgencyIndex agencyIndex;
	private GfxHooks panGfxHooks;
	private AudioHooks panAudioHooks;
	// how much time has passed (via updates) since this Agency was constructed?
	private float globalTimer;
	private HashSet<AgentBody> destroyedBodies = new HashSet<AgentBody>();

	public Agency(TextureAtlas atlas) {
		panWorld = new World(new Vector2(0, -10f), true);
		panWorld.setContactListener(new AgentContactListener());
		panWorld.setContactFilter(new AgentContactFilter());
		this.panAtlas = atlas;
		myEye = null;
		earplug = new EarPlug();
		agencyIndex = new AgencyIndex();
		panGfxHooks = new GfxHooks(this);
		panAudioHooks = new AudioHooks(this);
		globalTimer = 0f;
	}

	public void update(final float timeDelta) {
		// call update listeners with update order < 0
		agencyIndex.doPreStepAgentUpdates(new FrameTime(timeDelta, globalTimer));
		// process the update listener queue, so that listeners with update order >= 0 can be called post World.step
		agencyIndex.processUpdateListenerQueue();

		panWorld.step(timeDelta, 6, 2);
		// NOTE globalTimer is different for Agent post-step methods
		// TODO verify if this is the correct course of action?
		globalTimer += timeDelta;

		// call update listeners with update order >= 0
		agencyIndex.doPostStepAgentUpdates(new FrameTime(timeDelta, globalTimer));
		// process update listener queue before general queue, because general queue includes Agent removal
		agencyIndex.processUpdateListenerQueue();
		agencyIndex.processDrawListenerQueue();
		agencyIndex.processRemovalNodeDestroyQueue();
		agencyIndex.processRemoveAgentQueue();
	}

	public AgentHooksBundle createAgentHooksBundle() {
		Agent newAgent = new Agent();
		agencyIndex.addAgent(newAgent);
		return new AgentHooksBundle(newAgent, new AgentHooks(this, newAgent), new PhysicsHooks(this, newAgent),
				panAudioHooks, panGfxHooks);
	}

	public AgentBody createAgentBody(Agent agent, BodyDef bdef) {
		return new AgentBody(agent, panWorld.createBody(bdef));
	}

	public void destroyAgentBody(AgentBody agentBody) {
		if(destroyedBodies.contains(agentBody))
			throw new IllegalArgumentException("Cannot destory AgentBody twice, ref="+agentBody);
		destroyedBodies.add(agentBody);
		panWorld.destroyBody(agentBody.b2body);
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
		agencyIndex.doAgentDraws(myEye);
		myEye.end();
	}

	LinkedList<Agent> hookGetAgentsByProperties(String[] keys, Object[] vals) {
		return agencyIndex.getAgentsByProperties(keys, vals, false);
	}

	Agent hookGetFirstAgentByProperties(String[] keys, Object[] vals) {
		LinkedList<Agent> aList = agencyIndex.getAgentsByProperties(keys, vals, true);
		if(aList.isEmpty())
			return null;
		return aList.getFirst();
	}

	LinkedList<Agent> hookGetAgentsByProperty(String key, Object val) {
		return agencyIndex.getAgentsByProperties(new String[] { key }, new Object[] { val }, false);
	}

	Agent hookGetFirstAgentByProperty(String key, Object val) {
		LinkedList<Agent> aList = agencyIndex.getAgentsByProperties(new String[] { key }, new Object[] { val }, true);
		if(aList.isEmpty())
			return null;
		return aList.getFirst();
	}

	// for Box2D debug renderer
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
}