package kidridicarus.agent.Metroid.NPC;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.body.Metroid.NPC.ZoomerBody;
import kidridicarus.info.UInfo;
import kidridicarus.info.GameInfo.DiagonalDir4;
import kidridicarus.info.GameInfo.Direction4;

/*
 * Used by Zoomer class to figure out this whole crawling thing.
 */
public class CrawlNerve {
	/*
	 * MOVEVEL.x is the horizontal velocity when moving right, and
	 * MOVEVEL.y is the vertical velocity when moving right,
	 * before MOVE_REDUCE_FACTOR is applied.
	 * So, for example. If the zoomer is moving right on screen, and the zoomer's up direction is the screen
	 * up direction, then the reduction factor is applied to the movement y velocity - to reduce the down velocity.
	 * TODO: Refactor to simplify.
	 * Zoomer moves against (presses against?) the surface it is crawling on, so that it will 'juke' downward
	 * when it comes to a gap - the 'juke' is detected, and the zoomer turns down to crawl (down relative to
	 * original orientation).
	 */
	private static final Vector2 MOVEVEL = new Vector2(0.25f, -0.25f);
	private static final float MOVE_REDUCE_FACTOR = 0.2f;
	private static final float JUKE_EPSILON1 = 0.0002f;
	private static final float JUKE_EPSILON2 = UInfo.P2M(0.01f);

	/*
	 * If current up direction is null, then return UP.
	 * Otherwise, use info about current up direction, walk direction, body positions, to determine if the
	 * up direction needs to rotate in order for the nerve user to 'crawl' along the current surface.
	 */
	public static Direction4 checkUp(Direction4 curUpDir, boolean isWalkingRight, Vector2 curBodyPosition,
			Vector2 oldBodyPosition) {
		/* Get the movement delta between this frame and last frame, and apply transform according to upDir and
		 * isWalkingRight to get movement assuming upDir is actually "UP", and isWalkingRight is true.
		 * This transform simplifies later calculations. 
		 */
		Vector2 moveDelta = getRelPosition(curBodyPosition.cpy().sub(oldBodyPosition), curUpDir, isWalkingRight);
		boolean rotateClockwise = false;
		boolean rotateCC = false;
		// if they moved downward, then they must have come to the end of a lineSeg, so rotate clockwise
		if(moveDelta.y < -JUKE_EPSILON1)
			rotateClockwise = true;
		else if(moveDelta.x < JUKE_EPSILON2)
			rotateCC = true;
		if(rotateClockwise) {
			if(isWalkingRight)
				return curUpDir.rotate270();
			else
				return curUpDir.rotate90();
		}
		else if(rotateCC) {
			if(isWalkingRight)
				return curUpDir.rotate90();
			else
				return curUpDir.rotate270();
		}
		else
			return curUpDir;
	}

	/*
	 * Basically, rotate pos according to relUpDir and flipSign (flipsign is applied after, and only to x,
	 * because it relates to isWalkingRight).
	 * 
	 * "UP" is considered zero.
	 * Moving right is considered zero.
	 */
	private static Vector2 getRelPosition(Vector2 pos, Direction4 relUpDir, boolean notFlipsign) {
		switch(relUpDir) {
			case UP:
				return new Vector2(notFlipsign ? pos.x : -pos.x, pos.y);
			case DOWN:
				return new Vector2(notFlipsign ? -pos.x : pos.x, -pos.y);
			case RIGHT:
				return new Vector2(notFlipsign ? -pos.y : pos.y, pos.x);
			// LEFT, by deduction
			default:
				return new Vector2(notFlipsign ? pos.y : -pos.y, -pos.x);
		}
	}

	/*
	 * Based on the up direction and is walking right/left, return the movement direction.
	 */
	public static Vector2 getMoveVec(boolean isWalkingRight, Direction4 upDir) {
		if(isWalkingRight)
			return getMoveRight(upDir);
		else
			return getMoveLeft(upDir);
	}

	private static Vector2 getMoveRight(Direction4 upDir) {
		switch(upDir) {
			case RIGHT:
				return new Vector2(-MOVEVEL.x*MOVE_REDUCE_FACTOR, MOVEVEL.y);
			case UP:
				return new Vector2(MOVEVEL.x, MOVEVEL.y*MOVE_REDUCE_FACTOR);
			case LEFT:
				return new Vector2(MOVEVEL.x*MOVE_REDUCE_FACTOR, -MOVEVEL.y);
			// DOWN, by deduction
			default:
				return new Vector2(-MOVEVEL.x, -MOVEVEL.y*MOVE_REDUCE_FACTOR);
		}
	}

	private static Vector2 getMoveLeft(Direction4 upDir) {
		switch(upDir) {
			case RIGHT:
				return new Vector2(-MOVEVEL.x*MOVE_REDUCE_FACTOR, -MOVEVEL.y);
			case UP:
				return new Vector2(-MOVEVEL.x, MOVEVEL.y*MOVE_REDUCE_FACTOR);
			case LEFT:
				return new Vector2(MOVEVEL.x*MOVE_REDUCE_FACTOR, MOVEVEL.y);
			// DOWN, by deduction
			default:
				return new Vector2(MOVEVEL.x, -MOVEVEL.y*MOVE_REDUCE_FACTOR);
		}
	}

	private static final short TOPRIGHT_BIT = 2 << 0;
	private static final short TOPLEFT_BIT = 2 << 1;
	private static final short BOTTOMLEFT_BIT = 2 << 2;
	private static final short BOTTOMRIGHT_BIT = 2 << 3;
	public static Direction4 getInitialUpDir(boolean isWalkingRight, ZoomerBody zBody) {
		short totalSense = 0;
		if(zBody.isSensorContacting(DiagonalDir4.TOPRIGHT))
			totalSense += TOPRIGHT_BIT;
		if(zBody.isSensorContacting(DiagonalDir4.TOPLEFT))
			totalSense += TOPLEFT_BIT;
		if(zBody.isSensorContacting(DiagonalDir4.BOTTOMLEFT))
			totalSense += BOTTOMLEFT_BIT;
		if(zBody.isSensorContacting(DiagonalDir4.BOTTOMRIGHT))
			totalSense += BOTTOMRIGHT_BIT;

		if(isWalkingRight) 
			return getDirRight(totalSense, null);
		else
			return getDirLeft(totalSense, null);
	}

	/*
	 * Check sensors to determine "up" based on assumption that Zoomer will move right initially.
	 * To explain why left/right differentiation is necessary, consider the following:
	 * E.g. 1) If the zoomer's bottom sensors are contacting, and the top sensors are not contacting,
	 * then the zoomer is on the floor - it doesn't matter which direction it is moving.
	 * E.g. 2) If 3 of the zoomer's crawl sensors are contacting then we should check it's move direction to estimate
	 * where it would end up after it moves a short distance. If the topleft, bottomleft, and bottomright sensors
	 * are contacting, and the zoomer is moving right, then it should be put on the floor. Because, if it were put
	 * on the left wall then it move right initially (which is "down" the wall), and immediately change direction
	 * because it hit the floor. So, just set it's initial position to the floor.
	 */
	private static Direction4 getDirRight(short totalSense, Direction4 initDir) {
		switch(totalSense) {
			// all sensors contacting?
			case (TOPRIGHT_BIT | TOPLEFT_BIT | BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT):
			// no sensors contacting?
			case 0:
				// need to initialize move dir?
				if(initDir == null)
					return Direction4.UP;
				// spin later like your life depends on it!
				else
					return initDir.rotate90();
			// attach to ceiling?
			case (TOPRIGHT_BIT | TOPLEFT_BIT):
			case (TOPRIGHT_BIT | TOPLEFT_BIT | BOTTOMRIGHT_BIT):
				return Direction4.DOWN;
			// attach to left wall?
			case (TOPLEFT_BIT | BOTTOMLEFT_BIT):
			case (TOPLEFT_BIT | BOTTOMLEFT_BIT | TOPRIGHT_BIT):
				return Direction4.RIGHT;
			// attach to floor?
			case (BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT):
			case (BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT | TOPLEFT_BIT):
				return Direction4.UP;
			// attach to right wall, by deduction
			default:
				return Direction4.LEFT;
		}
	}

	/*
	 * Check sensors to determine "up" based on assumption that Zoomer will move left initially.
	 */ 
	private static Direction4 getDirLeft(short totalSense, Direction4 initDir) {
		switch(totalSense) {
			// all sensors contacting?
			case (TOPRIGHT_BIT | TOPLEFT_BIT | BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT):
			// no sensors contacting?
			case 0:
				// need to initialize move dir?
				if(initDir == null)
					return Direction4.UP;
				// spin later like your life depends on it!
				else
					return initDir.rotate270();
			// attach to ceiling?
			case (TOPRIGHT_BIT | TOPLEFT_BIT):
			case (TOPRIGHT_BIT | TOPLEFT_BIT | BOTTOMLEFT_BIT):
				return Direction4.DOWN;
			// attach to left wall?
			case (TOPLEFT_BIT | BOTTOMLEFT_BIT):
			case (TOPLEFT_BIT | BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT):
				return Direction4.RIGHT;
			// attach to floor?
			case (BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT):
			case (BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT | TOPRIGHT_BIT):
				return Direction4.UP;
			// attach to right wall, by deduction
			default:
				return Direction4.LEFT;
		}
	}
}