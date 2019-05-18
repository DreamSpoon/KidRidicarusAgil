package kidridicarus.common.role.semisolidfloor;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.common.role.optional.SolidRole;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.story.RoleHooks;

/*
 * One-way floor: What goes up must not go down, if above floor.
 */
public class SemiSolidFloor extends CorpusRole implements SolidRole {
	public SemiSolidFloor(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		Rectangle bounds = new Rectangle(RP_Tool.getBounds(properties));
		// ensure the floor bounds height = zero (essentially, creating a line at top of bounds)
		bounds.y = bounds.y + bounds.height;
		bounds.height = 0f;
		body = new SemiSolidFloorBody(this, myPhysHooks, bounds);
		myAgentHooks.createAgentRemoveListener(myAgent, new AgentRemoveCallback() {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}
}
