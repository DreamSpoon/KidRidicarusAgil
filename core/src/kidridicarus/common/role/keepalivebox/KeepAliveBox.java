package kidridicarus.common.role.keepalivebox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.followbox.FollowBox;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.story.RoleHooks;

public class KeepAliveBox extends FollowBox {
	public KeepAliveBox(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		body = new KeepAliveBoxBody(this, myPhysHooks, RP_Tool.getBounds(properties));
	}

	public static ObjectProperties makeRP(Vector2 position, float width, float height) {
		return RP_Tool.createRectangleRP(CommonKV.RoleClassAlias.VAL_KEEPALIVE_BOX,
				new Rectangle(position.x - width/2f, position.y - height/2f, width, height));
	}
}
