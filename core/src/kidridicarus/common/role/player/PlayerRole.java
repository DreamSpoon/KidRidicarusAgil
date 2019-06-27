package kidridicarus.common.role.player;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentPropertyListener;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.tool.MoveAdvice4x2;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;

public abstract class PlayerRole extends Role {
	public abstract void setFrameMoveAdvice(MoveAdvice4x2 moveAdvice);
	public abstract void setLevelEnded(String nextLevelFilename);
	public abstract String getLevelEnded();
	public abstract boolean isFacingRight();
	public abstract boolean isGameOver();

	protected abstract Vector2 getPosition();
	protected abstract Rectangle getBounds();
	protected abstract RoomBox getCurrentRoom();

	public PlayerRole(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		myAgentHooks.addPropertyListener(false, CommonKV.KEY_POSITION,
				new AgentPropertyListener<Vector2>(Vector2.class) {
				@Override
				public Vector2 getValue() { return getPosition(); }
			});
		myAgentHooks.addPropertyListener(false, CommonKV.KEY_BOUNDS,
				new AgentPropertyListener<Rectangle>(Rectangle.class) {
				@Override
				public Rectangle getValue() { return getBounds(); }
			});
		myAgentHooks.addPropertyListener(false, CommonKV.KEY_CURRENT_ROOM,
				new AgentPropertyListener<RoomBox>(RoomBox.class) {
				@Override
				public RoomBox getValue() { return getCurrentRoom(); }
			});
	}

	public void removeSelf() {
		myAgentHooks.removeThisAgent();
	}
}
