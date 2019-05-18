package kidridicarus.common.powerup;

import kidridicarus.game.KidIcarus.KidIcarusKV;

public enum PowChar {
	NONE(""),
	PIT(KidIcarusKV.RoleClassAlias.VAL_PIT);

	private String roleClassAlias;

	private PowChar(String roleClassAlias) {
		this.roleClassAlias = roleClassAlias;
	}

	public String getRoleClassAlias() {
		return roleClassAlias;
	}
}
