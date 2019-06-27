package kidridicarus.common.role.powerup;

import kidridicarus.common.role.optional.PowerupTakeRole;
import kidridicarus.story.Role;

public abstract class Powerup {
	public abstract PowChar getPowerupCharacter();

	public static boolean tryPushPowerup(Role role, Powerup pu) {
		if(role instanceof PowerupTakeRole)
			return ((PowerupTakeRole) role).onTakePowerup(pu);
		return false;
	}
}
