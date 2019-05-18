package kidridicarus.game.KidIcarus.role.NPC.shemum;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.common.role.optional.ContactDmgTakeRole;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;

public class Shemum extends CorpusRole implements ContactDmgTakeRole {
	private ShemumSpine spine;
	private ShemumBrain brain;
	private ShemumSprite sprite;

	public Shemum(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		spine = new ShemumSpine(this);
		body = new ShemumBody(this, myPhysHooks, RP_Tool.getCenter(properties),
				RP_Tool.safeGetVelocity(properties), spine.createSolidContactSensor(), spine.createRoleSensor(),
				spine.createPlayerSensor());
		spine.setBody(body);
		brain = new ShemumBrain(this, roleHooks, spine);
		sprite = new ShemumSprite(myGfxHooks.getAtlas(), body.getPosition());
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { brain.processContactFrame(); }
			});
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					sprite.processFrame(brain.processFrame(frameTime));
				}
			});
		myAgentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		myAgentHooks.createAgentRemoveListener(myAgent, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	@Override
	public boolean onTakeDamage(Role role, float amount, Vector2 dmgOrigin) {
		return brain.onTakeDamage(role);
	}
}
