package kidridicarus.common.role.despawnbox;

import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class DespawnBox extends CorpusRole {
	public DespawnBox(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		body = new DespawnBoxBody(this, myPhysHooks, RP_Tool.getBounds(properties));
		myAgentHooks.createInternalRemovalListener(new AgentRemovalCallback() {
				@Override
				public void preAgentRemoval() { dispose(); }
				@Override
				public void postAgentRemoval() {}
			});
	}
}
