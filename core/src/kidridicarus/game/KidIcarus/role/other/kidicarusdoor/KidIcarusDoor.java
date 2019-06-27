package kidridicarus.game.KidIcarus.role.other.kidicarusdoor;

import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.optional.SolidRole;
import kidridicarus.common.role.optional.TriggerTakeRole;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

// note: door is solid when closed; non-solid when open
public class KidIcarusDoor extends Role implements TriggerTakeRole, SolidRole {
	private KidIcarusDoorBrain brain;
	private KidIcarusDoorSprite sprite;

	public KidIcarusDoor(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		// default to open state unless Role is supposed to "expire immediately" (a closed door is a dead door?)
		boolean isOpened = !properties.getBoolean(CommonKV.Spawn.KEY_EXPIRE, false);
		KidIcarusDoorBody body = new KidIcarusDoorBody(myPhysHooks, RP_Tool.getCenter(properties), isOpened);
		brain = new KidIcarusDoorBrain(this, roleHooks, body, isOpened,
				RP_Tool.getTargetName(properties));
		sprite = new KidIcarusDoorSprite(myGfxHooks.getAtlas(), body.getPosition(), isOpened);
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { brain.processContactFrame(); }
			});
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame()); }
			});
		myAgentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	@Override
	public void onTakeTrigger() {
		brain.onTakeTrigger();
	}
}
