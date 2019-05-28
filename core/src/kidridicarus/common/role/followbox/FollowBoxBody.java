package kidridicarus.common.role.followbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentBody;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.agency.agentjoint.AgentMouseJoint;
import kidridicarus.agency.agentjoint.AgentMouseJointDef;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.rolebody.RoleBody;

public abstract class FollowBoxBody extends RoleBody {
	protected abstract CFBitSeq getCatBits();
	protected abstract CFBitSeq getMaskBits();

	// if the target position is at least this far away from the current position then reset the b2body
	// TODO: is 50 pixels right?
	private static final float RESET_DIST = UInfo.P2M(50);

	private Object sensorBoxUserData;
	private AgentMouseJoint mj;
	private boolean isSensor;

	public FollowBoxBody(PhysicsHooks physHooks, Rectangle bounds, boolean isSensor, Object sensorBoxUserData) {
		super(physHooks);
		this.isSensor = isSensor;
		this.physHooks = physHooks;
		this.sensorBoxUserData = sensorBoxUserData;
		defineBody(bounds);
	}

	private void defineBody(Rectangle bounds) {
		// destroy the old bodies if necessary
		if(mj != null && mj.getBodyA() != null) {
			// destroy the temp bodyA used by mouse joint, and the mouse joint
			physHooks.destroyBody(mj.getBodyA());
		}
		if(agentBody != null)
			physHooks.destroyBody(agentBody);
		// set body size info and create new body
		setBoundsSize(bounds.width, bounds.height);
		createRegBody(physHooks, bounds, getCatBits(), getMaskBits());
		createMouseJoint(physHooks, bounds.getCenter(new Vector2()));
	}

	private void createRegBody(PhysicsHooks physHooks, Rectangle bounds, CFBitSeq catBits, CFBitSeq maskBits) {
		agentBody = ABodyFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		agentBody.setGravityScale(0f);
		if(isSensor) {
			ABodyFactory.makeSensorBoxFixture(agentBody, catBits, maskBits, sensorBoxUserData,
					bounds.width, bounds.height);
		}
		else {
			ABodyFactory.makeBoxFixture(agentBody, catBits, maskBits, sensorBoxUserData,
					bounds.width, bounds.height);
		}
	}

	// mouse joint allows body to quickly change position without destroying/recreating the body/fixture constantly
	private void createMouseJoint(PhysicsHooks physHooks, Vector2 position) {
		// TODO: find a better place to stick this temp body 
		AgentBody tempA = ABodyFactory.makeDynamicBody(physHooks, new Vector2(0f, 0f));
		tempA.setGravityScale(0f);

		// the fake body does not contact anything
		// TODO is a fixture necessary? can the next line of code be deleted?
		ABodyFactory.makeSensorBoxFixture(tempA, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK, null,
				0.01f, 0.01f);

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
		if(diff.len() >= RESET_DIST)
			resetFollowBoxPosition(position);
		else
			mj.setTarget(position);
	}

	private void resetFollowBoxPosition(Vector2 position) {
		Rectangle oldBounds = getBounds();
		defineBody(new Rectangle(position.x - oldBounds.width/2f, position.y - oldBounds.height/2f,
				oldBounds.width, oldBounds.height));
	}

	@Override
	public void dispose() {
		physHooks.destroyBody(mj.getBodyA());	// destroy the temp bodyA used by mouse joint
		super.dispose();
	}
}
