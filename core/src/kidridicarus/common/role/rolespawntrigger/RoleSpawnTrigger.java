package kidridicarus.common.role.rolespawntrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.followbox.FollowBox;
import kidridicarus.common.role.optional.EnableTakeRole;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.story.RoleHooks;

public class RoleSpawnTrigger extends FollowBox {
	public RoleSpawnTrigger(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		body = new RoleSpawnTriggerBody(this, myPhysHooks, RP_Tool.getBounds(properties));
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					processFrame(((RoleSpawnTriggerBody) body).processFrame());
				}
			});
	}

	private void processFrame(SpawnTriggerFrameInput frameInput) {
		for(EnableTakeRole role : frameInput.beginContacts)
			role.onTakeEnable(true);
		for(EnableTakeRole role : frameInput.endContacts)
			role.onTakeEnable(false);
	}

	public static ObjectProperties makeRP(Vector2 position, float width, float height) {
		return RP_Tool.createRectangleRP(CommonKV.RoleClassAlias.VAL_ROLESPAWN_TRIGGER,
				new Rectangle(position.x - width/2f, position.y - height/2f, width, height));
	}
}
