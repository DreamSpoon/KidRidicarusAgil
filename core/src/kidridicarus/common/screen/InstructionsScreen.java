package kidridicarus.common.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import kidridicarus.agency.tool.Eye;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.KeyboardMapping;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.role.tiledmap.TiledMapMetaRole;
import kidridicarus.game.MyKidRidicarus;

public class InstructionsScreen implements Screen {
	private MyKidRidicarus game;
	private OrthogonalTiledMapRenderer tileRenderer;
	private OrthographicCamera gamecam;
	private Viewport viewport;
	private Stage stage;
	private InputProcessor oldInPr;

	private String nextLevelFilename;
	private boolean goRedTeamGo;
	private Eye myEye;

	public InstructionsScreen(MyKidRidicarus game, String nextLevelFilename) {
		this.game = game;
		this.nextLevelFilename = nextLevelFilename;
		tileRenderer = new OrthogonalTiledMapRenderer(null, UInfo.P2M(1f), game.batch);
		goRedTeamGo = false;

		gamecam = new OrthographicCamera();
		viewport = new FitViewport(UInfo.P2M(CommonInfo.V_WIDTH), UInfo.P2M(CommonInfo.V_HEIGHT), gamecam);
		// set position so bottom left of view screen is (0, 0) in Box2D world 
		gamecam.position.set(viewport.getWorldWidth()/2f, viewport.getWorldHeight()/2f, 0);
		// camera for stage is different from camera for game world
		stage = new Stage(new FitViewport(CommonInfo.V_WIDTH, CommonInfo.V_HEIGHT, new OrthographicCamera()),
				game.batch);
		setupStage();

		oldInPr = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(new MyLittleInPr());

		// load the game map
		game.story.externalCreateRole(TiledMapMetaRole.makeRP((new TmxMapLoader()).load(CommonInfo.INSTRO_FILENAME)));
		// run one update to let the map create the solid tile map and draw layer agents
		game.story.update(1f/60f);
		// run a second update for the map to create the other agents (e.g. player spawner, rooms)
		game.story.update(1f/60f);

		myEye = createEye(game.batch, gamecam);
		game.story.setEye(myEye);
	}

	private Eye createEye(final Batch batch, final OrthographicCamera camera) {
		return new Eye() {
			@Override
			public void setViewCenter(Vector2 viewCenter) {
				camera.position.set(viewCenter, 0f);
				camera.update();
				tileRenderer.setView(camera);
				batch.setProjectionMatrix(camera.combined);
			}
			@Override
			public void draw(Sprite spr) { spr.draw(batch); }
			@Override
			public void draw(TiledMapTileLayer layer) { tileRenderer.renderTileLayer(layer); }
			@Override
			public void begin() { batch.begin(); }
			@Override
			public void end() { batch.end(); }
			@Override
			public boolean isDrawing() { return batch.isDrawing(); }
			// it is a bit odd to create Stage here
			@Override
			public Stage createStage() {
				return new Stage(new FitViewport(CommonInfo.V_WIDTH, CommonInfo.V_HEIGHT, new OrthographicCamera()),
						batch);
			}
		};
	}

	private void setupStage() {
		BitmapFont realFont = new BitmapFont();
		LabelStyle labelFont = new LabelStyle(realFont, Color.WHITE);
		Table table = new Table();
		table.center();
		table.setFillParent(true);
		table.add(new Label("... ][ Kid Ridicarus ][ ...", labelFont)).expandX();
		table.row();
		table.add(new Label(getInstructionsString(), labelFont)).expandX().padTop(20f);
		table.row();
		table.add(new Label("press SPACE to play", labelFont)).expandX().padTop(10f).padBottom(30f);
		stage.addActor(table);
	}

	private CharSequence getInstructionsString() {
		return "KEY - ACTION\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_LEFT).toUpperCase() + "  - move LEFT\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_RIGHT).toUpperCase() + "  - move RIGHT\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_UP).toUpperCase() + "  - move UP\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_DOWN).toUpperCase() + "  - move DOWN\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_RUNSHOOT).toUpperCase() + "  - run/shoot\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_JUMP).toUpperCase() + "  - jump";
	}

	private class MyLittleInPr implements InputProcessor {
		// return true for all the following to relay that the event was handled
		@Override
		public boolean keyDown(int keycode) { return true; }
		@Override
		public boolean keyUp(int keycode) { return doKeyUp(keycode); }
		@Override
		public boolean keyTyped(char character) { return true; }
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) { return true; }
		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) { return true; }
		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) { return true; }
		@Override
		public boolean mouseMoved(int screenX, int screenY) { return true; }
		@Override
		public boolean scrolled(int amount) { return true; }

		private boolean doKeyUp(int keycode) {
			if(keycode == Input.Keys.SPACE)
				goRedTeamGo = true;
			return true;
		}
	}

	@Override
	public void show() {
		// this method is purposely empty
	}

	@Override
	public void render(float delta) {
		update(delta);
		drawScreen();
	}

	private void update(float delta) {
		game.story.update(delta);
		myEye.setViewCenter(UInfo.VectorP2M(CommonInfo.V_WIDTH/2f, CommonInfo.V_HEIGHT/2f));
	}

	private void drawScreen() {
		// clear screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// draw screen
		game.story.draw();

		// draw HUD last
		stage.draw();

		if(goRedTeamGo) {
			dispose();
			game.setScreen(new PlayScreen((MyKidRidicarus) game, nextLevelFilename, null));
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void pause() {
		// this method is purposely empty
	}

	@Override
	public void resume() {
		// this method is purposely empty
	}

	@Override
	public void hide() {
		// this method is purposely empty
	}

	@Override
	public void dispose() {
		Gdx.input.setInputProcessor(oldInPr);
		stage.dispose();
		game.story.removeAllRoles();
		tileRenderer.dispose();
	}
}
