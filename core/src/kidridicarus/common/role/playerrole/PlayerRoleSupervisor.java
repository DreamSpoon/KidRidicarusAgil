package kidridicarus.common.role.playerrole;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.CommonKV;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.powerup.PowerupList;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice4x2;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.rolescript.RoleSupervisor;
import kidridicarus.story.rolescript.ScriptedRoleState;
import kidridicarus.story.rolescript.RoleScript.RoleScriptHooks;
import kidridicarus.story.rolescript.ScriptedSpriteState.SpriteState;
import kidridicarus.story.tool.RP_Tool;

public class PlayerRoleSupervisor extends RoleSupervisor {
	private RoomBox currentRoom;
	private PowerupList nonCharPowerups;
	private Vector2 lastKnownViewCenter;
	private MoveAdvice4x2 moveAdvice;
	private String nextLevelName;
	private boolean isGameOver;

	public PlayerRoleSupervisor(Role supervisedRole, RoleHooks supervisedRoleHooks) {
		super(supervisedRole, supervisedRoleHooks);
		currentRoom = null;
		nonCharPowerups = new PowerupList();
		lastKnownViewCenter = null;
		moveAdvice = new MoveAdvice4x2();
		nextLevelName = null;
		isGameOver = false;
	}

	@Override
	public void postUpdateAgency() {
		super.postUpdateAgency();

		// check if player changed room, and if so, did the room music change?
		RoomBox nextRoom = ((PlayerRole) supervisedRole).getCurrentRoom();
		if(currentRoom != nextRoom) {
			roomChange(nextRoom);
			currentRoom = nextRoom;
		}

		// if current room is known then try to set last known view center
		RoomBox room = ((PlayerRole) supervisedRole).getCurrentRoom();
		Vector2 pos = RP_Tool.getCenter(supervisedRole);
		if(room != null && pos != null)
			lastKnownViewCenter = room.getViewCenterForPos(pos, lastKnownViewCenter);
	}

	private void roomChange(RoomBox newRoom) {
		if(newRoom != null) {
			String strMusic = newRoom.getAgent().getProperty(CommonKV.Room.KEY_MUSIC, null, String.class);
			if(strMusic != null)
				supervisedRoleHooks.agentHooksBundle.audioHooks.getEar().changeAndStartMainMusic(strMusic);
		}
		// Reset the view center, so that view does not "over-scroll" if player is teleported in a one way
		// scrolling room (e.g. using doors in Kid Icarus level 1-1).
		lastKnownViewCenter = null;
	}

	public Vector2 getViewCenter() {
		RoomBox room = ((PlayerRole) supervisedRole).getCurrentRoom();
		if(room == null)
			return null;
		return room.getViewCenterForPos(RP_Tool.getCenter(supervisedRole), lastKnownViewCenter);
	}

	public void receiveNonCharPowerup(Powerup pow) {
		nonCharPowerups.add(pow);
	}

	public PowerupList getNonCharPowerups() {
		return nonCharPowerups;
	}

	public void clearNonCharPowerups() {
		nonCharPowerups.clear();
	}

	@Override
	public void setMoveAdvice(MoveAdvice4x2 moveAdvice) {
		this.moveAdvice.set(moveAdvice);
	}

	@Override
	protected MoveAdvice4x2 internalPollMoveAdvice() {
		MoveAdvice4x2 adv = moveAdvice.cpy();
		moveAdvice.clear();
		return adv;
	}

	@Override
	protected ScriptedRoleState getCurrentScriptRoleState() {
		// throw exception if player doesn't have position
		Vector2 rolePos = RP_Tool.getCenter(supervisedRole);
		if(rolePos == null)
			throw new IllegalStateException("Player Role position cannot be null while getting state for script.");
		// get body state
		ScriptedRoleState curState = new ScriptedRoleState();
		curState.scriptedBodyState.contactEnabled = true;
		curState.scriptedBodyState.position.set(rolePos);
		// get sprite state
		curState.scriptedSpriteState.visible = true;
		curState.scriptedSpriteState.position.set(rolePos);
		curState.scriptedSpriteState.spriteState = supervisedRole.getAgent().getProperty(
				CommonKV.Script.KEY_SPRITE_STATE, SpriteState.STAND, SpriteState.class);
		if(supervisedRole.getAgent().getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE,Direction4.class).isRight())
			curState.scriptedSpriteState.isFacingRight = true;
		else
			curState.scriptedSpriteState.isFacingRight = false;

		return curState;
	}

	@Override
	protected RoleScriptHooks getRoleScriptHooks() {
		return new RoleScriptHooks() {
				@Override
				public void gotoNextLevel(String name) {
					nextLevelName = name;
				}
			};
	}

	@Override
	public String getNextLevelFilename() {
		return nextLevelName;
	}

	@Override
	public boolean isAtLevelEnd() {
		return nextLevelName != null;
	}

	public void setGameOver() {
		isGameOver = true;
	}

	@Override
	public boolean isGameOver() {
		return isGameOver;
	}
}
