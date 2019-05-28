package kidridicarus.game.KidIcarus.role.item.angelheart;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.common.rolespine.BasicRoleSpine;
import kidridicarus.game.KidIcarus.KidIcarusKV;
import kidridicarus.game.KidIcarus.role.item.angelheart.AngelHeartBrain.AngelHeartSize;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class AngelHeart extends CorpusRole {
	private BasicRoleSpine spine;
	private AngelHeartBrain brain;
	private AngelHeartSprite sprite;

	public AngelHeart(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		spine = new BasicRoleSpine(this);
		body = new AngelHeartBody(myPhysHooks, RP_Tool.getCenter(properties), spine.createRoleSensor());
		spine.setBody(body);
		brain = new AngelHeartBrain(roleHooks, spine, properties.get(KidIcarusKV.KEY_HEART_COUNT, 1, Integer.class));
		sprite = new AngelHeartSprite(myGfxHooks.getAtlas(), RP_Tool.getCenter(properties), brain.getHeartSize());
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame();
				}
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
		myAgentHooks.createInternalRemovalListener(new AgentRemovalCallback() {
				@Override
				public void preAgentRemoval() { dispose(); }
				@Override
				public void postAgentRemoval() {}
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
