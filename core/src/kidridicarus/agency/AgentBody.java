package kidridicarus.agency;

import java.util.HashSet;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

public class AgentBody {
	private final PhysicsHooks physicsHooks;
	final Body b2body;
	private HashSet<AgentFixture> fixtures;
	// flag to mark if the AgentBody has already been queued for removal
	boolean isDestroyQueueDirty;
	// TODO private Object userData;

	AgentBody(PhysicsHooks physicsHooks, Body b2body) {
		this.physicsHooks = physicsHooks;
		this.b2body = b2body;
		fixtures = new HashSet<AgentFixture>();
		isDestroyQueueDirty = false;
	}

	public AgentFixture createFixture(AgentFixtureDef afDef) {
		if(isDestroyQueueDirty) {
			throw new IllegalStateException("Cannot create AgentFixture while parent AgentBody is queued for "+
					"destroy.");
		}
		Fixture b2fixture = b2body.createFixture(afDef.getB2FixtureDef());
		AgentFixture newFixture = new AgentFixture(physicsHooks.myAgent, this, b2fixture, afDef);
		fixtures.add(newFixture);
		return newFixture;
	}

	public void queueDestroyFixture(AgentFixture agentFixture) {
		if(isDestroyQueueDirty) {
			throw new IllegalStateException("Cannot queue destruction of AgentFixture while parent AgentBody is "+
					"queued for destroy.");
		}
		if(!fixtures.contains(agentFixture))
			throw new IllegalArgumentException("Cannot remove AgentFixture that was not created by this AgentBody.");
		physicsHooks.queueDestroyFixture(agentFixture);
	}

	void doDestroyFixture(AgentFixture agentFixture) {
		fixtures.remove(agentFixture);
		b2body.destroyFixture(agentFixture.b2Fixture);
	}

	public void setVelocity(Vector2 velocity) {
		b2body.setLinearVelocity(velocity);
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

	public Agent getAgent() {
		return physicsHooks.myAgent;
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

	void destroy() {
		b2body.getWorld().destroyBody(b2body);
	}
}
