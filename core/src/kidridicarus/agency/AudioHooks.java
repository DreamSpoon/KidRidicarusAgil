package kidridicarus.agency;

import kidridicarus.agency.tool.Ear;

public class AudioHooks {
	private Agency myAgency;

	AudioHooks(Agency agency) {
		this.myAgency = agency;
	}

	public Ear getEar() {
		return myAgency.earplug.getEar();
	}
}
