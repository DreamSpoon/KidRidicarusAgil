package kidridicarus.common.role.optional;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.story.Role;

public interface TakeDamageRole {
	// returns true if damage was taken, otherwise returns false
	public boolean onTakeDamage(Role otherRole, float dmgAmount, Vector2 dmgOrigin);
}
