package kidridicarus.common.role.playerrole;

import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.story.RoleHooks;

public abstract class PlayerRole extends CorpusRole {
	public abstract PlayerRoleSupervisor getSupervisor();
	public abstract RoomBox getCurrentRoom();

	protected PlayerRole(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		myAgentHooks.createAgentRemoveListener(myAgent, new AgentRemoveCallback() {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}

	public void removeSelf() {
		myAgentHooks.removeThisAgent();
	}
}
