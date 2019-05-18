package kidridicarus.game.KidIcarus.role.player.pit;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.role.optional.ContactDmgTakeRole;
import kidridicarus.common.role.optional.PowerupTakeRole;
import kidridicarus.common.role.playerrole.PlayerRole;
import kidridicarus.common.role.playerrole.PlayerRoleBody;
import kidridicarus.common.role.playerrole.PlayerRoleSupervisor;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.game.KidIcarus.KidIcarusKV;
import kidridicarus.game.KidIcarus.role.player.pit.HUD.PitHUD;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;

/*
 * Notes:
 * Upon receiving damage contact, pit immediately moves 4 pixels to the left, with no change in velocity.
 *   (double check 4 pixels)
 * 
 * Pit faces these directions under these conditions:
 *   -if not aiming up
 *     -faces right when moving right (or if stopped and advised move right)
 *     -faces left when moving left (or if stopped and advised move left)
 *   -otherwise, aiming up:
 *     -if move right/left advice is given then use move advice to determine facing direction
 *     -otherwise retain previous facing direction
 * Glitches implemented:
 *   -duck, unduck re-shoot - if pit shoots, then quickly ducks and unducks, he can shoot more often than normal
 */
public class Pit extends PlayerRole implements PowerupTakeRole, ContactDmgTakeRole {
	private PitHUD playerHUD;
	private PitSpine spine;
	private PitBrain brain;
	private PitSprite sprite;

	public Pit(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		spine = new PitSpine(this);
		body = new PitBody(this, myPhysHooks, RP_Tool.getCenter(properties),
				RP_Tool.safeGetVelocity(properties), false, spine.createSolidContactSensor(),
				spine.createRoleSensor());
		spine.setBody(body);
		brain = new PitBrain(this, roleHooks, spine,
				properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE).isRight(),
				properties.getInteger(KidIcarusKV.KEY_HEALTH, null),
				properties.getInteger(KidIcarusKV.KEY_HEART_COUNT, null));
		sprite = new PitSprite(myGfxHooks.getAtlas(), body.getPosition());
		playerHUD = new PitHUD(this, myGfxHooks.getAtlas());
		createPropertyListeners();
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { brain.processContactFrame(); }
			});
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					sprite.processFrame(brain.processFrame(frameTime));
					((PlayerRoleBody) body).resetPrevValues();
				}
			});
		myAgentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		myAgentHooks.addDrawListener(CommonInfo.DrawOrder.PLAYER_HUD, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { playerHUD.draw(eye); }
			});
		myAgentHooks.createAgentRemoveListener(myAgent, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	private void createPropertyListeners() {
		myAgentHooks.addPropertyListener(false, CommonKV.Script.KEY_SPRITE_SIZE,
				new AgentPropertyListener<Vector2>(Vector2.class) {
				@Override
				public Vector2 getValue() { return new Vector2(sprite.getWidth(), sprite.getHeight()); }
			});
		myAgentHooks.addPropertyListener(false, CommonKV.KEY_DIRECTION,
				new AgentPropertyListener<Direction4>(Direction4.class) {
				@Override
				public Direction4 getValue() { return brain.isFacingRight() ? Direction4.RIGHT : Direction4.LEFT; }
			});
		myAgentHooks.addPropertyListener(false, KidIcarusKV.KEY_HEALTH,
				new AgentPropertyListener<Integer>(Integer.class) {
				@Override
				public Integer getValue() { return brain.getHealth(); }
			});
		myAgentHooks.addPropertyListener(false, KidIcarusKV.KEY_HEART_COUNT,
				new AgentPropertyListener<Integer>(Integer.class) {
				@Override
				public Integer getValue() { return brain.getHeartsCollected(); }
			});
	}

	@Override
	public boolean onTakePowerup(Powerup pu) {
		return brain.onTakePowerup(pu);
	}

	@Override
	public boolean onTakeDamage(Role role, float amount, Vector2 dmgOrigin) {
		return brain.onTakeDamage();
	}

	@Override
	public PlayerRoleSupervisor getSupervisor() {
		return brain.getSupervisor();
	}

	@Override
	public RoomBox getCurrentRoom() {
		return brain.getCurrentRoom();
	}

	@Override
	protected Vector2 getPosition() {
		return brain.getPosition();
	}
}
