package kidridicarus.common.role.playerrole;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.story.rolebody.RoleBody;

public abstract class PlayerRoleBody extends RoleBody {
	private Vector2 prevPosition;
	private Vector2 prevVelocity;

	public PlayerRoleBody(PhysicsHooks physHooks, Vector2 position, Vector2 velocity) {
		super(physHooks);
		prevPosition = position.cpy();
		prevVelocity = velocity.cpy();
	}

	public void resetPrevValues() {
		prevPosition.set(agentBody.getPosition());
		prevVelocity.set(agentBody.getVelocity());
	}

	public Vector2 getPrevPosition() {
		return prevPosition;
	}

	public Vector2 getPrevVelocity() {
		return prevVelocity;
	}
}
