package kidridicarus.agent.sprite.Metroid.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.Metroid.player.Samus.MoveState;
import kidridicarus.info.MetroidAnim;
import kidridicarus.info.UInfo;

public class SamusSprite extends Sprite {
	private static final float BIG_SPRITE_WIDTH = UInfo.P2M(32);
	private static final float BIG_SPRITE_HEIGHT = UInfo.P2M(40);
	private static final Vector2 BIG_SPRITE_OFFSET = UInfo.P2MVector(0, 5);

	private static final float MED_SPRITE_WIDTH = UInfo.P2M(24);
	private static final float MED_SPRITE_HEIGHT = UInfo.P2M(24);
	private static final Vector2 MED_SPRITE_OFFSET = UInfo.P2MVector(0, 5);

	private static final float SML_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SML_SPRITE_HEIGHT = UInfo.P2M(16);

	private static final float ANIM_SPEED = 0.075f;

	private Animation<TextureRegion> aimRightAnim;
	private Animation<TextureRegion> aimUpAnim;
	private Animation<TextureRegion> runAnim;
	private Animation<TextureRegion> runAimRightAnim;
	private Animation<TextureRegion> runAimUpAnim;
	private Animation<TextureRegion> jumpAnim;
	private Animation<TextureRegion> jumpAimRightAnim;
	private Animation<TextureRegion> jumpAimUpAnim;
	private Animation<TextureRegion> jumpSpinAnim;
	private Animation<TextureRegion> ballAnim;
	private float stateTimer;

	public SamusSprite(TextureAtlas atlas, Vector2 position) {
		aimRightAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.AIMRIGHT), PlayMode.LOOP);
		aimUpAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.AIMUP), PlayMode.LOOP);
		runAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.RUN), PlayMode.LOOP);
		runAimRightAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.RUN_AIMRIGHT), PlayMode.LOOP);
		runAimUpAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.RUN_AIMUP), PlayMode.LOOP);
		jumpAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.JUMP), PlayMode.LOOP);
		jumpAimRightAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.JUMP_AIMRIGHT), PlayMode.LOOP);
		jumpAimUpAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.JUMP_AIMUP), PlayMode.LOOP);
		jumpSpinAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.JUMPSPIN), PlayMode.LOOP);
		ballAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.BALL), PlayMode.LOOP);

		stateTimer = 0f;
		setRegion(aimRightAnim.getKeyFrame(0));
		setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position, MoveState parentState, boolean isFacingRight) {
		Vector2 offset = new Vector2(0f, 0f);
		switch(parentState) {
			case STAND:
				setRegion(aimRightAnim.getKeyFrame(stateTimer, true));
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				offset.set(BIG_SPRITE_OFFSET);
				break;
			case RUN:
				setRegion(runAnim.getKeyFrame(stateTimer, true));
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				offset.set(BIG_SPRITE_OFFSET);
				break;
			case AIR:
			case JUMP:
				setRegion(jumpAnim.getKeyFrame(stateTimer, true));
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				offset.set(BIG_SPRITE_OFFSET);
				break;
			case AIRSPIN:
			case JUMPSPIN:
				setRegion(jumpSpinAnim.getKeyFrame(stateTimer, true));
				setBounds(getX(), getY(), MED_SPRITE_WIDTH, MED_SPRITE_HEIGHT);
				offset.set(MED_SPRITE_OFFSET);
				break;
		}

		// should the sprite be flipped on X due to facing direction?
		if((isFacingRight && isFlipX()) || (!isFacingRight && !isFlipX()))
			flip(true,  false);

		// update sprite position
		setPosition(position.x - getWidth()/2 + offset.x, position.y - getHeight()/2 + offset.y);

		stateTimer += delta;
	}
}