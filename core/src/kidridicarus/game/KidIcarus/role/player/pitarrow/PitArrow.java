package kidridicarus.game.KidIcarus.role.player.pitarrow;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.common.rolespine.SolidContactSpine;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.KidIcarusKV;
import kidridicarus.game.KidIcarus.role.player.pit.Pit;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class PitArrow extends CorpusRole {
	private SolidContactSpine spine;
	private PitArrowBrain brain;
	private PitArrowSprite sprite;

	public PitArrow(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		Direction4 arrowDir = properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE);
		spine = new SolidContactSpine(this);
		body = new PitArrowBody(myPhysHooks, RP_Tool.getCenter(properties),
				RP_Tool.safeGetVelocity(properties), arrowDir, spine.createSolidContactSensor(),
				spine.createRoleSensor());
		spine.setBody(body);
		brain = new PitArrowBrain(properties.get(CommonKV.KEY_PARENT_ROLE, null, Pit.class), myAgentHooks,
				spine, properties.getBoolean(CommonKV.Spawn.KEY_EXPIRE, false), arrowDir);
		sprite = new PitArrowSprite(myGfxHooks.getAtlas(), new PitArrowSpriteFrameInput(body.getPosition(), arrowDir));
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
		myAgentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
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

	public static ObjectProperties makeRP(Pit parentAgent, Vector2 position, Vector2 velocity, Direction4 arrowDir,
			boolean isExpireImmediately) {
		ObjectProperties props = RP_Tool.createPointRP(KidIcarusKV.RoleClassAlias.VAL_PIT_ARROW, position, velocity);
		props.put(CommonKV.KEY_PARENT_ROLE, parentAgent);
		props.put(CommonKV.KEY_DIRECTION, arrowDir);
		props.put(CommonKV.Spawn.KEY_EXPIRE, isExpireImmediately);
		return props;
	}
}
