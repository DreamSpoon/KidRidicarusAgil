package kidridicarus.story.rolebody;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentBody;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.common.tool.Direction4;

public class RoleBody {
	protected PhysicsHooks physHooks; 
	protected AgentBody agentBody;

	public RoleBody(PhysicsHooks physHooks) {
		this.physHooks = physHooks;
		this.agentBody = null;
	}

	public Vector2 getPosition() {
		return agentBody.getPosition();
	}

	public Vector2 getVelocity() {
		return agentBody.getVelocity();
	}

	public void setVelocity(float x, float y) {
		this.agentBody.setVelocity(x, y);
	}

	public void setVelocity(Vector2 velocity) {
		this.agentBody.setVelocity(velocity.x, velocity.y);
	}

	public void zeroVelocity(boolean zeroX, boolean zeroY) {
		this.agentBody.setVelocity(zeroX ? 0f : agentBody.getVelocity().x, zeroY ? 0f : agentBody.getVelocity().y);
	}

	public void applyImpulse(Vector2 impulse) {
		this.agentBody.applyImpulse(impulse);
	}

	public void applyForce(Vector2 force) {
		this.agentBody.applyForce(force);
	}

	/*
	 * Returns true if dir is NONE and current velocity in both axes is zero (within epsilon test).
	 * Returns true if dir is not NONE and current velocity is non-zero in the axis direction given by dir.
	 *   Only one axis is checked, e.g. if dir = RIGHT then only the x-axis velocity is checked against epsilon.
	 *   e.g. If dir = LEFT and currentVelocity = (x, y) = (-2, +3) then method returns true because of -2 velocity
	 *        on the x-axis.
	 *   e.g. If dir = DOWN and currentVelocity = (x, y) = (+2, +1) then method returns false because of +1 velocity
	 *        on the y-axis.
	 * Returns false if dir == null.
	 * Throws an exception if epsVelocity < 0 .
	 */
	public boolean hasVelocityInDir(Direction4 dir, float epsVelocity) {
		if(dir == null)
			return false;
		if(epsVelocity < 0) {
			throw new IllegalArgumentException("Cannot check current velocity against negative epsilon velocity: "+
					"epsVelocity="+epsVelocity);
		}
		Vector2 currentVel = agentBody.getVelocity();
		switch(dir) {
			case RIGHT:
				if(currentVel.x > epsVelocity)
					return true;
				break;
			case LEFT:
				if(currentVel.x < -epsVelocity)
					return true;
				break;
			case UP:
				if(currentVel.y > epsVelocity)
					return true;
				break;
			case DOWN:
				if(currentVel.y < -epsVelocity)
					return true;
				break;
			case NONE:
				// use greater-than-or-equals and less-than-or-equals to allow for zero epsVelocity
				if(currentVel.x >= -epsVelocity && currentVel.x <= epsVelocity &&
						currentVel.y >= -epsVelocity && currentVel.y <= epsVelocity)
					return true;
				break;
		}
		return false;
	}

	public void applyImpulseAndCapVel(Direction4 dir, float impulse, float maxVelocity) {
		switch(dir) {
			case RIGHT:
				agentBody.applyImpulse(new Vector2(impulse, 0f));
				if(agentBody.getVelocity().x > maxVelocity)
					agentBody.setVelocity(maxVelocity, agentBody.getVelocity().y);
				break;
			case UP:
				agentBody.applyImpulse(new Vector2(0f, impulse));
				if(agentBody.getVelocity().y > maxVelocity)
					agentBody.setVelocity(agentBody.getVelocity().x, maxVelocity);
				break;
			case LEFT:
				agentBody.applyImpulse(new Vector2(-impulse, 0f));
				if(agentBody.getVelocity().x < -maxVelocity)
					agentBody.setVelocity(-maxVelocity, agentBody.getVelocity().y);
				break;
			case DOWN:
				agentBody.applyImpulse(new Vector2(0f, -impulse));
				if(agentBody.getVelocity().y < -maxVelocity)
					agentBody.setVelocity(agentBody.getVelocity().x, -maxVelocity);
				break;
			case NONE:
				// TODO write words here
				throw new IllegalArgumentException("Cannot apply impulse with direction = NONE.");
		}
	}

	public void applyImpulse(Direction4 dir, float impulse) {
		switch(dir) {
			case RIGHT:
				agentBody.applyImpulse(new Vector2(impulse, 0f));
				break;
			case UP:
				agentBody.applyImpulse(new Vector2(0f, impulse));
				break;
			case LEFT:
				agentBody.applyImpulse(new Vector2(-impulse, 0f));
				break;
			case DOWN:
				agentBody.applyImpulse(new Vector2(0f, -impulse));
				break;
			case NONE:
				// TODO write words here
				throw new IllegalArgumentException("Cannot apply impulse with direction = NONE.");
		}
	}
}
