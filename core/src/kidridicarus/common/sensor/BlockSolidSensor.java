package kidridicarus.common.sensor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFixture;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.role.tiledmap.solidlayer.SolidLineSeg;
import kidridicarus.common.tool.Direction4;
import kidridicarus.story.RoleSensor;

/*
 * Detect blocking solids, i.e. solids that block movement.
 * Direction given can be up, down, left, right - so detect ceiling, floor, left wall, right wall, respectively.
 */
public class BlockSolidSensor extends RoleSensor {
	public BlockSolidSensor(AgentFixture agentFixture) {
		super(agentFixture, CommonCF.HALF_SOLID_FILTER);
	}

	public boolean isMoveBlocked(Rectangle moveBounds, Direction4 moveDir) {
		for(AgentFixture fix : getCurrentContactsByFixtureDataClass(SolidLineSeg.class)) {
			if(isMoveBlockedByLine(moveBounds, moveDir, (SolidLineSeg) fix.getUserData()))
				return true;
		}
		return false;
	}

	private boolean isMoveBlockedByLine(Rectangle moveBounds, Direction4 moveDir, SolidLineSeg lineSeg) {
		// Check for actual bound contact, not just close call... to know if this bound is blocking just a
		// teensy bit or a reasonable amount.
		if(!lineSeg.dblCheckContact(moveBounds))
			return false;
		Vector2 center = moveBounds.getCenter(new Vector2());
		switch(moveDir) {
			case RIGHT:
				// if moving right and there is a right wall on the right then return blocked true
				if(!lineSeg.isHorizontal && !lineSeg.upNormal && center.x <= lineSeg.getBounds().x)
					return true;
				break;
			case LEFT:
				// if moving left and there is a left wall on the left then return blocked true
				if(!lineSeg.isHorizontal && lineSeg.upNormal && center.x >= lineSeg.getBounds().x)
					return true;
				break;
			case UP:
				// if moving up and there is a ceiling above then return blocked true
				if(lineSeg.isHorizontal && !lineSeg.upNormal && center.y <= lineSeg.getBounds().y)
					return true;
				break;
			case DOWN:
			default:
				// if moving down and there is a floor above then return blocked true
				if(lineSeg.isHorizontal && lineSeg.upNormal && center.y >= lineSeg.getBounds().y)
					return true;
				break;
		}
		// no blocking line segments were detected, so return false
		return false;
	}
}
