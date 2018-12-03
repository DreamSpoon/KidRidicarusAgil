package com.ridicarus.kid.sprites.SMB;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.GameInfo;

public class CastleFlagSprite extends Sprite {
	public CastleFlagSprite(TextureAtlas atlas, Vector2 position) {
		super(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_CASTLEFLAG), 0, 0, 16, 16));
		setBounds(getX(), getY(), GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(Vector2 position) {
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
	}
}
