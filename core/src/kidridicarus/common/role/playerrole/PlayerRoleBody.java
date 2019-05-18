package kidridicarus.common.role.playerrole;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.story.Role;

public abstract class PlayerRoleBody extends AgentBody {
	private Vector2 prevPosition;
	private Vector2 prevVelocity;

	public PlayerRoleBody(Role parentRole, PhysicsHooks physHooks, Vector2 position, Vector2 velocity) {
		super(parentRole.getAgent(), physHooks);
		prevPosition = position.cpy();
		prevVelocity = velocity.cpy();
	}

	public void resetPrevValues() {
		prevPosition.set(b2body.getPosition());
		prevVelocity.set(b2body.getLinearVelocity());
	}

	public Vector2 getPrevPosition() {
		return prevPosition;
	}

	public Vector2 getPrevVelocity() {
		return prevVelocity;
	}
}
