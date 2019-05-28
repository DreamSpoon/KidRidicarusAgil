package kidridicarus.common.role.followbox;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.story.RoleHooks;

public abstract class FollowBox extends CorpusRole {
	public FollowBox(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		myAgentHooks.createInternalRemovalListener(new AgentRemovalCallback() {
			@Override
			public void preAgentRemoval() { dispose(); }
			@Override
			public void postAgentRemoval() {}
		});
	}

	/*
	 * Set the target center position of the follow box, and the box will move on update (mouse joint).
	 */
	public void setTarget(Vector2 position) {
		((FollowBoxBody) body).setPosition(position);
	}
}
