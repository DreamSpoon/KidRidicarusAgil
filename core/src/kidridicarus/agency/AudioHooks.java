package kidridicarus.agency;

import kidridicarus.agency.tool.Ear;
import kidridicarus.agency.tool.EarPlug;

public class AudioHooks {
	private EarPlug earPlug;

	AudioHooks(EarPlug earPlug) {
		this.earPlug = earPlug;
	}

	public Ear getEar() {
		return earPlug.getEar();
	}
}
