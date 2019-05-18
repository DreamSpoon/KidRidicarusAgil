package kidridicarus.common.rolespine;

import kidridicarus.common.role.playerrole.PlayerRole;
import kidridicarus.common.rolesensor.RoleContactHoldSensor;

public class PlayerContactNerve {
	private RoleContactHoldSensor playerSensor = null;

	public RoleContactHoldSensor createPlayerSensor() {
		playerSensor = new RoleContactHoldSensor(null);
		return playerSensor;
	}

	public PlayerRole getFirstPlayerContact() {
		if(playerSensor == null)
			return null;
		return playerSensor.getFirstContactByUserDataClass(PlayerRole.class);
	}
}
