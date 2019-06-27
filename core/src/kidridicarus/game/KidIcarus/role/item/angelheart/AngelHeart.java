package kidridicarus.game.KidIcarus.role.item.angelheart;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.role.powerup.PowerupBody;
import kidridicarus.game.KidIcarus.KidIcarusKV;
import kidridicarus.game.KidIcarus.role.item.angelheart.AngelHeartBrain.AngelHeartSize;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class AngelHeart extends Role {
	private static final Vector2 HITBOX_SIZE = UInfo.VectorP2M(3f, 3f);

	private AngelHeartBrain brain;
	private AngelHeartSprite sprite;

	public AngelHeart(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		PowerupBody body = new PowerupBody(myPhysHooks, RP_Tool.getCenter(properties), HITBOX_SIZE);
		brain = new AngelHeartBrain(roleHooks, (PowerupBody) body,
				properties.get(KidIcarusKV.KEY_HEART_COUNT, 1, Integer.class));
		sprite = new AngelHeartSprite(myGfxHooks.getAtlas(), RP_Tool.getCenter(properties), brain.getHeartSize());
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { brain.processContactFrame(); }
			});
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					sprite.processFrame(brain.processFrame(frameTime.timeDelta));
				}
			});
		myAgentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	public static ObjectProperties makeRP(Vector2 position, int heartCount) {
		if(!AngelHeartSize.isValidHeartCount(heartCount))
			throw new IllegalArgumentException("Unable to create Role with heart count = " + heartCount);
		ObjectProperties props = RP_Tool.createPointRP(KidIcarusKV.RoleClassAlias.VAL_ANGEL_HEART, position);
		props.put(KidIcarusKV.KEY_HEART_COUNT, heartCount);
		return props;
	}
}
