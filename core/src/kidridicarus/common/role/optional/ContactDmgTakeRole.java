package kidridicarus.common.role.optional;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.story.Role;

public interface ContactDmgTakeRole {
	// returns true if damage was taken, otherwise returns false
	public boolean onTakeDamage(Role role, float amount, Vector2 dmgOrigin);
}
