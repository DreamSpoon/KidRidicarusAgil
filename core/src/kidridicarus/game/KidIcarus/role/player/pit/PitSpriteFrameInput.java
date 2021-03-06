package kidridicarus.game.KidIcarus.role.player.pit;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.role.player.pit.PitBrain.MoveState;

class PitSpriteFrameInput extends SpriteFrameInput {
	MoveState moveState;
	boolean isDmgFrame;
	boolean isShooting;
	boolean isHeadInTile;
	boolean isJumpUp;
	Direction4 climbDir;

	PitSpriteFrameInput(Vector2 position, boolean isFacingRight, FrameTime frameTime, MoveState moveState,
			boolean isDmgFrame, boolean isShooting, boolean isHeadInTile, boolean isJumpUp, Direction4 climbDir) {
		super(frameTime, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
		this.isDmgFrame = isDmgFrame;
		this.isShooting = isShooting;
		this.isHeadInTile = isHeadInTile;
		this.isJumpUp = isJumpUp;
		this.climbDir = climbDir;
	}
}
