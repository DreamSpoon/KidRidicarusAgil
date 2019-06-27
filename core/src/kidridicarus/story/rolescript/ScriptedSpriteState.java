package kidridicarus.story.rolescript;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.tool.Direction4;

public class ScriptedSpriteState {
	public enum SpriteState { STAND, MOVE, CLIMB }

	public SpriteState spriteState;
	public Vector2 position;
	public boolean visible;
	public boolean isFacingRight;
	public Direction4 moveDir;

	public ScriptedSpriteState() {
		spriteState = SpriteState.STAND;
		position = new Vector2(0f, 0f);
		visible = false;
		isFacingRight = false;
		moveDir = Direction4.NONE;
	}

	public ScriptedSpriteState(SpriteState spriteState, Vector2 position, boolean visible, boolean isFacingRight,
			Direction4 moveDir) {
		this.spriteState = spriteState;
		this.position = position;
		this.visible = visible;
		this.isFacingRight = isFacingRight;
		this.moveDir = moveDir;
	}

	public ScriptedSpriteState cpy() {
		return new ScriptedSpriteState(this.spriteState, this.position.cpy(), this.visible, this.isFacingRight,
				this.moveDir);
	}
}
