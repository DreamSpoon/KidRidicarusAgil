package kidridicarus.game.KidIcarus.role.NPC.shemum;

import java.util.Collection;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.CommonCF.ACFB;
import kidridicarus.common.info.CommonCF.QCF;
import kidridicarus.common.info.QCC;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.role.optional.TakeDamageRole;
import kidridicarus.common.role.player.PlayerRole;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.sensor.BlockSolidSensor;
import kidridicarus.common.sensor.KeepAliveSensor;
import kidridicarus.common.sensor.OnGroundSensorFixture;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.common.tool.Direction4;
import kidridicarus.story.RoleSensor;
import kidridicarus.story.rolebody.RoleBody;
import kidridicarus.story.tool.RP_Tool;

class ShemumBody extends RoleBody {
	private static final float WALK_VEL = 0.3f;
	private static final Vector2 HITBOX_SIZE = UInfo.VectorP2M(6f, 14f);
	private static final Vector2 GROUND_SENSOR_SIZE = UInfo.VectorP2M(HITBOX_SIZE.x, 4f);
	private static final Vector2 PLAYER_SENSOR_SIZE = UInfo.VectorP2M(256f, 256f);
	private static final Vector2 PLAYER_SENSOR_OFFSET = UInfo.VectorP2M(0f, -80);

	private BlockSolidSensor blockSolidSensor;
	private OnGroundSensorFixture onGroundSF;
	private RoleSensor hitboxPlayerSensor;
	private KeepAliveSensor hitboxKeepAliveSensor;
	private RoleSensor hitboxRoomSensor;
	private RoleSensor altPlayerSensor;

	ShemumBody(PhysicsHooks physHooks, Vector2 position, Vector2 velocity) {
		super(physHooks);

		agentBody = ABodyFactory.makeDynamicBody(physHooks, position, velocity);

		// main body solid fixture and sensor
		AgentFixture solidFixture = ABodyFactory.makeBoxFixture(agentBody, CommonCF.HALF_SOLID_FILTER, HITBOX_SIZE);
		blockSolidSensor = new BlockSolidSensor(solidFixture);
		// on-ground sensor fixture attached to bottom of main body solid
		onGroundSF = new OnGroundSensorFixture(
				agentBody, solidFixture, GROUND_SENSOR_SIZE, new Vector2(0f, -HITBOX_SIZE.y/2f));

		// main body sensor fixture and sensors
		AgentFixture hitboxFixture = ABodyFactory.makeSensorBoxFixture(agentBody, new AgentFilter(
				QCF.fbs(ACFB.NPC_GIVEBIT, ACFB.PLAYER_TAKEBIT, ACFB.KEEP_ALIVE_TRIGGER_TAKEBIT,
						ACFB.DESPAWN_TRIGGER_TAKEBIT, ACFB.ROOM_TAKEBIT),
				QCF.fbs(ACFB.NPC_TAKEBIT, ACFB.PLAYER_GIVEBIT, ACFB.KEEP_ALIVE_TRIGGER_GIVEBIT,
						ACFB.DESPAWN_TRIGGER_GIVEBIT, ACFB.ROOM_GIVEBIT)),
				HITBOX_SIZE);
		hitboxPlayerSensor = new RoleSensor(hitboxFixture,
				new AgentFilter(ACFB.PLAYER_TAKEBIT, ACFB.PLAYER_GIVEBIT));
		hitboxKeepAliveSensor = new KeepAliveSensor(hitboxFixture);
		hitboxRoomSensor = new RoleSensor(hitboxFixture, new AgentFilter(ACFB.ROOM_TAKEBIT, ACFB.ROOM_GIVEBIT));

		// create alternative  player sensor fixture that covers most of the screen and detects players to target
		AgentFixture altPlayerSensorFixture = ABodyFactory.makeSensorBoxFixture(agentBody, new AgentFilter(
				ACFB.PLAYER_TAKEBIT, ACFB.PLAYER_GIVEBIT), PLAYER_SENSOR_SIZE, PLAYER_SENSOR_OFFSET);
		altPlayerSensor = new RoleSensor(altPlayerSensorFixture, new AgentFilter(
				ACFB.PLAYER_TAKEBIT, ACFB.PLAYER_GIVEBIT));
	}

	boolean isKeepAlive() {
		return hitboxKeepAliveSensor.isKeepAlive();
	}

	Collection<TakeDamageRole> getContactDmgTakeRoles() {
		return hitboxPlayerSensor.getCurrentContactsByRoleClass(TakeDamageRole.class);
	}

	RoomBox getCurrentRoom() {
		return hitboxRoomSensor.getFirstCurrentContactByRoleClass(RoomBox.class);
	}

	boolean isOnGround() {
		return onGroundSF.isOnGround();
	}

	boolean isSideMoveBlocked(boolean moveRight) {
		if(moveRight)
			return blockSolidSensor.isMoveBlocked(QCC.rectSizePos(HITBOX_SIZE, getPosition()), Direction4.RIGHT);
		else
			return blockSolidSensor.isMoveBlocked(QCC.rectSizePos(HITBOX_SIZE, getPosition()), Direction4.LEFT);
	}

	Direction4 getSideMoveToPlayerDir() {
		// if player not found then exit
		PlayerRole playerRole = altPlayerSensor.getFirstCurrentContactByRoleClass(PlayerRole.class);
		if(playerRole == null)
			return Direction4.NONE;
		// if other Role doesn't have a position then exit
		Vector2 otherPos = RP_Tool.getCenter(playerRole);
		if(otherPos == null)
			return Direction4.NONE;
		// return horizontal direction to move to player
		if(agentBody.getPosition().x < otherPos.x)
			return Direction4.RIGHT;
		else
			return Direction4.LEFT;
	}

	void doWalkMove(boolean moveRight) {
		if(moveRight)
			agentBody.setVelocity(WALK_VEL, agentBody.getVelocity().y);
		else
			agentBody.setVelocity(-WALK_VEL, agentBody.getVelocity().y);
	}
}
