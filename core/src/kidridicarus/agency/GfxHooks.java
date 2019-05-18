package kidridicarus.agency;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import kidridicarus.agency.tool.Eye;

public class GfxHooks {
	private final Agency myAgency;

	GfxHooks(Agency agency) {
		this.myAgency = agency;
	}

	public TextureAtlas getAtlas() {
		return myAgency.panAtlas;
	}

	public Eye getEye() {
		return myAgency.myEye;
	}
}
