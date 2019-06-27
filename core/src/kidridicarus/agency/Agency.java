package kidridicarus.agency;

import java.util.Collection;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.tool.Ear;
import kidridicarus.agency.tool.EarPlug;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.EyePlug;
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
	private World panWorld;
	private AgencyIndex agencyIndex;
	private EyePlug eyePlug;
	private EarPlug earPlug;
	private GfxHooks panGfxHooks;
	private AudioHooks panAudioHooks;
	// how much time has passed (via updates) since this Agency was constructed?
	private float globalTimer;
	private AgencyContactListener worldContactListener;

	public Agency(TextureAtlas atlas) {
		worldContactListener = new AgencyContactListener();
		panWorld = new World(new Vector2(0, -10f), true);
		panWorld.setContactListener(worldContactListener);
		panWorld.setContactFilter(new AgencyContactFilter());
		agencyIndex = new AgencyIndex();
		eyePlug = new EyePlug();
		earPlug = new EarPlug();
		panGfxHooks = new GfxHooks(atlas, eyePlug);
		panAudioHooks = new AudioHooks(earPlug);
		globalTimer = 0f;
	}

	/*
	 * An external Agent create method is available, but an external Agent destroy method is not available.
	 * The Agent can only self-remove via the AgentHooks in the hooks bundle.
	 */
	public AgentHooksBundle createAgentHooksBundle() {
		Agent newAgent = new Agent();
		agencyIndex.addAgent(newAgent);
		return new AgentHooksBundle(newAgent, new AgentHooks(this, agencyIndex, newAgent),
				new PhysicsHooks(this, panWorld, newAgent), panAudioHooks, panGfxHooks);
	}

	public void update(final float timeDelta) {
		// call update listeners with update order < 0
		agencyIndex.doPreStepAgentUpdates(new FrameTime(timeDelta, globalTimer));
		// Process queued AgentBody destroys before World.step, so that contact changes caused by these destroys
		// will be available post-step. This is intended to allow greater flexibility re: event propagation.
		agencyIndex.processAgentB2DestroyQ();

		panWorld.step(timeDelta, 6, 2);
		// NOTE globalTimer is different for Agent post-step methods
		// TODO verify if this is the correct course of action?
		globalTimer += timeDelta;

		// call update listeners with update order >= 0
		agencyIndex.doPostStepAgentUpdates(new FrameTime(timeDelta, globalTimer));
		// process update listener queue after all update listers have been called
		agencyIndex.processUpdateListenerChangeQueue();
		// process the remove Agent queue last (body destroys are dependent on this, so technically second last)
		agencyIndex.processRemoveAgentQueue();
		// removal of Agents may have caused AgentBody destroy requests to be queued
		agencyIndex.processAgentB2DestroyQ();

		// clear begin and end contacts lists of "dirty" AgentContactSensors
		worldContactListener.endFrameCleanContactSensors();
	}

	public void draw() {
		eyePlug.getEye().begin();
		agencyIndex.doAgentDraws(eyePlug.getEye());
		eyePlug.getEye().end();
	}

	Collection<Agent> hookGetAgentsByProperties(String[] keys, Object[] vals) {
		return agencyIndex.getAgentsByProperties(keys, vals, false);
	}

	Agent hookGetFirstAgentByProperties(String[] keys, Object[] vals) {
		Collection<Agent> aList = agencyIndex.getAgentsByProperties(keys, vals, true);
		if(aList.isEmpty())
			return null;
		return aList.iterator().next();
	}

	void queueDestroyAgentBody(final Agent agent, final AgentBody agentBody) {
		agencyIndex.queueDestroyAgentBody(agent, agentBody);
	}

	void queueDestroyAgentFixture(AgentFixture agentFixture) {
		agencyIndex.queueDestroyAgentFixture(agentFixture);
	}

	public void setEar(Ear ear) {
		earPlug.setEar(ear);
	}

	public void setEye(Eye eye) {
		eyePlug.setEye(eye);
	}

	// for Box2D debug renderer
	public World externalGetWorld() {
		return panWorld;
	}

	float getAbsTime() {
		return globalTimer;
	}

	// remove all Agents from Agency, but do not dispose Agency
	public void removeAllAgents() {
		agencyIndex.removeAllAgents();
		// removal of Agents may have created AgentBody destroy requests
		agencyIndex.processAgentB2DestroyQ();
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
