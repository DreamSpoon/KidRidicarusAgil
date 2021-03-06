package kidridicarus.story.rolescript;

import kidridicarus.common.tool.MoveAdvice4x2;

/*
/*
 * A "Voodoo doll" that the script uses to control the Agent.
 *   e.g. Move a sprite across the screen by manipulating the sprite's position over time.
 * Note: The information that this class contains is like a "snapshot" of the agent's state at a point in time.
 *   It can be updated on a frame by frame basis to achieve animation.
 */
public class ScriptedRoleState {
	public MoveAdvice4x2 scriptedMoveAdvice;
	public ScriptedBodyState scriptedBodyState;
	public ScriptedSpriteState scriptedSpriteState;
	public boolean isOverrideAllowed;

	public ScriptedRoleState() {
		// Init move advice to null, to signal that move advice is not being given (so use body and sprite
		// state instead).
		scriptedMoveAdvice = null;
		scriptedBodyState = null;
		scriptedSpriteState = null;
		isOverrideAllowed = false;
	}

	public ScriptedRoleState(MoveAdvice4x2 scriptedMoveAdvice, ScriptedBodyState scriptedBodyState,
			ScriptedSpriteState scriptedSpriteState, boolean isOverrideAllowed) {
		this.scriptedMoveAdvice = scriptedMoveAdvice;
		this.scriptedBodyState = scriptedBodyState;
		this.scriptedSpriteState = scriptedSpriteState;
		this.isOverrideAllowed = isOverrideAllowed;
	}

	public ScriptedRoleState cpy() {
		return new ScriptedRoleState(this.scriptedMoveAdvice, this.scriptedBodyState.cpy(),
				this.scriptedSpriteState.cpy(), this.isOverrideAllowed);
	}
}
