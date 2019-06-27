package kidridicarus.game.KidIcarus.role.player.pit;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.AgentFixtureDef;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.CommonCF.ACFB;
import kidridicarus.common.info.CommonCF.QCF;
import kidridicarus.common.info.QCC;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.role.tiledmap.solidlayer.SolidTiledMapRole;
import kidridicarus.common.sensor.OnGroundSensorFixture;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.common.tool.Direction4;
import kidridicarus.story.RoleSensor;
import kidridicarus.story.rolebody.RoleBody;
import kidridicarus.story.rolescript.ScriptedBodyState;

class PitBody extends RoleBody {
	private static final float MIN_WALK_VEL = 0.1f;
	private static final float GROUNDMOVE_XIMP = 0.2f;
	private static final float MAX_GROUNDMOVE_VEL = 0.65f;
	private static final float AIRMOVE_XIMP = GROUNDMOVE_XIMP * 0.7f;
	private static final float MAX_AIRMOVE_VEL = MAX_GROUNDMOVE_VEL;
	private static final float STOPMOVE_XIMP = 0.08f;
	private static final float JUMPUP_CONSTVEL = 1.6f;
	private static final float JUMPUP_FORCE = 15.7f;

	private static final Vector2 DUCK_TO_STAND_OFFSET = UInfo.VectorP2M(0f, 3f);
	private static final Vector2 STANDING_HITBOX_SIZE = UInfo.VectorP2M(8f, 16f);
	private static final Vector2 DUCKING_HITBOX_SIZE = UInfo.VectorP2M(8f, 10f);
	private static final Vector2 GROUND_SENSOR_SIZE = UInfo.VectorP2M(4f, 4f);
	static final float GRAVITY_SCALE = 1f;
	private static final float FRICTION = 0f;	// (default is 0.2f)

	private AgentFixture solidFixture;
	private OnGroundSensorFixture onGroundSF;
	private AgentFixture hitboxFixture;
	private RoleSensor hitboxDespawnSensor;
	private RoleSensor hitboxRoomSensor;
	private RoleSensor hitboxTileMapSensor;

	private boolean isDuckingForm;
	private boolean isSolidContactState;

	PitBody(PhysicsHooks physHooks, Vector2 position, Vector2 velocity, boolean isDuckingForm) {
		super(physHooks);
		this.isDuckingForm = isDuckingForm;
		this.isSolidContactState = true;
		defineBody(position, velocity);
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		// dispose the old body if it exists
		if(agentBody != null)
			physHooks.queueDestroyAgentBody(agentBody);

		agentBody = ABodyFactory.makeDynamicBody(physHooks, position, velocity);
		agentBody.setGravityScale(GRAVITY_SCALE);
		if(isDuckingForm)
			createFixtures(DUCKING_HITBOX_SIZE);
		else
			createFixtures(STANDING_HITBOX_SIZE);
	}

	private void createFixtures(Vector2 hitboxSize) {
		// create solid fixture
		AgentFixtureDef afdef = new AgentFixtureDef();
		afdef.friction = FRICTION;
		afdef.agentFilter = getSolidFilter();
		solidFixture = ABodyFactory.makeBoxFixture(agentBody, afdef, hitboxSize);

		// create on ground sensor fixture
		onGroundSF = new OnGroundSensorFixture(agentBody, solidFixture, GROUND_SENSOR_SIZE,
				new Vector2(0f, -hitboxSize.y/2f));

		// create hitbox for sensing (and/or being sensed by) NPCs, rooms, etc.
		hitboxFixture = ABodyFactory.makeSensorBoxFixture(agentBody, getHitboxFilter(), hitboxSize);
		hitboxDespawnSensor = new RoleSensor(hitboxFixture,
				new AgentFilter(ACFB.DESPAWN_TRIGGER_TAKEBIT, ACFB.DESPAWN_TRIGGER_GIVEBIT));
		hitboxRoomSensor = new RoleSensor(hitboxFixture, new AgentFilter(ACFB.ROOM_TAKEBIT, ACFB.ROOM_GIVEBIT));
		hitboxTileMapSensor = new RoleSensor(hitboxFixture, new AgentFilter(
				ACFB.SOLID_TILEMAP_TAKEBIT, ACFB.SOLID_TILEMAP_GIVEBIT));
	}

	private AgentFilter getSolidFilter() {
		if(isSolidContactState)
			return CommonCF.HALF_SOLID_FILTER;
		else
			return CommonCF.NO_CONTACT_FILTER;
	}

	private AgentFilter getHitboxFilter() {
		if(isSolidContactState) {
			return new AgentFilter(
					QCF.fbs(ACFB.ROOM_TAKEBIT, ACFB.PLAYER_GIVEBIT, ACFB.NPC_TAKEBIT, ACFB.POWERUP_TAKEBIT,
							ACFB.DESPAWN_TRIGGER_TAKEBIT, ACFB.SOLID_TILEMAP_TAKEBIT, ACFB.SCROLL_PUSH_TAKEBIT),
					QCF.fbs(ACFB.ROOM_GIVEBIT, ACFB.PLAYER_TAKEBIT, ACFB.NPC_GIVEBIT, ACFB.POWERUP_GIVEBIT,
							ACFB.DESPAWN_TRIGGER_GIVEBIT, ACFB.SOLID_TILEMAP_GIVEBIT, ACFB.SCROLL_PUSH_GIVEBIT));
		}
		else
			return new AgentFilter(ACFB.ROOM_TAKEBIT, ACFB.ROOM_GIVEBIT);
	}

	void applyDeadContacts() {
		isSolidContactState = false;
		solidFixture.setFilterData(getSolidFilter());
		hitboxFixture.setFilterData(getHitboxFilter());
	}

	void useScriptedBodyState(ScriptedBodyState sbState) {
		// if solid status needs to be switched...
		if(isSolidContactState != sbState.contactEnabled) {
			isSolidContactState = sbState.contactEnabled;
			solidFixture.setFilterData(getSolidFilter());
			hitboxFixture.setFilterData(getHitboxFilter());
		}
		// If scripted position is more than Epsilon distance from current position then redefine body scripted
		// position.
		if(sbState.position != null && sbState.position.sub(agentBody.getPosition()).len() > UInfo.POS_EPSILON)
			defineBody(sbState.position, sbState.velocity);
		// if position is okay, but velocity is different by at least epsilon amount, then reset to scripted velocity
		else if(sbState.velocity != null && sbState.velocity.sub(agentBody.getVelocity()).len() > UInfo.VEL_EPSILON)
			agentBody.setVelocity(sbState.velocity);
		agentBody.setGravityScale(sbState.gravityFactor * GRAVITY_SCALE);
		// Body may "fall asleep" while no activity, also while gravityScale was zero,
		// wake it up so that gravity functions properly.
		agentBody.setAwake(true);
	}

	void setDuckingForm(boolean isDuckingForm) {
		Vector2 newPos = null;
		// if currently ducking and instructed to change to standing form...
		if(this.isDuckingForm && !isDuckingForm) {
			this.isDuckingForm = isDuckingForm;
			newPos = agentBody.getPosition().cpy().add(DUCK_TO_STAND_OFFSET);
		}
		// if currently standing and instructed to change to ducking form...
		else if(!this.isDuckingForm && isDuckingForm) {
			this.isDuckingForm = isDuckingForm;
			newPos = agentBody.getPosition().cpy().sub(DUCK_TO_STAND_OFFSET);
		}
		// if new position needs to be set then redefine body at new position
		if(newPos != null)
			defineBody(newPos, agentBody.getVelocity());
	}

	boolean isDuckingForm() {
		return isDuckingForm;
	}

	RoomBox getCurrentRoom() {
		return hitboxRoomSensor.getFirstCurrentContactByRoleClass(RoomBox.class);
	}

	boolean isContactDespawn() {
		return hitboxDespawnSensor.isContact();
	}

	boolean isOnGround() {
		return onGroundSF.isOnGround();
	}

	boolean hasWalkVelocityInDir(Direction4 right) {
		return hasVelocityInDir(right, MIN_WALK_VEL);
	}

	void applyWalkMove(boolean isRight) {
		if(isRight)
			applyImpulseAndCapVel(Direction4.RIGHT, GROUNDMOVE_XIMP, MAX_GROUNDMOVE_VEL);
		else
			applyImpulseAndCapVel(Direction4.LEFT, GROUNDMOVE_XIMP, MAX_GROUNDMOVE_VEL);
	}

	void applyAirMove(boolean isRight) {
		if(isRight)
			applyImpulseAndCapVel(Direction4.RIGHT, AIRMOVE_XIMP, MAX_AIRMOVE_VEL);
		if(isRight)
			applyImpulseAndCapVel(Direction4.LEFT, AIRMOVE_XIMP, MAX_AIRMOVE_VEL);
	}

	void applyStopMove() {
		// if moving right...
		if(agentBody.getVelocity().x > MIN_WALK_VEL)
			applyImpulse(Direction4.RIGHT, -STOPMOVE_XIMP);
		// if moving left...
		else if(agentBody.getVelocity().x < -MIN_WALK_VEL)
			applyImpulse(Direction4.LEFT, -STOPMOVE_XIMP);
		// not moving right or left fast enough, set horizontal velocity to zero to avoid wobbling
		else
			agentBody.setVelocity(0f, agentBody.getVelocity().y);
	}

	void applyJumpVelocity() {
		agentBody.setVelocity(agentBody.getVelocity().x, JUMPUP_CONSTVEL);
	}

	void applyJumpForce(float forceTimer, float jumpForceDuration) {
		if(forceTimer < jumpForceDuration)
			agentBody.applyForce(new Vector2(0f, JUMPUP_FORCE * forceTimer / jumpForceDuration));
	}

	boolean isHeadInTile() {
		return isMapTileSolid(UInfo.VectorM2T(agentBody.getPosition()).add(0, 1));
	}

	boolean isMapTileSolid(Vector2 tileCoords) {
		SolidTiledMapRole ctMap =
				this.hitboxTileMapSensor.getFirstCurrentContactByRoleClass(SolidTiledMapRole.class);
		return ctMap == null ? false : ctMap.isMapTileSolid(tileCoords);
	}

	boolean isMapPointSolid(Vector2 position) {
		SolidTiledMapRole ctMap =
				this.hitboxTileMapSensor.getFirstCurrentContactByRoleClass(SolidTiledMapRole.class);
		return ctMap == null ? false : ctMap.isMapPointSolid(position);
	}

	Rectangle getBounds() {
		return QCC.rectSizePos(isDuckingForm ? DUCKING_HITBOX_SIZE : STANDING_HITBOX_SIZE, getPosition());
	}
}
