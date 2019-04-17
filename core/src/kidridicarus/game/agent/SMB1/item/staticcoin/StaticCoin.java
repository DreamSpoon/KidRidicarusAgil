package kidridicarus.game.agent.SMB1.item.staticcoin;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.staticpowerup.StaticPowerup;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.info.SMB1_Audio;
import kidridicarus.game.powerup.SMB1_Pow;

public class StaticCoin extends StaticPowerup implements DisposableAgent {
	public StaticCoin(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new StaticCoinBody(this, agency.getWorld(), AP_Tool.getCenter(properties));
		sprite = new StaticCoinSprite(agency.getAtlas(), AP_Tool.getCenter(properties));
	}

	// always returns false, since this method completely overrides the original, due to global timer sprite update
	@Override
	protected boolean doPowerupUpdate(float delta, boolean isPowUsed) {
		if(isPowUsed) {
			agency.getEar().playSound(SMB1_Audio.Sound.COIN);
			agency.removeAgent(this);
			return false;
		}
		sprite.update(agency.getGlobalTimer(), true, false, body.getPosition());
		return false;
	}

	@Override
	protected Powerup getStaticPowerupPow() {
		return new SMB1_Pow.CoinPow();
	}
}
