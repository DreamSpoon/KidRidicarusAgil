package kidridicarus.story.rolebody;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.AgentBody;
import kidridicarus.agency.PhysicsHooks;

public class RoleBody implements Disposable {
	protected AgentBody agentBody;
	protected PhysicsHooks physHooks; 
	// bounds size is for information purposes only, it is not necessarily the current dimensions of b2body
	private Vector2 boundsSize;

	public RoleBody(PhysicsHooks physHooks) {
		this.physHooks = physHooks;
		this.agentBody = null;
		boundsSize = new Vector2(0f, 0f);
	}

	public void setBoundsSize(float width, float height) {
		boundsSize.set(width, height);
	}

	public Rectangle getBounds() {
		return new Rectangle(agentBody.getPosition().x - boundsSize.x/2f,
				agentBody.getPosition().y - boundsSize.y/2f, boundsSize.x, boundsSize.y);
	}

	public Vector2 getPosition() {
		return agentBody.getPosition();
	}

	public Vector2 getVelocity() {
		return agentBody.getVelocity();
	}

	public void setVelocity(float x, float y) {
		agentBody.setVelocity(x, y);
	}

	public void setVelocity(Vector2 velocity) {
		agentBody.setVelocity(velocity.x, velocity.y);
	}

	public void zeroVelocity(boolean zeroX, boolean zeroY) {
		agentBody.setVelocity(zeroX ? 0f : agentBody.getVelocity().x, zeroY ? 0f : agentBody.getVelocity().y);
	}

	public void applyImpulse(Vector2 impulse) {
		agentBody.applyImpulse(impulse);
	}

	public void applyForce(Vector2 force) {
		agentBody.applyForce(force);
	}

	@Override
	public void dispose() {
		if(agentBody != null) {
			physHooks.destroyBody(agentBody);
			agentBody = null;
		}
	}
}
