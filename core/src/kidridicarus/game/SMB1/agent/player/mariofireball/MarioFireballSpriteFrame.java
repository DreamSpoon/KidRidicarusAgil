package kidridicarus.game.SMB1.agent.player.mariofireball;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.game.SMB1.agent.player.mariofireball.MarioFireballBrain.MoveState;

public class MarioFireballSpriteFrame extends SpriteFrameInput {
	public MoveState moveState;

	public MarioFireballSpriteFrame(Vector2 position, boolean isFacingRight, float timeDelta, MoveState moveState) {
		super(false, timeDelta, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
	}
}