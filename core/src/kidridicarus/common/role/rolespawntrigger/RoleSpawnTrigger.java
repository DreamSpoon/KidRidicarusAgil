package kidridicarus.common.role.rolespawntrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.followbox.FollowBox;
import kidridicarus.common.role.optional.EnableTakeRole;
import kidridicarus.common.rolesensor.OneWayContactSensor;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class RoleSpawnTrigger extends FollowBox {
	private OneWayContactSensor beginContactSensor;
	private OneWayContactSensor endContactSensor;

	public RoleSpawnTrigger(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		beginContactSensor = new OneWayContactSensor(this, true);
		endContactSensor = new OneWayContactSensor(this, false);
		beginContactSensor.chainTo(endContactSensor);
		body = new RoleSpawnTriggerBody(myPhysHooks, RP_Tool.getBounds(properties), beginContactSensor);
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					for(EnableTakeRole role : beginContactSensor.getOnlyAndResetContacts(EnableTakeRole.class))
						role.onTakeEnable(true);
					for(EnableTakeRole role : endContactSensor.getOnlyAndResetContacts(EnableTakeRole.class))
						role.onTakeEnable(false);
				}
			});
	}

	public static ObjectProperties makeRP(Vector2 position, float width, float height) {
		return RP_Tool.createRectangleRP(CommonKV.RoleClassAlias.VAL_ROLESPAWN_TRIGGER,
				new Rectangle(position.x - width/2f, position.y - height/2f, width, height));
	}
}
