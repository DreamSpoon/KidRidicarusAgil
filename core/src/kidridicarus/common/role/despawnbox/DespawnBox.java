package kidridicarus.common.role.despawnbox;

import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.story.RoleHooks;

public class DespawnBox extends CorpusRole {
	public DespawnBox(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		body = new DespawnBoxBody(this, myPhysHooks, RP_Tool.getBounds(properties));
		myAgentHooks.createAgentRemoveListener(myAgent, new AgentRemoveCallback() {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}
}
