package kidridicarus.agency;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.EyePlug;

public class GfxHooks {
	private TextureAtlas atlas;
	private EyePlug eyePlug;

	GfxHooks(TextureAtlas atlas, EyePlug eyePlug) {
		this.atlas = atlas;
		this.eyePlug = eyePlug;
	}

	public TextureAtlas getAtlas() {
		return atlas;
	}

	public Eye getEye() {
		return eyePlug.getEye();
	}
}
