package kidridicarus.common.role.scrollpushbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.followbox.FollowBoxBody;
import kidridicarus.common.role.scrollbox.ScrollBox;
import kidridicarus.common.tool.Direction4;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

// this class does not implement DisposableAgent because this is a sub-Agent related to player Agents
public class ScrollPushBox extends ScrollBox {
	public ScrollPushBox(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
	}

	@Override
	protected FollowBoxBody createScrollBoxBody(PhysicsHooks physHooks, Rectangle bounds) {
		return new ScrollPushBoxBody(physHooks, bounds);
	}

	public static ObjectProperties makeRP(Vector2 position, Direction4 scrollDir) {
		ObjectProperties rp = RP_Tool.createPointRP(CommonKV.RoleClassAlias.VAL_SCROLL_PUSHBOX, position);
		rp.put(CommonKV.KEY_DIRECTION, scrollDir);
		return rp;
	}
}
