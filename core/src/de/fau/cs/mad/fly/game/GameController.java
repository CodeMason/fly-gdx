package de.fau.cs.mad.fly.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.fau.cs.mad.fly.Debug;
import de.fau.cs.mad.fly.Fly;
import de.fau.cs.mad.fly.features.ICollisionListener;
import de.fau.cs.mad.fly.features.IFeatureDispose;
import de.fau.cs.mad.fly.features.IFeatureFinish;
import de.fau.cs.mad.fly.features.IFeatureInit;
import de.fau.cs.mad.fly.features.IFeatureLoad;
import de.fau.cs.mad.fly.features.IFeatureRender;
import de.fau.cs.mad.fly.features.IFeatureUpdate;
import de.fau.cs.mad.fly.features.game.AsteroidBelt;
import de.fau.cs.mad.fly.features.game.CollectibleObjects;
import de.fau.cs.mad.fly.features.game.EndlessLevelGenerator;
import de.fau.cs.mad.fly.features.game.GateIndicator;
import de.fau.cs.mad.fly.features.overlay.FPSOverlay;
import de.fau.cs.mad.fly.features.overlay.GameFinishedOverlay;
import de.fau.cs.mad.fly.features.overlay.PauseGameOverlay;
import de.fau.cs.mad.fly.features.overlay.SteeringOverlay;
import de.fau.cs.mad.fly.features.overlay.SteeringResetOverlay;
import de.fau.cs.mad.fly.features.overlay.TimeLeftOverlay;
import de.fau.cs.mad.fly.features.overlay.TimeUpOverlay;
import de.fau.cs.mad.fly.features.overlay.TouchScreenOverlay;
import de.fau.cs.mad.fly.player.IPlane;
import de.fau.cs.mad.fly.player.Player;
import de.fau.cs.mad.fly.player.Spaceship;
import de.fau.cs.mad.fly.profile.PlayerManager;
import de.fau.cs.mad.fly.res.Level;
import de.fau.cs.mad.fly.settings.SettingManager;
import de.fau.cs.mad.fly.ui.UI;

/**
 * Manages the Player, the Level, the UI, the CameraController and all the
 * optional Features and calls the load(), init(), render(), finish() and
 * dispose() methods of those.
 * <p>
 * Optional Feature Interfaces:
 *		load():		- called before the game starts while the loading screen is shown
 *					- should be stuff like loading models, creating instances, which takes a while
 * 		init():		- called the moment the game starts after switching to the game screen
 * 					- should be stuff like setting values, resetting counter
 * 		update(): 	- called every frame while the game is running and not paused
 * 					- should be stuff like calculating and updating values
 * 		render(): 	- called every frame while the game is running or paused, in pause the delta time is 0
 * 					- should be stuff like rendering models, showing overlays
 * 		finish(): 	- called at the moment the game is over, still in game screen
 * 					- should be stuff like showing points, saving the highscore
 * 		dispose(): 	- called when the game screen is left
 * 					- should be stuff like disposing models
 * 
 * @author Lukas Hahmann
 */
public class GameController implements TimeIsUpListener{
	public enum GameState {
		RUNNING, PAUSED, FINISHED
	}

	protected Stage stage;
	protected List<IFeatureLoad> optionalFeaturesToLoad;
	protected List<IFeatureInit> optionalFeaturesToInit;
	protected List<IFeatureUpdate> optionalFeaturesToUpdate;
	protected List<IFeatureRender> optionalFeaturesToRender;
	protected List<IFeatureDispose> optionalFeaturesToDispose;
	protected List<IFeatureFinish> optionalFeaturesToFinish;
	protected FlightController flightController;
	protected PerspectiveCamera camera;
	protected ModelBatch batch;
	protected Level level;
	private GameState gameState;
	private TimeController timeController;

	/** Use Builder to initiate GameController */
	protected GameController() {
	}

	/**
	 * Getter for the model batch used to draw the 3d game.
	 * 
	 * @return ModelBatch
	 */
	public ModelBatch getBatch() {
		return batch;
	}

	/**
	 * Getter for the stage.
	 * 
	 * @return {@link #stage}
	 */
	public Stage getStage() {
		return stage;
	}

	/**
	 * Getter for the camera controller.
	 * 
	 * @return {@link #flightController}
	 */
	public FlightController getCameraController() {
		return flightController;
	}

	/**
	 * Getter for the camera.
	 * 
	 * @return {@link #camera}
	 */
	public PerspectiveCamera getCamera() {
		return camera;
	}

	/**
	 * Getter for the level.
	 * 
	 * @return {@link #level}
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * Setter for the level.
	 */
	public void setLevel(Level level) {
		this.level = level;
	}

	/**
	 * This method is called, while the level is loading. It loads everything
	 * the default functions need. Furthermore all optional features in
	 * {@link #optionalFeaturesToLoad} are loaded.
	 */
	public void loadGame() {
		// loads all optional features
		for (IFeatureLoad optionalFeature : optionalFeaturesToLoad) {
			optionalFeature.load(this);
		}
	}

	/**
	 * This method is called, when the level is initialized. It initializes all
	 * default functions that are needed in all levels, like render the level.
	 * Furthermore all optional features in {@link #optionalFeaturesToInit} are
	 * initialized.
	 */
	public void initGame() {
		camera = flightController.getCamera();

		// initializes all optional features
		for (IFeatureInit optionalFeature : optionalFeaturesToInit) {
			optionalFeature.init(this);
		}

		//TODO: number of lifes should not be defined statically
		PlayerManager.getInstance().getCurrentPlayer().setLives(10);
		Debug.setOverlay(0, PlayerManager.getInstance().getCurrentPlayer().getLives());

		startGame();
		Gdx.app.log("GameController.initGame", "OK HAVE FUN!");
	}

	/**
	 * Sets the game state to running.
	 */
	public void startGame() {
		gameState = GameState.RUNNING;
		timeController.initAndStartTimer(level.getLeftTime());
	}

	/**
	 * Sets the game state to paused.
	 */
	public void pauseGame() {
		gameState = GameState.PAUSED;
		timeController.pause();
	}

	/**
	 * Sets the game state to finished and ends the game.
	 */
	public void finishGame() {
		System.out.println("FINISHED");
		gameState = GameState.FINISHED;
		endGame();
	}
	
	/**
	 * Sets the game from paused to running
	 */
	public void resumeGame() {
		gameState = GameState.RUNNING;
		timeController.resume();
	}

	/**
	 * Getter for the game state.
	 * 
	 * @return GameState
	 */
	public GameState getGameState() {
		return gameState;
	}

	/**
	 * Checks if the game is running.
	 * 
	 * @return true if the game is running, otherwise false.
	 */
	public boolean isRunning() {
		if (gameState == GameState.RUNNING)
			return true;
		return false;
	}

	/**
	 * Checks if the game is paused.
	 * 
	 * @return true if the game is paused, otherwise false.
	 */
	public boolean isPaused() {
		if (gameState == GameState.PAUSED)
			return true;
		return false;
	}

	/**
	 * This method is called every frame. Furthermore all optional features in
	 * {@link #optionalFeaturesToRender} are updated and rendered.
	 * 
	 * @param delta
	 *            Time after the last call.
	 */
	public void renderGame(float delta) {
		stage.act(delta);

		if (gameState == GameState.RUNNING) {
			camera = flightController.recomputeCamera(delta);

			level.update(delta, camera);

			// update optional features if the game is not paused
			for (IFeatureUpdate optionalFeature : optionalFeaturesToUpdate) {
				optionalFeature.update(delta);
			}

			CollisionDetector.getInstance().perform(delta);
			timeController.checkTime();
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		batch.begin(camera);
		level.render(delta, batch, camera);
		batch.end();
		// TODO: care about begin()/end() from Batch / Stage / ShapeRenderer
		// etc., split render up?

		// render optional features
		for (IFeatureRender optionalFeature : optionalFeaturesToRender) {
			optionalFeature.render(delta);
		}

		stage.draw();
	}

	/**
	 * This method is called when the game is over. Furthermore all optional
	 * features in {@link #optionalFeaturesToFinish} are finished.
	 */
	public void endGame() {
	    pauseGame();
		for (IFeatureFinish optionalFeature : optionalFeaturesToFinish) {
			optionalFeature.finish();
		}
	}

	/**
	 * This method is called when the game is over. Furthermore all optional
	 * features in {@link #optionalFeaturesToDispose} are disposed.
	 */
	public void disposeGame() {
		for (IFeatureDispose optionalFeature : optionalFeaturesToDispose) {
			//Gdx.app.log("GameController.disposeGame", "dispose: " + optionalFeature.getClass().getSimpleName());
			optionalFeature.dispose();
		}
		CollisionDetector.getInstance().dispose();
		optionalFeaturesToUpdate.clear();
		optionalFeaturesToRender.clear();
	}
	
	public void setTimeController(TimeController timeController) {
		this.timeController = timeController;
		timeController.registerTimeIsUpListener(this);

	}
	
	public TimeController getTimeController() {
		return timeController;
	}
	
	@Override
	public void timeIsUp() {
	    pauseGame();
	}
}
