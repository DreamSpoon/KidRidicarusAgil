package kidridicarus.common.role.powerup;

import java.util.Collection;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.tool.FilterBitSet;
import kidridicarus.common.info.CommonCF.ACFB;
import kidridicarus.common.role.optional.PowerupTakeRole;
import kidridicarus.common.sensor.KeepAliveSensor;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.RoleSensor;
import kidridicarus.story.rolebody.RoleBody;

public class PowerupBody extends RoleBody {
	public class PowerupBodyFdbk {
		public boolean isKeepAlive;
		public Collection<PowerupTakeRole> powerupTakers;
	}

	private KeepAliveSensor keepAliveSensor;
	private RoleSensor powerupTakeSensor;

	public PowerupBody(PhysicsHooks physHooks, Vector2 position, Vector2 hitboxSize) {
		super(physHooks);
		agentBody = ABodyFactory.makeDynamicBody(physHooks, position);
		agentBody.setGravityScale(0f);
		AgentFixture myFixture = ABodyFactory.makeBoxFixture(agentBody, new AgentFilter(
				new FilterBitSet(
					ACFB.POWERUP_GIVEBIT, ACFB.KEEP_ALIVE_TRIGGER_TAKEBIT, ACFB.DESPAWN_TRIGGER_TAKEBIT),
				new FilterBitSet(
					ACFB.POWERUP_TAKEBIT, ACFB.KEEP_ALIVE_TRIGGER_GIVEBIT, ACFB.DESPAWN_TRIGGER_GIVEBIT)),
				hitboxSize);
		this.keepAliveSensor = new KeepAliveSensor(myFixture);
		this.powerupTakeSensor = new RoleSensor(myFixture, new AgentFilter(
				new FilterBitSet(ACFB.POWERUP_GIVEBIT), new FilterBitSet(ACFB.POWERUP_TAKEBIT)));
	}

	public PowerupBodyFdbk getPowerupFeedback() {
		PowerupBodyFdbk fdbk = new PowerupBodyFdbk();
		fdbk.isKeepAlive = keepAliveSensor.isKeepAlive();
		fdbk.powerupTakers = powerupTakeSensor.getCurrentContactsByRoleClass(PowerupTakeRole.class);
		return fdbk;
	}
}
