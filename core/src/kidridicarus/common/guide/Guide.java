package kidridicarus.common.guide;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import kidridicarus.agency.tool.Ear;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.AudioInfo;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.role.player.PlayerControllerRole;
import kidridicarus.story.Story;

/*
 * Guide AKA Player (not actually tho - eventually this class will split into Player and others).
 * Handles:
 *   -music changes
 *   -returning info about state of play.
 */
public class Guide implements Disposable {
	private AssetManager manager;
	private Story story;
	private OrthogonalTiledMapRenderer tileRenderer;
	private PlayerControllerRole playerController;
	private Eye eye;
	private Ear ear;

	private String currentMainMusicName;
	private Music currentMainMusic;
	private boolean isMainMusicPlaying;
	private Music currentSinglePlayMusic;

	public Guide(AssetManager manager, Batch batch, OrthographicCamera camera, Story story) {
		this.manager = manager;
		this.story = story;
		tileRenderer = new OrthogonalTiledMapRenderer(null, UInfo.P2M(1f), batch);

		playerController = null;
		currentMainMusicName = "";
		currentMainMusic = null;
		isMainMusicPlaying = false;
		currentSinglePlayMusic = null;
		story.setEar(createEar());
		story.setEye(createEye(batch, camera));
	}

	// create an Ear to give to Agency, so that Guide can receive sound/music callbacks from Agency
	private Ear createEar() {
		ear = new Ear() {
			@Override
			public void registerMusic(String musicName) {
				manager.load(musicName, Music.class);
				manager.finishLoading();
			}
			@Override
			public void playSound(String soundName) {
				manager.get(soundName, Sound.class).play(AudioInfo.SOUND_VOLUME);
			}
			@Override
			public void changeAndStartMainMusic(String musicName) { doChangeAndStartMainMusic(musicName); }
			@Override
			public void startSinglePlayMusic(String musicName) { doStartSinglePlayMusic(musicName); }
			@Override
			public void stopAllMusic() { doStopAllMusic(); }
		};
		return ear;
	}

	private void doChangeAndStartMainMusic(String musicName) {
		// exit if no name given or if already playing given music
		if(musicName == null || currentMainMusicName.equals(musicName))
			return;

		// stop main music if it is playing
		if(currentMainMusic != null)
			currentMainMusic.stop();

		// if music name is an empty string, then do not start music
		if(!musicName.equals("")) {
			currentMainMusic = manager.get(musicName, Music.class);
			startMainMusic();
		}
		currentMainMusicName = musicName;
	}

	private void startMainMusic() {
		if(currentMainMusic != null) {
			currentMainMusic.setLooping(true);
			currentMainMusic.setVolume(AudioInfo.MUSIC_VOLUME);
			currentMainMusic.play();
			isMainMusicPlaying = true;
		}
	}

	private void doStopMainMusic() {
		if(currentMainMusic != null) {
			currentMainMusic.stop();
			isMainMusicPlaying = false;
		}
	}

	// play music, no loop (for things like mario powerstar)
	private void doStartSinglePlayMusic(String musicName) {
		// pause the current music
		if(currentMainMusic != null)
			currentMainMusic.pause();

		// if single play music is already playing, then stop it before starting new single play music
		if(currentSinglePlayMusic != null)
			currentSinglePlayMusic.stop();

		currentSinglePlayMusic = manager.get(musicName, Music.class);
		currentSinglePlayMusic.setLooping(false);
		currentSinglePlayMusic.setVolume(AudioInfo.MUSIC_VOLUME);
		currentSinglePlayMusic.play();
		currentSinglePlayMusic.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(Music music) {
				returnToMainMusic();
			}});
	}

	// resume the current music if it was playing
	private void returnToMainMusic() {
		if(currentMainMusic != null && isMainMusicPlaying)
			currentMainMusic.play();
	}

	private void doStopAllMusic() {
		// stop current main music if playing
		if(currentMainMusic != null)
			currentMainMusic.stop();
		currentMainMusic = null;
		// stop single play music if playing
		if(currentSinglePlayMusic != null)
			currentSinglePlayMusic.stop();
		currentSinglePlayMusic = null;
	}

	private Eye createEye(final Batch batch, final OrthographicCamera camera) {
		if(eye != null)
			throw new IllegalStateException("Cannot create second eye.");
		eye = new Eye() {
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
		return eye;
	}

	public void createPlayerRole(ObjectProperties playerRoleProperties) {
		playerController = (PlayerControllerRole)
				story.externalCreateRole(PlayerControllerRole.makeRP(playerRoleProperties));
	}

	public boolean isGameWon() {
		return playerController.isGameWon();
	}

	public boolean isGameOver() {
		return playerController.isGameOver();
	}

	public String getNextLevelFilename() {
		return playerController.getNextLevelFilename();
	}

	public ObjectProperties getCopyPlayerRoleProperties() {
		return playerController.getCopyPlayerRoleProperties();
	}

	@Override
	public void dispose() {
		if(ear != null) {
			doStopMainMusic();
			story.setEar(null);
			ear = null;
		}
		if(eye != null) {
			story.setEye(null);
			eye = null;
		}
		tileRenderer.dispose();
	}
}
