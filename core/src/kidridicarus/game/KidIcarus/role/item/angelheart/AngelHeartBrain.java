package kidridicarus.game.KidIcarus.role.item.angelheart;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.role.optional.PowerupTakeRole;
import kidridicarus.common.rolespine.BasicRoleSpine;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.KidIcarus.KidIcarusAudio;
import kidridicarus.game.KidIcarus.KidIcarusPow;
import kidridicarus.story.RoleHooks;

class AngelHeartBrain {
	private static final int SMALL_HEARTCOUNT = 1;
	private static final int HALF_HEARTCOUNT = 5;
	private static final int FULL_HEARTCOUNT = 10;
	private static final float LIVE_TIME = 23/6f;

	enum AngelHeartSize { SMALL(SMALL_HEARTCOUNT), HALF(HALF_HEARTCOUNT), FULL(FULL_HEARTCOUNT);
		private int hc;
		AngelHeartSize(int hc) { this.hc = hc; }
		public int getHeartCount() { return hc; }
		public static boolean isValidHeartCount(int hc) {
			return hc == SMALL_HEARTCOUNT || hc == HALF_HEARTCOUNT || hc == FULL_HEARTCOUNT;
		}
	}

	private RoleHooks parentRoleHooks;
	private BasicRoleSpine spine;
	private float moveStateTimer;
	private boolean despawnMe;
	private boolean isUsed;
	private AngelHeartSize heartSize;

	AngelHeartBrain(RoleHooks parentRoleHooks, BasicRoleSpine spine, int heartCount) {
		this.parentRoleHooks = parentRoleHooks;
		this.spine = spine;
		moveStateTimer = 0f;
		despawnMe = false;
		isUsed = false;
		switch(heartCount) {
			case SMALL_HEARTCOUNT:
				this.heartSize = AngelHeartSize.SMALL;
				break;
			case HALF_HEARTCOUNT:
				this.heartSize = AngelHeartSize.HALF;
				break;
			case FULL_HEARTCOUNT:
				this.heartSize = AngelHeartSize.FULL;
				break;
			default:
				throw new IllegalStateException(
						"Unable to spawn this Role because of irregular heart count: "+heartCount);
		}
	}

	void processContactFrame() {
		// exit if used
		if(isUsed)
			return;
		if(!spine.isContactKeepAlive() || spine.isContactDespawn()) {
			despawnMe = true;
			return;
		}
		// if any Roles touching this powerup are able to take it, then push it to them
		PowerupTakeRole taker = spine.getTouchingPowerupTaker();
		if(taker == null)
			return;
		if(taker.onTakePowerup(new KidIcarusPow.AngelHeartPow(heartSize.hc)))
			isUsed = true;
	}

	SpriteFrameInput processFrame(float delta) {
		if(isUsed) {
			parentRoleHooks.agentHooksBundle.audioHooks.getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
			parentRoleHooks.agentHooksBundle.agentHooks.removeThisAgent();
			return null;
		}
		else if(despawnMe || moveStateTimer > LIVE_TIME) {
			parentRoleHooks.agentHooksBundle.agentHooks.removeThisAgent();
			return null;
		}
		moveStateTimer += delta;
		return SprFrameTool.place(spine.getPosition());
	}

	AngelHeartSize getHeartSize() {
		return heartSize;
	}
}
