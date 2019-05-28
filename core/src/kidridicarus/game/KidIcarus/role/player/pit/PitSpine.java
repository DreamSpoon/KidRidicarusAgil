package kidridicarus.game.KidIcarus.role.player.pit;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.common.role.playerrole.PlayerSpine;
import kidridicarus.story.Role;
import kidridicarus.story.rolescript.ScriptedBodyState;

class PitSpine extends PlayerSpine {
	private static final float MIN_WALK_VEL = 0.1f;
	private static final float GROUNDMOVE_XIMP = 0.2f;
	private static final float MAX_GROUNDMOVE_VEL = 0.65f;
	private static final float STOPMOVE_XIMP = 0.08f;
	private static final float AIRMOVE_XIMP = GROUNDMOVE_XIMP * 0.7f;
	private static final float MAX_AIRMOVE_VEL = MAX_GROUNDMOVE_VEL;
	private static final float JUMPUP_FORCE = 15.7f;
	private static final float JUMPUP_CONSTVEL = 1.6f;
	private static final Vector2 DEAD_VEL = UInfo.VectorP2M(0f, -120);

	public PitSpine(Role parentRole) {
		super(parentRole);
	}

	// apply walk impulse and cap horizontal velocity.
	void applyWalkMove(boolean moveRight) {
		applyHorizImpulseAndCapVel(moveRight, GROUNDMOVE_XIMP, MAX_GROUNDMOVE_VEL);
	}

	// apply air impulse and cap horizontal velocity.
	void applyAirMove(boolean moveRight) {
		applyHorizImpulseAndCapVel(moveRight, AIRMOVE_XIMP, MAX_AIRMOVE_VEL);
	}

	void applyStopMove() {
		// if moving right...
		if(roleBody.getVelocity().x > MIN_WALK_VEL)
			applyHorizontalImpulse(true, -STOPMOVE_XIMP);
		// if moving left...
		else if(roleBody.getVelocity().x < -MIN_WALK_VEL)
			applyHorizontalImpulse(false, -STOPMOVE_XIMP);
		// not moving right or left fast enough, set horizontal velocity to zero to avoid wobbling
		else
			roleBody.setVelocity(0f, roleBody.getVelocity().y);
	}

	void applyJumpForce(float forceTimer, float jumpForceDuration) {
		if(forceTimer < jumpForceDuration)
			roleBody.applyForce(new Vector2(0f, JUMPUP_FORCE * forceTimer / jumpForceDuration));
	}

	void applyJumpVelocity() {
		roleBody.setVelocity(roleBody.getVelocity().x, JUMPUP_CONSTVEL);
	}

	boolean isStandingStill() {
		return isStandingStill(MIN_WALK_VEL);
	}

	void checkDoBodySizeChange(boolean isNextDucking) {
		boolean isCurrentlyDucking = ((PitBody) roleBody).isDuckingForm();
		boolean isSolidTileAbove = isMapTileSolid(UInfo.VectorM2T(roleBody.getPosition()).add(0, 1));
		if(isCurrentlyDucking && !isNextDucking && !isSolidTileAbove)
			((PitBody) roleBody).setDuckingForm(false);
		else if(!isCurrentlyDucking && isNextDucking)
			((PitBody) roleBody).setDuckingForm(true);
	}

	boolean isHeadInTile() {
		return isMapTileSolid(UInfo.VectorM2T(roleBody.getPosition()).add(0, 1));
	}

	boolean isWalkingRight() {
		return roleBody.getVelocity().x > MIN_WALK_VEL;
	}

	boolean isWalkingLeft() {
		return roleBody.getVelocity().x < -MIN_WALK_VEL;
	}

	void applyDead() {
		((PitBody) roleBody).applyDead();
		roleBody.setVelocity(DEAD_VEL);
	}

	public void useScriptedBodyState(ScriptedBodyState scriptedBodyState) {
		((PitBody) roleBody).useScriptedBodyState(scriptedBodyState);
	}
}
