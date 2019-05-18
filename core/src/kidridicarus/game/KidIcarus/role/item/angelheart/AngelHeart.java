package kidridicarus.game.KidIcarus.role.item.angelheart;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.common.rolespine.BasicRoleSpine;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.game.KidIcarus.KidIcarusKV;
import kidridicarus.game.KidIcarus.role.item.angelheart.AngelHeartBrain.AngelHeartSize;
import kidridicarus.story.RoleHooks;

public class AngelHeart extends CorpusRole {
	private BasicRoleSpine spine;
	private AngelHeartBrain brain;
	private AngelHeartSprite sprite;

	public AngelHeart(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		spine = new BasicRoleSpine(this);
		body = new AngelHeartBody(this, myPhysHooks, RP_Tool.getCenter(properties), spine.createRoleSensor());
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
		myAgentHooks.createAgentRemoveListener(myAgent, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	public static ObjectProperties makeRP(Vector2 position, int heartCount) {
		if(!AngelHeartSize.isValidHeartCount(heartCount))
			throw new IllegalArgumentException("Unable to create Role with heart count = " + heartCount);
		ObjectProperties props = RP_Tool.createPointAP(KidIcarusKV.RoleClassAlias.VAL_ANGEL_HEART, position);
		props.put(KidIcarusKV.KEY_HEART_COUNT, heartCount);
		return props;
	}
}
