package kidridicarus.game.KidIcarus.role.player.pit;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.CommonCF.Alias;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.role.playerrole.PlayerRoleBody;
import kidridicarus.common.rolesensor.RoleContactHoldSensor;
import kidridicarus.common.rolesensor.SolidContactSensor;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.Role;
import kidridicarus.story.rolescript.ScriptedBodyState;

class PitBody extends PlayerRoleBody {
	private static final float STAND_BODY_WIDTH = UInfo.P2M(8f);
	private static final float STAND_BODY_HEIGHT = UInfo.P2M(16f);
	private static final float DUCKING_BODY_WIDTH = UInfo.P2M(8f);
	private static final float DUCKING_BODY_HEIGHT = UInfo.P2M(10f);
	private static final float FOOT_WIDTH = UInfo.P2M(4f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);
	private static final float GRAVITY_SCALE = 1f;
	private static final float FRICTION = 0f;	// (default is 0.2f)
	private static final Vector2 DUCK_TO_STAND_OFFSET = UInfo.VectorP2M(0f, 3f);
	// main body
	private static final CFBitSeq MAINBODY_CFCAT = new CFBitSeq(Alias.ROLE_BIT);
	private static final CFBitSeq MAINBODY_CFMASK = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT,
			CommonCF.Alias.SCROLL_PUSH_BIT, CommonCF.Alias.SEMISOLID_FLOOR_BIT);
	// role sensor
	private static final CFBitSeq RS_ENABLED_CFCAT = new CFBitSeq(CommonCF.Alias.ROLE_BIT);
	private static final CFBitSeq RS_ENABLED_CFMASK = new CFBitSeq(CommonCF.Alias.ROLE_BIT,
			CommonCF.Alias.ROOM_BIT, CommonCF.Alias.SOLID_MAP_BIT, CommonCF.Alias.POWERUP_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.SCROLL_KILL_BIT);
	private static final CFBitSeq RS_DISABLED_CFCAT = new CFBitSeq(CommonCF.Alias.ROLE_BIT);
	private static final CFBitSeq RS_DISABLED_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT,
			CommonCF.Alias.SOLID_MAP_BIT);
	private static final CFBitSeq GROUND_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.ROLE_BIT);
	private static final CFBitSeq GROUND_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT,
			CommonCF.Alias.SEMISOLID_FLOOR_FOOT_BIT);

	private SolidContactSensor solidSensor;
	private RoleContactHoldSensor roleSensor;
	private boolean isRoleSensorEnabled;
	private Fixture roleSensorFixture;
	private boolean isDuckingForm;

	PitBody(Role parentRole, PhysicsHooks physHooks, Vector2 position, Vector2 velocity, boolean isDuckingForm,
			SolidContactSensor solidSensor, RoleContactHoldSensor roleSensor) {
		super(physHooks, position, velocity);
		this.solidSensor = solidSensor;
		this.roleSensor = roleSensor;
		this.isDuckingForm = isDuckingForm;
		isRoleSensorEnabled = true;
		defineBody(new Rectangle(position.x, position.y, 0f, 0f), velocity);
	}

	private void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(agentBody != null)
			physHooks.destroyBody(agentBody);

		if(isDuckingForm)
			setBoundsSize(DUCKING_BODY_WIDTH, DUCKING_BODY_HEIGHT);
		else
			setBoundsSize(STAND_BODY_WIDTH, STAND_BODY_HEIGHT);
		agentBody = ABodyFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()), velocity);
		agentBody.setGravityScale(GRAVITY_SCALE);
		createFixtures();
		resetPrevValues();
	}

	private void createFixtures() {
		// create main fixture
		FixtureDef fdef = new FixtureDef();
		fdef.friction = FRICTION;
		ABodyFactory.makeBoxFixture(agentBody, fdef, MAINBODY_CFCAT, MAINBODY_CFMASK, this,
				getBounds().width, getBounds().height);

		// create Role sensor fixture
		if(isRoleSensorEnabled) {
			roleSensorFixture = ABodyFactory.makeSensorBoxFixture(agentBody, RS_ENABLED_CFCAT, RS_ENABLED_CFMASK,
					roleSensor, getBounds().width, getBounds().height);
		}
		else {
			roleSensorFixture = ABodyFactory.makeSensorBoxFixture(agentBody, RS_DISABLED_CFCAT, RS_DISABLED_CFMASK,
					roleSensor, getBounds().width, getBounds().height);
		}
		// create on ground sensor fixture
		ABodyFactory.makeSensorBoxFixture(agentBody, GROUND_SENSOR_CFCAT, GROUND_SENSOR_CFMASK, solidSensor,
				FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -getBounds().height/2f));
	}

	void useScriptedBodyState(ScriptedBodyState sbState) {
		if(sbState.contactEnabled && !isRoleSensorEnabled) {
			((AgentBodyFilter) roleSensorFixture.getUserData()).categoryBits = RS_ENABLED_CFCAT;
			((AgentBodyFilter) roleSensorFixture.getUserData()).maskBits = RS_ENABLED_CFMASK;
			roleSensorFixture.refilter();
			isRoleSensorEnabled = true;
		}
		else if(!sbState.contactEnabled && isRoleSensorEnabled) {
			((AgentBodyFilter) roleSensorFixture.getUserData()).categoryBits = RS_DISABLED_CFCAT;
			((AgentBodyFilter) roleSensorFixture.getUserData()).maskBits = RS_DISABLED_CFMASK;
			roleSensorFixture.refilter();
			isRoleSensorEnabled = false;
		}
		if(!sbState.position.epsilonEquals(getPosition(), UInfo.POS_EPSILON))
			defineBody(new Rectangle(sbState.position.x, sbState.position.y, 0f, 0f), new Vector2(0f, 0f));
		agentBody.setGravityScale(sbState.gravityFactor * GRAVITY_SCALE);
		// Body may "fall asleep" while no activity, also while gravityScale was zero,
		// wake it up so that gravity functions again.
		agentBody.setAwake(true);
	}

	void applyDead() {
		allowOnlyDeadContacts();
		agentBody.setGravityScale(0f);
	}

	private void allowOnlyDeadContacts() {
		// disable all, and...
		agentBody.disableAllContacts();
		// ... re-enable the needed agent contact sensor bits
		((AgentBodyFilter) roleSensorFixture.getUserData()).categoryBits = RS_DISABLED_CFCAT;
		((AgentBodyFilter) roleSensorFixture.getUserData()).maskBits = RS_DISABLED_CFMASK;
		roleSensorFixture.refilter();
		isRoleSensorEnabled = false;
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
			defineBody(new Rectangle(newPos.x, newPos.y, 0f, 0f), agentBody.getVelocity());
	}

	boolean isDuckingForm() {
		return isDuckingForm;
	}
}
