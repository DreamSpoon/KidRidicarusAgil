package kidridicarus.common.role.rolespawntrigger;

import java.util.List;

import kidridicarus.common.role.optional.EnableTakeRole;

class SpawnTriggerFrameInput {
	List<EnableTakeRole> beginContacts;
	List<EnableTakeRole> endContacts;

	SpawnTriggerFrameInput(List<EnableTakeRole> beginContacts, List<EnableTakeRole> endContacts) {
		this.beginContacts = beginContacts;
		this.endContacts = endContacts;
	}
}
