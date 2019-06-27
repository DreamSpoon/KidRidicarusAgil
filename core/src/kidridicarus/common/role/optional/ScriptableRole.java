package kidridicarus.common.role.optional;

import kidridicarus.story.rolescript.ScriptedRoleState;

public interface ScriptableRole {
	public void scriptSetEnabled(boolean enabled);
	public boolean scriptIsEnabled();
	// Any (or all?) of the following given states could be null.
	// e.g. If only control state is needed then physical state and visual state may be null.
	public void scriptSetFrameState(ScriptedRoleState frameState);
	public ScriptedRoleState scriptGetFrameState();
	public boolean scriptIsOverrideAllowed();
}
