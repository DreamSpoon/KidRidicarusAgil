package kidridicarus.common.role.followbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentBody;
import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentjoint.AgentMouseJoint;
import kidridicarus.agency.agentjoint.AgentMouseJointDef;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.rolebody.RoleBody;

public abstract class FollowBoxBody extends RoleBody {
	protected abstract AgentFixture createAgentFixture(Vector2 size);

	private static final float GRAVITY_SCALE = 0f;
	// if the target position is at least this far away from the current position then reset the b2body
	// TODO: is 50 pixels right?
	private static final float RESET_DIST = UInfo.P2M(50);

	protected AgentFixture agentFixture;
	private AgentMouseJoint mj;
	private Vector2 boundSize;

	public FollowBoxBody(PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		this.boundSize = bounds.getSize(new Vector2());
		defineBody(bounds.getCenter(new Vector2()));
	}

	private void defineBody(Vector2 position) {
		// destroy the old bodies if necessary
		if(mj != null && mj.getBodyA() != null) {
			// destroy the temp bodyA used by mouse joint, and the mouse joint
			physHooks.queueDestroyAgentBody(mj.getBodyA());
		}
		if(agentBody != null)
			physHooks.queueDestroyAgentBody(agentBody);
		// set body size info and create new body
		createRegBody(physHooks, position);
		createMouseJoint(physHooks, position);
	}

	private void createRegBody(PhysicsHooks physHooks, Vector2 position) {
		agentBody = ABodyFactory.makeDynamicBody(physHooks, position);
		agentBody.setGravityScale(GRAVITY_SCALE);
		agentFixture = createAgentFixture(boundSize);
	}

	// mouse joint allows body to quickly change position without destroying/recreating the body/fixture constantly
	private void createMouseJoint(PhysicsHooks physHooks, Vector2 position) {
		// TODO: find a better place to stick this temp body 
		AgentBody tempA = ABodyFactory.makeDynamicBody(physHooks, new Vector2(0f, 0f));
		tempA.setGravityScale(0f);

		// the fake body does not contact anything
		// TODO is a fixture necessary? can the next line of code be deleted?
		ABodyFactory.makeSensorBoxFixture(tempA, new AgentFilter(), 0.01f, 0.01f);

		AgentMouseJointDef amjdef = new AgentMouseJointDef();
		// this body is supposedly ignored by box2d, but needs to be a valid non-static body (non-sensor also?)
		amjdef.agentBodyA = tempA;
		// this is the body that will move to "catch up" to the mouse joint target
		amjdef.agentBodyB = agentBody;
		amjdef.maxForce = 5000f * agentBody.getMass();
		amjdef.frequencyHz = 5f;
		amjdef.dampingRatio = 0.9f;
		amjdef.target.set(position);
		mj = (AgentMouseJoint) physHooks.createJoint(amjdef);
	}

	public void setPosition(Vector2 position) {
		Vector2 diff = position.cpy().sub(agentBody.getPosition());
		// if target position is too far away from current position, then destroy box body and re-create at target
		if(diff.len() >= RESET_DIST)
			defineBody(position);
		else	// otherwise do regular set target
			mj.setTarget(position);
	}
}
