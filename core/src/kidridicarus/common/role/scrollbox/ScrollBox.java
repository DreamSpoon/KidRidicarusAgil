package kidridicarus.common.role.scrollbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.role.followbox.FollowBox;
import kidridicarus.common.role.followbox.FollowBoxBody;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;

public abstract class ScrollBox extends FollowBox {
	private static final float SHORT_DIM = UInfo.P2M(4f);
	private static final float LONG_DIM = UInfo.P2M(32f);
	private static final float OFFSET = UInfo.P2M(10f);

	public abstract FollowBoxBody createScrollBoxBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds);

	private Direction4 scrollDir;

	public ScrollBox(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		scrollDir = properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE);
		// the position is used, but the bounds width and height will be ignored
		float width;
		float height;
		switch(scrollDir) {
			case RIGHT:
			case LEFT:
				width = SHORT_DIM * UInfo.TILEPIX_X;
				height = LONG_DIM * UInfo.TILEPIX_Y;
				break;
			case UP:
			case DOWN:
				width = LONG_DIM * UInfo.TILEPIX_X;
				height = SHORT_DIM * UInfo.TILEPIX_Y;
				break;
			default:
				throw new IllegalStateException("Cannot create scroll push box with scrollDir = " + scrollDir);
		}
		Vector2 pos = RP_Tool.getCenter(properties);
		body = createScrollBoxBody(this, myPhysHooks, new Rectangle(pos.x, pos.y, width, height));
		myAgentHooks.createAgentRemoveListener(myAgent, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	/*
	 * Get view center, add offset based on scroll direction, set target from offset position.
	 */
	@Override
	public void setTarget(Vector2 position) {
		Vector2 offsetCenter = position.cpy();
		switch(scrollDir) {
			case RIGHT:
				// box is left of center, and moves right
				offsetCenter.x = position.x - OFFSET * UInfo.TILEPIX_X;
				break;
			case UP:
				// box is below center, and moves up
				offsetCenter.y = position.y - OFFSET * UInfo.TILEPIX_Y;
				break;
			case LEFT:
				// box is right of center, and moves left
				offsetCenter.x = position.x + OFFSET * UInfo.TILEPIX_X;
				break;
			case DOWN:
			default:
				// box is above center, and moves down
				offsetCenter.y = position.y + OFFSET * UInfo.TILEPIX_Y;
				break;
		}
		super.setTarget(offsetCenter);
	}

	public void removeSelf() {
		myAgentHooks.removeThisAgent();
	}
}
