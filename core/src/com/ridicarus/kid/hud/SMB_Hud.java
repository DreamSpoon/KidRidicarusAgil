package com.ridicarus.kid.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.worldrunner.Player;
import com.ridicarus.kid.worldrunner.WorldRunner;

public class SMB_Hud implements Disposable {
	private SpriteBatch batch;
	private WorldRunner runner;
	private Player rePlayer;
	private Stage stage;
	private Viewport viewport;

	private Label scoreVarLabel;
	private Label coinVarLabel;
	private Label worldVarLabel;
	private Label timeVarLabel;

	public SMB_Hud(SpriteBatch batch, WorldRunner runner, Player rePlayer) {
		this.batch = batch;
		this.runner = runner;
		this.rePlayer = rePlayer;

		viewport = new FitViewport(GameInfo.V_WIDTH, GameInfo.V_HEIGHT, new OrthographicCamera());
		stage = new Stage(viewport, batch);

		Table table = new Table();
		table.top();
		table.setFillParent(true);

		LabelStyle labelstyle = new Label.LabelStyle(new BitmapFont(Gdx.files.internal(GameInfo.SMB_FONT), false),
				Color.WHITE);
		Label marioLabel = new Label("MARIO", labelstyle);
		Label worldLabel = new Label("WORLD", labelstyle);
		Label timeLabel = new Label("TIME", labelstyle);
		scoreVarLabel = new Label(String.format("%06d", 0), labelstyle);
		coinVarLabel = new Label(String.format("�%02d", 0), labelstyle);
		worldVarLabel = new Label("1-1", labelstyle);
		timeVarLabel = new Label(String.format("%03d", 0), labelstyle);

		table.add(marioLabel).align(Align.left).colspan(3).expandX().padLeft(24).padTop(16);
		table.add(worldLabel).align(Align.left).expandX().padTop(16);
		table.add(timeLabel).align(Align.left).expandX().padTop(16);
		table.row();
		table.add(scoreVarLabel).align(Align.left).expandX().padLeft(24);
		table.add(new HudCoin(runner.getAtlas())).align(Align.right);
		table.add(coinVarLabel).align(Align.left).expandX();
		table.add(worldVarLabel).align(Align.left).expandX();
		table.add(timeVarLabel).align(Align.left).expandX();

		stage.addActor(table);
	}

	public void update(float delta) {
		scoreVarLabel.setText(String.format("%06d", rePlayer.getPointTotal()));
		timeVarLabel.setText(String.format("%03d", (int) runner.getLevelTimeRemaining()));
		coinVarLabel.setText(String.format("�%02d", rePlayer.getCoinTotal()));
	}

	public void draw() {
		batch.setProjectionMatrix(stage.getCamera().combined);
		stage.draw();
	}

	public void dispose() {
		stage.dispose();
	}
}
