package kidridicarus.common.role.followbox;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;

public abstract class FollowBox extends Role {
	protected FollowBoxBody body;

	public FollowBox(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		body = null;
	}

	// set the target center position of the follow box, and the box will move on update (mouse joint)
	public void setTarget(Vector2 position) {
		if(body != null)
			body.setPosition(position);
	}
}
