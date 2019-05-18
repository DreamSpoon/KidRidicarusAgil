package kidridicarus.common.rolebrain;

import java.util.List;

import kidridicarus.common.role.optional.ContactDmgTakeRole;
import kidridicarus.common.role.roombox.RoomBox;

public class ContactDmgBrainContactFrameInput extends BrainContactFrameInput {
	public List<ContactDmgTakeRole> contactDmgTakeAgents;

	public ContactDmgBrainContactFrameInput(RoomBox room, boolean isKeepAlive, boolean isDespawn,
			List<ContactDmgTakeRole> contactDmgTakeAgents) {
		super(room, isKeepAlive, isDespawn);
		this.contactDmgTakeAgents = contactDmgTakeAgents;
	}
}
