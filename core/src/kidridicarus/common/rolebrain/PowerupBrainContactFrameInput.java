package kidridicarus.common.rolebrain;

import kidridicarus.common.role.optional.PowerupTakeRole;
import kidridicarus.common.role.roombox.RoomBox;

public class PowerupBrainContactFrameInput extends BrainContactFrameInput {
	public PowerupTakeRole powerupTaker;

	public PowerupBrainContactFrameInput(RoomBox room, boolean isKeepAlive, boolean isDespawn,
			PowerupTakeRole powerupTaker) {
		super(room, isKeepAlive, isDespawn);
		this.powerupTaker = powerupTaker;
	}
}
