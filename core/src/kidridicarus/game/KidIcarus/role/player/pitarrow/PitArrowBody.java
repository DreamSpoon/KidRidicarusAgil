package kidridicarus.game.KidIcarus.role.player.pitarrow;

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
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.sensor.BlockSolidSensor;
import kidridicarus.common.sensor.KeepAliveSensor;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.common.tool.Direction4;
import kidridicarus.story.RoleSensor;
import kidridicarus.story.rolebody.RoleBody;

class PitArrowBody extends RoleBody {
	private static final Vector2 HITBOX_SIZE = UInfo.VectorP2M(3f, 3f);
	private static final float GRAVITY_SCALE = 0f;

	private BlockSolidSensor blockSolidSensor;
	private RoleSensor hitboxRoomSensor;
	private KeepAliveSensor hitboxKeepAliveSensor;
	private RoleSensor hitboxNPC_Sensor;

	PitArrowBody(PhysicsHooks physHooks, Vector2 position, Vector2 velocity) {
		super(physHooks);
		agentBody = ABodyFactory.makeDynamicBody(physHooks, position, velocity);
		agentBody.setGravityScale(GRAVITY_SCALE);
		agentBody.setBullet(true);
		// create solid fixture
		AgentFixture solidFixture = ABodyFactory.makeBoxFixture(agentBody, CommonCF.HALF_SOLID_FILTER, HITBOX_SIZE);
		blockSolidSensor = new BlockSolidSensor(solidFixture);
		// create general contact sensor fixture
		AgentFixture hitboxFixture = ABodyFactory.makeSensorBoxFixture(agentBody, new AgentFilter(
				QCF.fbs(ACFB.ROOM_TAKEBIT, ACFB.KEEP_ALIVE_TRIGGER_TAKEBIT, ACFB.DESPAWN_TRIGGER_TAKEBIT,
						ACFB.NPC_TAKEBIT),
				QCF.fbs(ACFB.ROOM_GIVEBIT, ACFB.KEEP_ALIVE_TRIGGER_GIVEBIT, ACFB.DESPAWN_TRIGGER_GIVEBIT,
						ACFB.NPC_GIVEBIT)),
				HITBOX_SIZE);
		hitboxRoomSensor = new RoleSensor(hitboxFixture,
				new AgentFilter(QCF.fbs(ACFB.ROOM_TAKEBIT), QCF.fbs(ACFB.ROOM_GIVEBIT)));
		hitboxKeepAliveSensor = new KeepAliveSensor(hitboxFixture);
		hitboxNPC_Sensor = new RoleSensor(hitboxFixture,
				new AgentFilter(QCF.fbs(ACFB.NPC_TAKEBIT), QCF.fbs(ACFB.NPC_GIVEBIT)));
	}

	boolean isMoveBlocked(Direction4 arrowDir) {
		return this.blockSolidSensor.isMoveBlocked(QCC.rectSizePos(HITBOX_SIZE, getPosition()), arrowDir);
	}

	RoomBox getCurrentRoom() {
		return hitboxRoomSensor.getFirstCurrentContactByRoleClass(RoomBox.class);
	}

	boolean isContactKeepAlive() {
		return hitboxKeepAliveSensor.isKeepAlive();
	}

	Collection<TakeDamageRole> getContactDmgTakeRoles() {
		return hitboxNPC_Sensor.getCurrentContactsByRoleClass(TakeDamageRole.class);
	}
}
