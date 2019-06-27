package kidridicarus.game.KidIcarus.role.player.pit.HUD;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import kidridicarus.common.info.GFX_Info;
import kidridicarus.common.role.player.PlayerRole;
import kidridicarus.common.role.player.HUD.PlayerHUD;
import kidridicarus.common.role.player.HUD.TexRegionActor;
import kidridicarus.game.KidIcarus.KidIcarusGfx;
import kidridicarus.game.KidIcarus.KidIcarusKV;

public class PitHUD extends PlayerHUD {
	private PlayerRole playerRole;
	private TextureAtlas atlas;

	private Label heartCountLabel;
	private HealthBarActor healthBar;

	public PitHUD(PlayerRole playerRole, TextureAtlas atlas) {
		this.playerRole = playerRole;
		this.atlas = atlas;
	}

	@Override
	public void setupStage(Stage stage) {
		Table table = new Table();
		table.top();
		table.setFillParent(true);

		LabelStyle labelstyle = new Label.LabelStyle(new BitmapFont(Gdx.files.internal(GFX_Info.SMB1_FONT), false),
				Color.WHITE);
		heartCountLabel = new Label(String.format("%03d", 0), labelstyle);

		table.add(new TexRegionActor(atlas.findRegion(KidIcarusGfx.Item.HEART1))).
				align(Align.left).padLeft(16).padTop(16);
		table.add(heartCountLabel).align(Align.left).expandX().padTop(16);
		table.row();
		healthBar = new HealthBarActor(atlas);
		table.add(healthBar).align(Align.left).padLeft(16);

		stage.addActor(table);
	}

	@Override
	protected void preDrawStage() {
		heartCountLabel.setText(String.format("%03d",
				playerRole.getAgent().getProperty(KidIcarusKV.KEY_HEART_COUNT, 0, Integer.class)));
		healthBar.setHealth(playerRole.getAgent().getProperty(KidIcarusKV.KEY_HEALTH, 0, Integer.class));
	}
}
