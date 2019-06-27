package kidridicarus.common.role.optional;

import kidridicarus.common.role.powerup.Powerup;
import kidridicarus.common.role.powerup.PowerupList;

public interface PowerupTakeRole {
	public abstract boolean onTakePowerup(Powerup pu);
	public abstract PowerupList getNonCharPowerupList();
}
