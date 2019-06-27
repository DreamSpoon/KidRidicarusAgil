package kidridicarus.game.KidIcarus.role.other.vanishpoof;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.KidIcarus.KidIcarusKV;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class VanishPoof extends Role {
	private static final float POOF_TIME = 2/5f;

	private Vector2 position;
	private VanishPoofSprite sprite;
	private float stateTimer;

	public VanishPoof(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		this.position = RP_Tool.getCenter(properties);
		stateTimer = 0f;
		sprite = new VanishPoofSprite(myGfxHooks.getAtlas(), position,
				properties.getBoolean(KidIcarusKV.KEY_IS_BIG, false));
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(doUpdate(frameTime)); }
			});
		myAgentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	private SpriteFrameInput doUpdate(FrameTime frameTime) {
		stateTimer += frameTime.timeDelta;
		if(stateTimer > POOF_TIME) {
			myAgentHooks.removeThisAgent();
			return null;
		}
		return SprFrameTool.placeAnim(position, frameTime);
	}

	public static ObjectProperties makeRP(Vector2 position, boolean isBig) {
		ObjectProperties props = RP_Tool.createPointRP(KidIcarusKV.RoleClassAlias.VAL_VANISH_POOF, position);
		props.put(KidIcarusKV.KEY_IS_BIG, isBig);
		return props;
	}
}
