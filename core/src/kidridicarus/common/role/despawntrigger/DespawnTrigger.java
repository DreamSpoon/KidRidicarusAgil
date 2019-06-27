package kidridicarus.common.role.despawntrigger;

import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class DespawnTrigger extends Role {
	public DespawnTrigger(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		new DespawnTriggerBody(myPhysHooks, RP_Tool.getBounds(properties));
	}
}
