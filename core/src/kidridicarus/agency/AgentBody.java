package kidridicarus.agency;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.common.info.CommonCF;

/*
 * Assume that an AgentBody can contain exactly one Box2D body. If more bodies are needed then a body list
 * scenario may be fruitful.
 * Body is brute and dumb - any movements, forces, etc. can be accomplished simply by calling the body's methods,
 * but the methods tend to the simple: setPosition, applyForce, applyImpulse, etc.
 * However, for more organized/coordinated movements, use a spine instead (e.g. SamusSpine.applyDamageKick method).
 * TODO
 *   -use applyForce, applyImpulse, setVelocity paradigm as much as possible, instead of manually modifying
 *    body position
 *   -caller should explicitly invoke the create/destroy methods when manually modifying the position of AgentBody
 *   -remove the defineBody/dispose paradigm, replace with the above described create/destroy method paradigm
 */
public class AgentBody {
	private final Agent parentAgent;
	final Body b2body;

	public AgentBody(Agent parentAgent, Body b2body) {
		this.parentAgent = parentAgent;
		this.b2body = b2body;
	}

	public Fixture createFixture(FixtureDef fdef) {
		return b2body.createFixture(fdef);
	}

	public void setVelocity(float x, float y) {
		b2body.setLinearVelocity(x, y);
	}

	public void applyImpulse(Vector2 impulse) {
		b2body.applyLinearImpulse(impulse, b2body.getWorldCenter(), true);
	}

	public void applyForce(Vector2 f) {
		b2body.applyForceToCenter(f, true);
	}

	public void setGravityScale(float gravityScale) {
		b2body.setGravityScale(gravityScale);
	}

	public void setAwake(boolean isAwake) {
		b2body.setAwake(isAwake);
	}

	// TODO this convenience method should go somewhere else
	public void disableAllContacts() {
		for(Fixture fix : b2body.getFixtureList()) {
			((AgentBodyFilter) fix.getUserData()).categoryBits = CommonCF.NO_CONTACT_CFCAT;
			((AgentBodyFilter) fix.getUserData()).maskBits = CommonCF.NO_CONTACT_CFMASK;
			fix.refilter();
		}
	}

	public Agent getParent() {
		return parentAgent;
	}

	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	public Vector2 getVelocity() {
		return b2body.getLinearVelocity();
	}

	public float getMass() {
		return b2body.getMass();
	}

	public void setBullet(boolean isBullet) {
		b2body.setBullet(isBullet);
	}
}
