package kidridicarus.game.agent.Metroid.player.samus.HUD;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.game.agent.Metroid.player.samus.Samus;
import kidridicarus.game.info.GameInfo;
import kidridicarus.game.info.GameKV;

public class SamusHUD implements Disposable {
	private Samus samus;
	private Stage stage;

	private Label energyAmountLabel;

	public SamusHUD(Samus samus, TextureAtlas atlas, Stage stage) {
		this.samus = samus;
		this.stage = stage;

		Table table = new Table();
		table.top();
		table.setFillParent(true);

		LabelStyle labelstyle = new Label.LabelStyle(new BitmapFont(Gdx.files.internal(GameInfo.SMB1_FONT), false),
				Color.WHITE);
		energyAmountLabel = new Label(String.format("%02d", 0), labelstyle);

		table.add(new EnergyTextActor(atlas)).align(Align.left).padLeft(24).padTop(16);
		table.add(energyAmountLabel).align(Align.left).expandX().padTop(16);

		stage.addActor(table);
	}

	public void update() {
		energyAmountLabel.setText(String.format("%02d",
				samus.getProperty(GameKV.Metroid.KEY_ENERGY_SUPPLY, 0, Integer.class)));
		stage.act();
	}

	public void draw() {
		update();
		stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
		stage.draw();
	}

	@Override
	public void dispose() {
		stage.dispose();
	}
}
