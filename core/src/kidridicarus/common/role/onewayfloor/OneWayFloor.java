package kidridicarus.common.role.onewayfloor;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.role.optional.SolidRole;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

/*
 * One-way floor: What goes up must not come down, if above floor.
 */
public class OneWayFloor extends Role implements SolidRole {
	public OneWayFloor(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		Rectangle bounds = new Rectangle(RP_Tool.getBounds(properties));
		// ensure the floor bounds height = zero (basically, pushing bottom bound up to meet top bound)
		bounds.y = bounds.y + bounds.height;
		bounds.height = 0f;
		new OneWayFloorBody(myPhysHooks, bounds);
	}
}
