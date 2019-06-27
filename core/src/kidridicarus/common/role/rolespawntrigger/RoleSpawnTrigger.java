package kidridicarus.common.role.rolespawntrigger;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.followbox.FollowBox;
import kidridicarus.common.role.optional.EnableTakeRole;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class RoleSpawnTrigger extends FollowBox {
	private Collection<EnableTakeRole> enabledRoles;

	public RoleSpawnTrigger(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		this.body = new RoleSpawnTriggerBody(myPhysHooks, RP_Tool.getBounds(properties));
		this.enabledRoles = new LinkedList<EnableTakeRole>();
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { doUpdate(); }
			});
	}

	private void doUpdate() {
		// check for roles that need to be enabled (begin contacts)
		for(EnableTakeRole role : ((RoleSpawnTriggerBody) body).getEnableTakeBeginContacts()) {
			enabledRoles.add(role);
			role.onTakeEnable(true);
		}
		// check for roles that need to be disabled (end contacts)
		for(EnableTakeRole role : enabledRoles) {
			if(!((RoleSpawnTriggerBody) body).isContactingRole(role)) {
				enabledRoles.remove(role);
				role.onTakeEnable(false);
			}
		}
	}

	public static ObjectProperties makeRP(Vector2 position, float width, float height) {
		return RP_Tool.createRectangleRP(CommonKV.RoleClassAlias.VAL_ROLESPAWN_TRIGGER,
				new Rectangle(position.x - width/2f, position.y - height/2f, width, height));
	}
}
