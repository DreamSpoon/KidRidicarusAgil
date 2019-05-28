package kidridicarus.game.KidIcarus.role.other.kidicarusdoor;

import kidridicarus.common.rolespine.BasicRoleSpine;
import kidridicarus.story.Role;

public class KidIcarusDoorSpine extends BasicRoleSpine {
	public KidIcarusDoorSpine(Role parentRole) {
		super(parentRole);
	}

	public void setOpened(boolean isOpened) {
		((KidIcarusDoorBody) roleBody).setOpened(isOpened);
	}
}
