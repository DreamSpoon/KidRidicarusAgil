package kidridicarus.common.role.rolespawntrigger;

import java.util.Collection;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.tool.FilterBitSet;
import kidridicarus.common.info.CommonCF.ACFB;
import kidridicarus.common.role.followbox.FollowBoxBody;
import kidridicarus.common.role.optional.EnableTakeRole;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.Role;
import kidridicarus.story.RoleSensor;

class RoleSpawnTriggerBody extends FollowBoxBody {
	private RoleSensor mySensor;

	RoleSpawnTriggerBody(PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks, bounds);
	}

	@Override
	protected AgentFixture createAgentFixture(Vector2 size) {
		AgentFilter filter = new AgentFilter(new FilterBitSet(ACFB.SPAWN_TRIGGER_GIVEBIT),
				new FilterBitSet(ACFB.SPAWN_TRIGGER_TAKEBIT));
		AgentFixture fixture = ABodyFactory.makeSensorBoxFixture(agentBody, filter, size);
		this.mySensor = new RoleSensor(fixture, filter);
		return fixture;
	}

	Collection<EnableTakeRole> getEnableTakeBeginContacts() {
		return mySensor.getBeginContactsByRoleClass(EnableTakeRole.class);
	}

	boolean isContactingRole(EnableTakeRole role) {
		return mySensor.isCurrentContactRole((Role) role);
	}
}
