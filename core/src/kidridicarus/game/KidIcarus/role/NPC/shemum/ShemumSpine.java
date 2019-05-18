package kidridicarus.game.KidIcarus.role.NPC.shemum;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.role.playerrole.PlayerRole;
import kidridicarus.common.rolesensor.RoleContactHoldSensor;
import kidridicarus.common.rolespine.PlayerContactNerve;
import kidridicarus.common.rolespine.SolidContactSpine;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.story.Role;

class ShemumSpine extends SolidContactSpine {
	private static final float WALK_VEL = 0.3f;

	private PlayerContactNerve pcNerve;

	ShemumSpine(Role parentRole) {
		super(parentRole);
		pcNerve = new PlayerContactNerve();
	}

	void doWalkMove(boolean isFacingRight) {
		if(isFacingRight)
			body.setVelocity(WALK_VEL, body.getVelocity().y);
		else
			body.setVelocity(-WALK_VEL, body.getVelocity().y);
	}

	RoleContactHoldSensor createPlayerSensor() {
		return pcNerve.createPlayerSensor();
	}

	Direction4 getPlayerDir() {
		// if player not found then exit
		PlayerRole playerRole = pcNerve.getFirstPlayerContact();
		if(playerRole == null)
			return Direction4.NONE;
		// if other Role doesn't have a position then exit
		Vector2 otherPos = RP_Tool.getCenter(playerRole);
		if(otherPos == null)
			return Direction4.NONE;
		// return horizontal direction to move to player
		if(body.getPosition().x < otherPos.x)
			return Direction4.RIGHT;
		else
			return Direction4.LEFT;
	}
}
