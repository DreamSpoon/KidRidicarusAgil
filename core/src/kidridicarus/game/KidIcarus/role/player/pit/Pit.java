package kidridicarus.game.KidIcarus.role.player.pit;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.AgentPropertyListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.optional.PowerupTakeRole;
import kidridicarus.common.role.optional.ScriptableRole;
import kidridicarus.common.role.optional.TakeDamageRole;
import kidridicarus.common.role.player.PlayerRole;
import kidridicarus.common.role.powerup.Powerup;
import kidridicarus.common.role.powerup.PowerupList;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice4x2;
import kidridicarus.game.KidIcarus.KidIcarusKV;
import kidridicarus.game.KidIcarus.role.player.pit.HUD.PitHUD;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.rolescript.ScriptedRoleState;
import kidridicarus.story.tool.RP_Tool;

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
public class Pit extends PlayerRole implements ScriptableRole, PowerupTakeRole, TakeDamageRole {
	private PitHUD playerHUD;
	private PitBrain brain;
	private PitSprite sprite;
	private String nextLevelFilename;

	public Pit(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		this.nextLevelFilename = null;
		PitBody body = new PitBody(myPhysHooks, RP_Tool.getCenter(properties), RP_Tool.safeGetVelocity(properties),
				false);
		this.brain = new PitBrain(this, roleHooks, (PitBody) body,
				properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE).isRight(),
				properties.getInteger(KidIcarusKV.KEY_HEALTH, null),
				properties.getInteger(KidIcarusKV.KEY_HEART_COUNT, null));
		this.sprite = new PitSprite(myGfxHooks.getAtlas(), body.getPosition());
		this.playerHUD = new PitHUD(this, myGfxHooks.getAtlas());
		// update listeners
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { brain.processContactFrame(); }
			});
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		// draw listeners
		myAgentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		myAgentHooks.addDrawListener(CommonInfo.DrawOrder.PLAYER_HUD, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { playerHUD.draw(eye); }
			});
		// custom property listeners
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
	protected Vector2 getPosition() {
		return brain.getPosition();
	}

	@Override
	protected Rectangle getBounds() {
		return brain.getBounds();
	}

	@Override
	protected RoomBox getCurrentRoom() {
		return brain.getCurrentRoom();
	}

	@Override
	public void setFrameMoveAdvice(MoveAdvice4x2 moveAdvice) {
		this.brain.setPlayFrameMove(moveAdvice);
	}

	// set to null to indicate level not "ended", otherwise set to valid String for next level filename
	@Override
	public void setLevelEnded(String nextLevelFilename) {
		this.nextLevelFilename = nextLevelFilename;
	}

	// returns null if level is not "ended", otherwise returns String for next level filename
	@Override
	public String getLevelEnded() {
		return nextLevelFilename;
	}

	@Override
	public boolean isFacingRight() {
		return brain.isFacingRight();
	}

	@Override
	public boolean isGameOver() {
		return brain.isGameOver();
	}

	@Override
	public void scriptSetEnabled(boolean enabled) {
		this.brain.setScriptEnabled(enabled);
	}

	@Override
	public boolean scriptIsEnabled() {
		return brain.isScriptEnabled();
	}

	@Override
	public boolean scriptIsOverrideAllowed() {
		return brain.isScriptOverrideAllowed();
	}

	@Override
	public void scriptSetFrameState(ScriptedRoleState frameState) {
		this.brain.setScriptedFrameState(frameState);
	}

	@Override
	public ScriptedRoleState scriptGetFrameState() {
		return brain.getScriptedRoleState();
	}

	@Override
	public boolean onTakePowerup(Powerup pu) {
		return this.brain.onTakePowerup(pu);
	}

	@Override
	public PowerupList getNonCharPowerupList() {
		return brain.getNonCharPowerupList();
	}

	@Override
	public boolean onTakeDamage(Role otherRole, float dmgAmount, Vector2 dmgOrigin) {
		return this.brain.onTakeDamage();
	}
}
