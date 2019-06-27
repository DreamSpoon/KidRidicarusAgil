package kidridicarus.common.sensor;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.tool.FilterBitSet;
import kidridicarus.common.info.CommonCF.ACFB;
import kidridicarus.common.role.despawntrigger.DespawnTrigger;
import kidridicarus.common.role.keepalivetrigger.KeepAliveTrigger;
import kidridicarus.story.RoleSensor;

// parent role "takes" despawn trigger and keep alive trigger contacts 
public class KeepAliveSensor extends RoleSensor {
	public KeepAliveSensor(AgentFixture agentFixture) {
		super(agentFixture, new AgentFilter(
				new FilterBitSet(ACFB.KEEP_ALIVE_TRIGGER_TAKEBIT, ACFB.DESPAWN_TRIGGER_TAKEBIT),
				new FilterBitSet(ACFB.KEEP_ALIVE_TRIGGER_GIVEBIT, ACFB.DESPAWN_TRIGGER_GIVEBIT)));
	}

	/*
	 * Keep alive equals not contacting despawn trigger and is contacting keep alive trigger.
	 * If needed, a separate method could be created for isDespawn.
	 */
	public boolean isKeepAlive() {
		return isCurrentContactRoleClass(KeepAliveTrigger.class) && !isCurrentContactRoleClass(DespawnTrigger.class);
	}
}
