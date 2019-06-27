package kidridicarus.common.role.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent;
import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.KeyboardMapping;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.role.keepalivetrigger.KeepAliveTrigger;
import kidridicarus.common.role.optional.PowerupTakeRole;
import kidridicarus.common.role.playerspawner.PlayerSpawner;
import kidridicarus.common.role.powerup.PowChar;
import kidridicarus.common.role.powerup.Powerup;
import kidridicarus.common.role.rolespawntrigger.RoleSpawnTrigger;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.role.scrollbox.ScrollBox;
import kidridicarus.common.role.scrollkillbox.ScrollKillBox;
import kidridicarus.common.role.scrollpushbox.ScrollPushBox;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice4x2;
import kidridicarus.common.tool.QQ;
import kidridicarus.game.KidIcarus.KidIcarusPow;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.info.StoryKV;
import kidridicarus.story.tool.RP_Tool;

public class PlayerControllerRole extends Role {
	private static final Vector2 SPAWN_TRIGGER_SIZE = UInfo.VectorP2M(UInfo.TILEPIX_X * 20, UInfo.TILEPIX_Y * 15);
	private static final Vector2 KEEP_ALIVE_TRIGGER_SIZE =
			UInfo.VectorP2M(UInfo.TILEPIX_X * 22, UInfo.TILEPIX_Y * 15);
	/*
	 * TODO Replace use of safety spawn dist - created because change from small Mario to Samus would sometimes push
	 * new Samus body out of bounds - with a check of nearby space for an empty spot to use as safe spawn position.
	 */
	private static final Vector2 SAFETY_RESPAWN_OFFSET = UInfo.VectorP2M(0f, 8f);

	private PlayerRole playerRole;
	private RoleSpawnTrigger spawnTrigger;
	private KeepAliveTrigger keepAliveBox;
	// scrollBox can be a "push box", like in Super Mario Bros 1 "ratchet scrolling", or it can be a "kill box" like
	// in Kid Icarus 1 "bottom-of-screen kill box".
	private ScrollBox scrollBox;
	private RoomBox currentRoom;
	private Vector2 bestViewCenter;

	public PlayerControllerRole(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);

		this.currentRoom = null;
		this.bestViewCenter = new Vector2(0f, 0f);

		// create the PlayerRole that this wrapper will control
		ObjectProperties playerRoleProperties =
				properties.get(CommonKV.Player.KEY_ROLE_PROPERTIES, null, ObjectProperties.class);
		createPlayerRole(playerRoleProperties);

		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.PLAY_CONTROL_PREUPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { doPreUpdate(); }
			});
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.PLAY_CONTROL_POSTUPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { doPostUpdate(); }
			});
		myAgentHooks.addDrawListener(CommonInfo.DrawOrder.UPDATE_CAMERA, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { updateCamera(); }
			});
	}

	private void createPlayerRole(ObjectProperties playerRoleProperties) {
		// find main player spawner and return fail if none found
		PlayerSpawner spawner = getMainPlayerSpawner();
		if(spawner == null)
			throw new IllegalStateException("Cannot spawn player, main player spawner not found.");

		// spawn player with properties at spawn location
		playerRole = spawnPlayerRoleWithProperties(playerRoleProperties, spawner);
		// create player's associated Roles (generally, they follow player)
		spawnTrigger = (RoleSpawnTrigger) myStoryHooks.createRole(
				RoleSpawnTrigger.makeRP(bestViewCenter, SPAWN_TRIGGER_SIZE.x, SPAWN_TRIGGER_SIZE.y));
		keepAliveBox = (KeepAliveTrigger) myStoryHooks.createRole(
				KeepAliveTrigger.makeRP(bestViewCenter, KEEP_ALIVE_TRIGGER_SIZE.x, KEEP_ALIVE_TRIGGER_SIZE.y));

		// if this PlayerControllerRole is removed, then it's sub-roles must be removed, and removed first
		myAgentHooks.createAgentRemovalRequirement(playerRole.getAgent(), false);
		myAgentHooks.createAgentRemovalRequirement(spawnTrigger.getAgent(), false);
		myAgentHooks.createAgentRemovalRequirement(keepAliveBox.getAgent(), false);
		myAgentHooks.createAgentRemovalOrder(playerRole.getAgent(), false);
		myAgentHooks.createAgentRemovalOrder(spawnTrigger.getAgent(), false);
		myAgentHooks.createAgentRemovalOrder(keepAliveBox.getAgent(), false);
	}

	// get user input
	private void doPreUpdate() {
		if(playerRole == null)
			return;
		// ensure spawn trigger and keep alive box follow view center
		spawnTrigger.setTarget(bestViewCenter);
		keepAliveBox.setTarget(bestViewCenter);
		if(scrollBox != null)
			scrollBox.setTarget(bestViewCenter);
		handleInput();
	}

	private void handleInput() {
		if(playerRole == null)
			return;

		if(Gdx.input.isKeyJustPressed(KeyboardMapping.DEBUG_TOGGLE))
			QQ.toggleOn();
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP_PIT))
			Powerup.tryPushPowerup(playerRole, new KidIcarusPow.AngelHeartPow(5));
		MoveAdvice4x2 inputMoveAdvice = new MoveAdvice4x2();
		inputMoveAdvice.moveRight = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RIGHT);
		inputMoveAdvice.moveUp = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_UP);
		inputMoveAdvice.moveLeft = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_LEFT);
		inputMoveAdvice.moveDown = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_DOWN);
		inputMoveAdvice.action0 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUNSHOOT);
		inputMoveAdvice.action1 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP);
		playerRole.setFrameMoveAdvice(inputMoveAdvice);
	}

	private void doPostUpdate() {
		if(playerRole == null)
			return;

		// check for "out-of-character" powerup received and change to appropriate character for powerup
		Powerup nonCharPowerup = ((PowerupTakeRole) playerRole).getNonCharPowerupList().getFirst();
		if(nonCharPowerup != null)
			switchRoleType(nonCharPowerup.getPowerupCharacter());

		// check if player changed room, and if so, did the room music change?
		RoomBox nextRoom = RP_Tool.getCurrentRoom(playerRole);
		if(currentRoom != nextRoom) {
			if(nextRoom != null) {
				String strMusic = nextRoom.getAgent().getProperty(CommonKV.Room.KEY_MUSIC, null, String.class);
				if(strMusic != null)
					myAudioHooks.getEar().changeAndStartMainMusic(strMusic);
			}
			currentRoom = nextRoom;
		}

		// if current room is known then try to set last known view center
		Vector2 pos = RP_Tool.getCenter(playerRole);
		if(currentRoom != null && pos != null)
			bestViewCenter.set(currentRoom.getViewCenterForPos(pos, bestViewCenter));

		checkCreateScrollBox();
	}

	// as the player moves into and out of rooms, the scroll box may need to be created/modified/removed
	private void checkCreateScrollBox() {
		if(currentRoom == null)
			return;
		Direction4 scrollDir =
				currentRoom.getAgent().getProperty(CommonKV.Room.KEY_SCROLL_DIR, Direction4.NONE, Direction4.class);
		// if current room has scroll push box property = true then create/change to scroll push box
		if(currentRoom.getAgent().getProperty(CommonKV.Room.KEY_SCROLL_PUSHBOX, false, Boolean.class)) {
			if(scrollBox != null && !(scrollBox instanceof ScrollPushBox)) {
				scrollBox.removeSelf();
				scrollBox = null;
			}
			// if scroll box needs to be created and a valid scroll direction is given then create push box
			if(scrollBox == null && scrollDir != Direction4.NONE) {
				scrollBox = (ScrollPushBox) myStoryHooks.createRole(ScrollPushBox.makeRP(bestViewCenter, scrollDir));
				// if this PlayerControllerRole is removed, then it's sub-roles must be removed, and removed first
				myAgentHooks.createAgentRemovalRequirement(scrollBox.getAgent(), false);
				myAgentHooks.createAgentRemovalOrder(scrollBox.getAgent(), false);
			}
		}
		// if current room has scroll kill box property = true then create/change to scroll kill box
		else if(currentRoom.getAgent().getProperty(CommonKV.Room.KEY_SCROLL_KILLBOX, false, Boolean.class)) {
			if(scrollBox != null && !(scrollBox instanceof ScrollKillBox)) {
				scrollBox.removeSelf();
				scrollBox = null;
			}
			// if scroll box needs to be created and a valid scroll direction is given then create kill box
			if(scrollBox == null && scrollDir != Direction4.NONE) {
				scrollBox = (ScrollKillBox) myStoryHooks.createRole(ScrollKillBox.makeRP(bestViewCenter, scrollDir));
				// if this PlayerControllerRole is removed, then it's sub-roles must be removed, and removed first
				myAgentHooks.createAgentRemovalRequirement(scrollBox.getAgent(), false);
				myAgentHooks.createAgentRemovalOrder(scrollBox.getAgent(), false);
			}
		}
		// need to remove a scroll box?
		else if(scrollBox != null) {
			scrollBox.removeSelf();
			scrollBox = null;
		}
	}

	private void switchRoleType(PowChar pc) {
		// if power character class alias is blank then throw exception
		if(pc.getRoleClassAlias().equals(""))
			throw new IllegalArgumentException("Cannot create player Role from blank class alias.");
		// if player Role is null or doesn't have position then throw exception
		if(playerRole == null)
			throw new IllegalStateException("Current player Role cannot be null when switching power character.");

		// save copy of position
		Vector2 oldPosition = RP_Tool.getCenter(playerRole);
		if(oldPosition == null) {
			throw new IllegalStateException(
					"Current player Role must have a position when switching power character.");
		}
		// save copy of facing right
		boolean facingRight = RP_Tool.safeGetDirection4(playerRole).isRight();
		// save copy of velocity
		Vector2 oldVelocity = RP_Tool.getVelocity(playerRole);
		// remove old player character
		playerRole.removeSelf();
		playerRole = null;
		// create new player character properties
		ObjectProperties props = RP_Tool.createPointRP(pc.getRoleClassAlias(),
				oldPosition.cpy().add(SAFETY_RESPAWN_OFFSET));
		// put facing right property if needed
		if(facingRight)
			props.put(CommonKV.KEY_DIRECTION, Direction4.RIGHT);
		// put velocity if available
		if(oldVelocity != null)
			props.put(CommonKV.KEY_VELOCITY, oldVelocity);
		// create new player power character Role
		playerRole = (PlayerRole) myStoryHooks.createRole(props);
	}

	private void updateCamera() {
		if(playerRole == null)
			return;
		// if player is not dead then use their current room to determine the gamecam position
		if(!playerRole.isGameOver())
			myGfxHooks.getEye().setViewCenter(bestViewCenter);
	}

	private PlayerSpawner getMainPlayerSpawner() {
		// find main spawnpoint and spawn player there, or spawn at (0, 0) if no spawnpoint found
		Agent spawner = myAgentHooks.getFirstAgentByProperties(
				new String[] { StoryKV.KEY_ROLE_CLASS, CommonKV.Spawn.KEY_SPAWN_MAIN },
				new Object[] { CommonKV.RoleClassAlias.VAL_PLAYER_SPAWNER, true });
		if(spawner == null || !(spawner.getUserData() instanceof PlayerSpawner))
			return null;
		return (PlayerSpawner) spawner.getUserData();
	}

	private PlayerRole spawnPlayerRoleWithProperties(ObjectProperties playerRoleProperties,
			PlayerSpawner spawner) {
		// if no spawn position then return null
		Vector2 spawnPos = RP_Tool.getCenter(spawner);
		if(spawnPos == null)
			throw new IllegalStateException("Cannot spawn player role with spawn properties' position equal to null.");
		// if no Role properties given then use spawner to determine player class and position
		if(playerRoleProperties == null)
			return spawnPlayerRoleWithSpawnerProperties(spawner, spawnPos);
		// otherwise insert spawn position into properties and create player Role
		playerRoleProperties.put(CommonKV.KEY_POSITION, spawnPos);
		PlayerRole playerRole = (PlayerRole) myStoryHooks.createRole(playerRoleProperties);
		if(playerRole == null)
			throw new IllegalStateException("Error creating player with role properties="+playerRoleProperties);
		return playerRole;
	}

	private PlayerRole spawnPlayerRoleWithSpawnerProperties(PlayerSpawner spawner, Vector2 spawnPos) {
		String initPlayClass =
				spawner.getAgent().getProperty(CommonKV.Spawn.KEY_PLAYER_ROLECLASS, null, String.class);
		if(initPlayClass == null)
			throw new IllegalStateException("Cannot spawn player with initial play class equal to null.");
		ObjectProperties playerAP = RP_Tool.createPointRP(initPlayClass, spawnPos);
		if(RP_Tool.safeGetDirection4(spawner).isRight())
			playerAP.put(CommonKV.KEY_DIRECTION, Direction4.RIGHT);
		return (PlayerRole) myStoryHooks.createRole(playerAP);
	}

	// game is won if level is ended
	public boolean isGameWon() {
		if(playerRole == null)
			return false;
		return playerRole.getLevelEnded() != null;
	}

	public boolean isGameOver() {
		if(playerRole == null)
			return false;
		return playerRole.isGameOver();
	}

	public String getNextLevelFilename() {
		if(playerRole == null)
			return null;
		return playerRole.getLevelEnded();
	}

	public ObjectProperties getCopyPlayerRoleProperties() {
		if(playerRole == null)
			return null;
		return playerRole.getAgent().getAllProperties();
	}

	public static ObjectProperties makeRP(ObjectProperties playerRoleProperties) {
		ObjectProperties props = RP_Tool.createRP(CommonKV.RoleClassAlias.VAL_PLAYER_CONTROLLER);
		props.put(CommonKV.Player.KEY_ROLE_PROPERTIES, playerRoleProperties);
		return props;
	}
}
