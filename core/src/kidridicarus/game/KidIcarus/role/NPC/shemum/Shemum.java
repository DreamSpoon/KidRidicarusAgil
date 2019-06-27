package kidridicarus.game.KidIcarus.role.NPC.shemum;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.role.optional.TakeDamageRole;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class Shemum extends Role implements TakeDamageRole {
	private ShemumBrain brain;

	public Shemum(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		ShemumBody body = new ShemumBody(myPhysHooks, RP_Tool.getCenter(properties),
				RP_Tool.safeGetVelocity(properties));
		brain = new ShemumBrain(this, roleHooks, (ShemumBody) body);
		final ShemumSprite sprite = new ShemumSprite(myGfxHooks.getAtlas(), body.getPosition());
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { brain.processContactFrame(); }
			});
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		myAgentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	@Override
	public boolean onTakeDamage(Role otherRole, float dmgAmount, Vector2 dmgOrigin) {
		return brain.onTakeDamage(otherRole);
	}
}
