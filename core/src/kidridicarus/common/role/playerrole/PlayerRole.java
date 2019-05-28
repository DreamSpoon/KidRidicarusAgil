package kidridicarus.common.role.playerrole;

import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.story.RoleHooks;

public abstract class PlayerRole extends CorpusRole {
	public abstract PlayerRoleSupervisor getSupervisor();
	public abstract RoomBox getCurrentRoom();

	protected PlayerRole(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		myAgentHooks.createInternalRemovalListener(new AgentRemovalCallback() {
			@Override
			public void preAgentRemoval() { dispose(); }
			@Override
			public void postAgentRemoval() {}
		});
	}

	public void removeSelf() {
		myAgentHooks.removeThisAgent();
	}
}
