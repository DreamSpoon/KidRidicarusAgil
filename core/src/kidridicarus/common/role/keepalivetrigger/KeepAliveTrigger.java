package kidridicarus.common.role.keepalivetrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.followbox.FollowBox;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class KeepAliveTrigger extends FollowBox {
	public KeepAliveTrigger(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		body = new KeepAliveTriggerBody(myPhysHooks, RP_Tool.getBounds(properties));
	}

	public static ObjectProperties makeRP(Vector2 position, float width, float height) {
		return RP_Tool.createRectangleRP(CommonKV.RoleClassAlias.VAL_KEEPALIVE_BOX,
				new Rectangle(position.x - width/2f, position.y - height/2f, width, height));
	}
}
